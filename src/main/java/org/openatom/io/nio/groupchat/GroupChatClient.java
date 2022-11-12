package org.openatom.io.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * NIO群聊客户端
 * 1.连接服务器
 * 2.发送消息
 * 3.接收服务器消息
 */
public class GroupChatClient {
    //服务器IP
    private final String HOST = "127.0.0.1";
    //服务器端口
    private static final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    /**
     * 初始化工作
     * @throws IOException
     */
    public GroupChatClient() throws IOException {
        selector = Selector.open();
        //连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST,PORT));
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //将channel注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);
        //得到username
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok ...");
    }

    public static void main(String[] args) throws IOException {
        final GroupChatClient groupChatClient = new GroupChatClient();
        //启动一个线程
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    groupChatClient.readMessage();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //发送数据给服务器端
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()) {
            String message = scanner.nextLine();
            groupChatClient.sendMessage(message);
        }
    }
    /**
     * 向服务器端发送消息
     * @param message
     */
    public void sendMessage(String message) {
        message = username + "说" + message;
        try {
            socketChannel.write(ByteBuffer.wrap(message.getBytes()));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取来自服务器端的消息
     */
    public void readMessage() {
        try {
            int readChannels = selector.select();
            //有可用的通道
            if(readChannels > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isReadable()) {
                        //得到相关的通道
                        SocketChannel sc = (SocketChannel)selectionKey.channel();
                        //得到一个Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //读取
                        sc.read(buffer);
                        //把读取到的缓冲区数据转成字符串
                        String message = new String(buffer.array());
                        System.out.println(message.trim());
                    }
                    //移除已经处理过的key
                    iterator.remove();
                }
            }else {
                //System.out.println("没有可用的通道......");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
