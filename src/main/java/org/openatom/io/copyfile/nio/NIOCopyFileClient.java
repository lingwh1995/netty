package org.openatom.io.copyfile.nio;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * NIO 零拷贝客户端
 */
public class NIOCopyFileClient {
    public static void main(String[] args)throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8899));

        String filePath = "D:\\a.txt";
        FileChannel channel = new FileInputStream(filePath).getChannel();

        //系统毫秒数
        long startTime = System.currentTimeMillis();
        //transferTo(传输的起始位置,传输的最大字节数,目标channel)
        //transferTo(底层使用零拷贝):Linux下调用一次就可以完成传输,Windows下每次最多传输8M,需要将文件分段
        long transferCount = channel.transferTo(0, channel.size(), socketChannel);

        System.out.println("发送总字节数：" + transferCount +
                ", 耗时：" + (System.currentTimeMillis() - startTime));

    }
}
