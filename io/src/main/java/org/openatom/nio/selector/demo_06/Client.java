package org.openatom.nio.selector.demo_06;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Client
 */
public class Client {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.1",8006));
            socketChannel.write(Charset.defaultCharset().encode("123456789"));
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
