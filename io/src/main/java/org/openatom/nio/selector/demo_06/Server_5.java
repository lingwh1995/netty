package org.openatom.nio.selector.demo_06;

import org.openatom.utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server 5.0 解决单线程(单客户端)读取正常,多线程(单客户端)读取会出问题的bug
 *      创建多个worker线程
 */
public class Server_5 {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = serverSocketChannel.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8006));
        //1.创建固定数量的Worker,这个数量最少为CPU的核心个数,建议手工指定,否则部署在docker中会出现问题
        //docker中的bug https://www.bilibili.com/video/BV1py4y1E7oA?p=46&spm_id_from=pageDriver&vd_source=a85210c92bb85bfec6cc4d48374b8ad0
        //Worker[] workers = new Worker[2];
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
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
                    //轮询运行在多个worker中
                    workers[index.getAndIncrement() % workers.length].register(socketChannel);
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
        //异步队列
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

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
            //向队列中添加任务,但是这个任务没有立刻执行
            queue.add(()->{
                try {
                    //2.关联selector
                    socketChannel.register(selector,SelectionKey.OP_READ,null);//在boss线程中被添加,运行在worker线程中,因为是worker线程调用的
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            //唤醒selector
            selector.wakeup();//在boss线程中被添加
        }

        @Override
        public void run() {
            System.out.println("run......");
            while (true) {
                try {
                    selector.select();//worker-0线程中运行
                    Runnable task = queue.poll();
                    if(task != null) {
                        //执行 socketChannel.register(selector,SelectionKey.OP_READ,null);
                        task.run();
                    }
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
                            System.out.println("当前工作线程:" + Thread.currentThread().getName());
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

