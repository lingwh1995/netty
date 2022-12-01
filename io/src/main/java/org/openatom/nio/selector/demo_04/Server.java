package org.openatom.nio.selector.demo_04;

import org.openatom.utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * ByteBuffer自动扩容Server端
 */
public class Server {
    public static void main(String[] args) {
        try {
            //1.获取Selector,管理多个channel
            Selector selector = Selector.open();
            //2.获取ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //3.设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //4.绑定端口
            serverSocketChannel.bind(new InetSocketAddress(8004));
            //5.通过SelectionKey可以知道是什么事件和是哪个Channel发生的事件,第二个参数0代表不关注任何事件
            SelectionKey sscKey = serverSocketChannel.register(selector, 0, null);
            //6.只关注accept事件
            sscKey.interestOps(SelectionKey.OP_ACCEPT);
            while (true) {
                //7.没有事件发生select()方法会让线程阻塞,有事件发生,线程就会恢复运行
                selector.select();
                //8.获取所有可以处理的事件
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    System.out.println("key:{}" + key);
                    if(key.isAcceptable()) {
                        ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                        SocketChannel socketChannel = channel.accept();
                        socketChannel.configureBlocking(false);
                        //把buffer作为附件作为socketChannel的参数使用
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        SelectionKey selectionKey = socketChannel.register(selector, 0, buffer);
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        System.out.println("socketChannel:{}" + socketChannel);
                    }else if(key.isReadable()) {
                        try {
                            //从SelectionKey得到Channel
                            SocketChannel channel = (SocketChannel) key.channel();
                            //获取ByteBuffer上关联的附件
                            ByteBuffer buffer = (ByteBuffer)key.attachment();
                            int read = channel.read(buffer);
                            //代表客户端传输完成了
                            if(read == -1) {
                                //客户端主动断开(正常断开)(Client中使用socketChannel.close();)
                                key.cancel();
                                System.out.println(Charset.defaultCharset().decode(buffer));
                            }else {
                                split(buffer);
                                //ByteBuffer扩容
                                System.out.println(buffer.position());
                                System.out.println(buffer.limit());
                                //buffer自动扩容,暂时未实现buffer自动缩容
                                if(buffer.position() == buffer.limit()) {
                                    buffer.flip();
                                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                    newBuffer.put(buffer);
                                    //替换和之前和当前通道关联的ByteBuffer
                                    key.attach(newBuffer);
                                }
                            }
                            ByteBufferUtil.debugAll(buffer);
                            System.out.println("from客户端:" + new String(buffer.array(),0,buffer.position()));
                            buffer.clear();
                        }catch (IOException e) {
                            e.printStackTrace();
                            //异常断开从selector中反注册
                            key.cancel();
                        }
                    }

                    //如果不处理事件,则调用cancel()方法
                    //selectionKey.cancel();
                }
                //一定要有下面这行代码,否则会报异常
                selectionKeyIterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理黏包半包
     * @param source
     */
    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                ByteBufferUtil.debugAll(target);
            }
        }
        source.compact();
    }
}
