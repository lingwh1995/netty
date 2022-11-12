package org.openatom.io.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO非阻塞网络编程Server端
 */
public class NIOServerSocket {
    public static void main(String[] args) throws IOException {
        //创建一个ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //得到一个Selector对象
        Selector selector = Selector.open();

        //绑定一个端口6666,在服务端监听
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //把serverSocketChannel注册到 selector,关注事件为 OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("注册后的SelectionKey的数量:" + selector.keys().size());
        //等待客户端连接
        while(true) {
            //这里我们等待1秒,如果没有事件发生,返回
            if(selector.select(1000) == 0) {
                System.out.println("服务器等待了1秒,无连接......");
                continue;
            }

            //如果返回的 >0,就获取到相关的selectionKey集合
            //1.如果返回的>0,表示已经获取到关注的事件
            //2.selector.selectedKeys() 返回关注事件的集合
            // 通过selectionKeys 反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while(keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();
                //根据selectionKey 对应的通道发生的事件做相应的处理
                if(selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功,生成了一个SocketChannel......" + socketChannel.hashCode());
                    //将SocketChannel设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将当前的socketChannel注册到selector,关注事件为 OP_READ,同时给socketChannel关联一个buffer
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("注册后的SelectionKey的数量:" + selector.keys().size());
                }
                //发生OP_READ
                if(selectionKey.isReadable()) {
                    //通过key,反向获取对应channel
                    SocketChannel channel = (SocketChannel)selectionKey.channel();
                    //获取到该channel关联的buffer
                    ByteBuffer buffer = (ByteBuffer)selectionKey.attachment();
                    channel.read(buffer);
                    System.out.println("from客户端:" + new String(buffer.array()));
                }
                //从集合中手动删除当前的key,防止重复操作
                keyIterator.remove();
            }
        }
    }
}
