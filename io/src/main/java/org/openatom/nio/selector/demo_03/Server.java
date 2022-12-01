package org.openatom.nio.selector.demo_03;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

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
            serverSocketChannel.bind(new InetSocketAddress(8003));
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
                        SelectionKey selectionKey = socketChannel.register(selector, 0, null);
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        System.out.println("socketChannel:{}" + socketChannel);
                    }else if(key.isReadable()) {
                        try {
                            //从SelectionKey得到Channel
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(4);
                            int read = channel.read(buffer);
                            //代表客户端传输完成了
                            if(read == -1) {
                                //客户端主动断开(正常断开)(Client中使用socketChannel.close();)
                                key.cancel();
                                System.out.println(Charset.defaultCharset().decode(buffer));
                            }else {
                                buffer.flip();
                            }
                            System.out.println("from客户端:" + new String(buffer.array()));
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
}
