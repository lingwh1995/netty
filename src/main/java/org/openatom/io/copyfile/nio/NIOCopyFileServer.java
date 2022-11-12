package org.openatom.io.copyfile.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * NIO 零拷贝服务端
 */
public class NIOCopyFileServer {
    public static void main(String[] args)throws Exception {
        //创建一个端口映射到8899端口
        InetSocketAddress address = new InetSocketAddress(8899);

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        //如果想绑定到某一个端口号上，但是那个端口号正处于超时状态，则不能绑定成功，此设置可以绑定成功
        //serverSocket.setReuseAddress(true);
        //绑定端口
        serverSocket.bind(address);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);

        System.out.println("server start");
        while (true){
            SocketChannel socketChannel = serverSocketChannel.accept();

            int readCount = 0;
            while (readCount != -1){
                try {
                    readCount = socketChannel.read(byteBuffer);
                }catch (Exception ex){
                    //ex.printStackTrace();
                    break;
                }

                //重新读,position = 0; mark = -1;
                byteBuffer.rewind();
            }
        }
    }

}
