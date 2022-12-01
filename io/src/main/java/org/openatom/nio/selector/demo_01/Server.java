package org.openatom.nio.selector.demo_01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * NIO非阻塞模型通信Server端
 * Selector何时不阻塞:触发某些事件如READ,WRITE等是就不会阻塞
 */
public class Server {
    public static void main(String[] args) {
        try {
            System.out.println("Server start......");
            //1.获取ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //2.切换为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //3.绑定连接的端口
            serverSocketChannel.bind(new InetSocketAddress(8001));
            //4.获取选择器Selector
            Selector selector = Selector.open();
            //5.将通道注册到Selector上,并指定监听事件为 接收事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //6.轮询方式获取已经注册到Selector上的事件
            while(selector.select() > 0) {
                System.out.println("开始一轮事件处理......");
                //7.获取已经注册到Selector上的所有事件
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                //8.遍历所有事件
                while (selectionKeyIterator.hasNext()) {
                    //9.获取每一个事件
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    if(selectionKey.isAcceptable()) {
                        //10.直接获取当前接入的客户端通道
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        //11.设置为非阻塞式通道
                        socketChannel.configureBlocking(false);
                        //12.将本客户端通道注册到Selector
                        socketChannel.register(selector,SelectionKey.OP_READ);
                    }else if(selectionKey.isReadable()) {
                        //13.通过SelectionKey反向获取当前选择器上的读事件
                        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                        //14.读取数据
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int len = 0;
                        while((len = socketChannel.read(buffer)) > 0) {
                            //切换读取模式
                            buffer.flip();
                            System.out.println(new String(buffer.array(),0,len));
                            //清除缓冲区中的数据,让下一次的数据可以再写入到缓冲区中
                            buffer.clear();
                        }
                    }
                }
                //从所有事件中移除已经处理过的事件
                selectionKeyIterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
