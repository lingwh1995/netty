package org.openatom.nio.selector.demo_02;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO非阻塞网络编程Client端
 */
public class Client {
    public static void main(String[] args) throws IOException {
        //得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //提供服务端的ip和端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8002);
        //连接服务器
        if(!socketChannel.connect(inetSocketAddress)) {
            while(!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间,客户端不会阻塞,可以做其他工作");
            }
        }
        //如果连接成功,就发送数据
        String str = "Hello 尚硅谷!";
        //包裹一个字节数组到 buffer 中
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        //发送数据,将buffer数据写入channel
        socketChannel.write(buffer);
        System.in.read();
    }
}
