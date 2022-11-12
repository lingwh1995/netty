package org.openatom.io.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO群聊服务端
 * 1.服务器启动并监听6667
 * 2.服务器端接收客户端消息,并实现处理上线和离线、转发
 */
public class GroupChatServer {
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;
    public GroupChatServer() {
        try {
            //得到选择器
            selector = Selector.open();
            //得到ServerSocketChannel
            listenChannel = ServerSocketChannel.open();
            //绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            //设置为非阻塞模式
            listenChannel.configureBlocking(false);
            //将该listenChannel注册到selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
    public void listen() {
        System.out.println("监听线程:" + Thread.currentThread().getName());
        try {
            while(true) {
                int count = selector.select(2000);
                //说明有事件发生
                if(count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    //遍历SelectionKey集合
                    while(iterator.hasNext()) {
                        //取出selectionKey
                        SelectionKey selectionKey = iterator.next();
                        //监听到accept事件
                        if(selectionKey.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            //设置非阻塞
                            sc.configureBlocking(false);
                            //将该sc注册到selector上
                            sc.register(selector,SelectionKey.OP_READ);
                            System.out.println("提示:" + sc.getRemoteAddress() + "上线");
                        }
                        //通道是可读状态
                        if(selectionKey.isReadable()) {
                            //处理读,专门写方法
                            readDate(selectionKey);
                        }
                        //处理完成后从SelectionKey集合中删除该SelectionKey,防止重复处理
                        iterator.remove();
                    }
                }else {
                    System.out.println("等待......");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 读取来自客户端的消息
     * @param selectionKey
     */
    public void readDate(SelectionKey selectionKey) {
        SocketChannel socketChannel = null;
        try {
            //得到socketChannel
            socketChannel = (SocketChannel) selectionKey.channel();
            //创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int count = socketChannel.read(buffer);
            //根据count的值做处理
            if(count > 0) {
                //把缓冲区数据转为字符串
                String message = new String(buffer.array());
                //输出该消息
                System.out.println("from客户端:" + message);
                //同时将消息转发给其他客户端,转发时要要排除自身通道(本质是转发给其他通道)
                sendMessageToOtherClient(message,socketChannel);
            }
        } catch (IOException e1) {
            try {
                System.out.println(socketChannel.getRemoteAddress() + "离线了...");
                //取消注册
                selectionKey.cancel();
                //关闭通道
                socketChannel.close();
            }catch (IOException e2) {
                e2.printStackTrace();
            }
        }

    }

    /**
     * 把消息转发给其他客户端
     * @param message
     * @param selfSocketChannel
     * @throws IOException
     */
    public void sendMessageToOtherClient(String message,SocketChannel selfSocketChannel) throws IOException {
        System.out.println("服务器转发消息中......");
        System.out.println("服务器转发消息给客户端线程:" + Thread.currentThread().getName());
        //遍历所有的注册到selector上的SocketChannel,并排除自身通道
        Set<SelectionKey> socketChannels = selector.keys();
        Iterator<SelectionKey> iterator = socketChannels.iterator();
        while(iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            //通过key获取对应的SocketChannel
            Channel currentChannel = selectionKey.channel();
            if(currentChannel instanceof SocketChannel && currentChannel != selfSocketChannel) {
                //将channel转为SocketChannel
                SocketChannel destChannel = (SocketChannel) currentChannel;
                //将message存储到buffer中
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                //将buffer的数据写入通道中
                destChannel.write(buffer);
            }
        }

    }
}
