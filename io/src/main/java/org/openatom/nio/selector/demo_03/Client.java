package org.openatom.nio.selector.demo_03;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * NIO非阻塞模型通信Client端
 */
public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("Client start......");
            //1.获取SocketChannel并绑定IP和端口
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",8003));
            //2.切换为非阻塞模式
            socketChannel.configureBlocking(false);
            //3.分配缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //4.发送数据给服务端
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                buffer.put(line.getBytes());
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
