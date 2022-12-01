package org.openatom.nio.selector.demo_06;

import org.openatom.utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Server 3.0 解决单线程(单客户端)读取正常,多线程(单客户端)读取会出问题的bug
 */
public class Server_3 {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = serverSocketChannel.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8006));
        //1.创建固定数量的Worker
        Worker worker = new Worker("worker-0");
        while (true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    System.out.println("connected..." + socketChannel.getRemoteAddress());
                    System.out.println("before register..." + socketChannel.getRemoteAddress());
                    worker.register(socketChannel);//运行在worker-0中
                    System.out.println("after register..." + socketChannel.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable{
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;

        public Worker(String name) {
            this.name = name;
        }

        /**
         * 初始化线程和selector
         * 用于启动Worker线程
         */
        public void register(SocketChannel socketChannel) throws IOException {
            System.out.println("register......");
            if(!start) {
                thread = new Thread(this, name);
                selector = Selector.open();
                thread.start();
                start = true;
            }
            //唤醒selector
            selector.wakeup();//在boss线程中被运行
            //2.关联selector
            socketChannel.register(selector,SelectionKey.OP_READ,null);//在boss线程中被运行
        }

        @Override
        public void run() {
            System.out.println("run......");
            while (true) {
                try {
                    selector.select();//worker-0线程中运行
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            System.out.println("read..." + channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            ByteBufferUtil.debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

