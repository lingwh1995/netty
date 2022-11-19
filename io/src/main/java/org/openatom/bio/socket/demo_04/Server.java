package org.openatom.bio.socket.demo_04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * BIO模型网络通信Server端
 * 目标: 1.服务端接收多个客户端多条消息发送和接收需求(一个客户端对应一个线程)
 *       2.通过线程池实现伪异步通信架构
 * 总结: 伪异步IO使用了线程池进行对线程的连接情况进行了优化,但是其底层还是同步阻塞模型,所以依然无法支撑大的并数量
 *       优势在于,绝对不会引起服务端宕机,因为处理不了的任务会先等待前面的任务执行完成后再执行
 */
public class Server {
    public static void main(String[] args) {
        try {
            //1.获取ServerSocket
            ServerSocket serverSocket = new ServerSocket(8004);
            //2.初始化一个线程池
            ServerThreadPool serverThreadPool = new ServerThreadPool(6, 10);
            System.out.println("Server端启动......");
            while (true) {
                //3.获取Socket
                Socket socket = serverSocket.accept();
                //4.把Socket封装成任务对象交给线程池处理
                ServerRunnableTask task = new ServerRunnableTask(socket);
                //5.使用线程池处理任务
                serverThreadPool.execute(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

/**
 * 用于处理多个客户端连接同一个服务端的线程池类
 */
class ServerThreadPool {
    private ExecutorService executorService;

    /**
     *
     * @param maxThreadNum 线程池最大的线程数量
     * @param taskQueueSize 线程池中任务队列的大小
     */
    public ServerThreadPool(int maxThreadNum, int taskQueueSize) {
        /**
         * 创建一个线程池对象,最好使用这个API进行创建,不推荐使用Executors.xxx()来创建
         * 参数信息：
         * int corePoolSize     核心线程大小，同时可以处理的线程数目，如果超过这个数目，则先会在队列中进行缓存
         * int maximumPoolSize  线程池最大容量大小
         * long keepAliveTime   线程空闲时，线程存活的时间
         * TimeUnit unit        时间单位
         * BlockingQueue<Runnable> workQueue  任务队列大小(一个阻塞队列)
         */
        this.executorService =
                new ThreadPoolExecutor(3, maxThreadNum, 120,
                        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(taskQueueSize));
    }

    /**
     * 提供一个方法来提交任务给线程池的任务队列来缓存,等着线程池来处理
     * @param task
     */
    public void execute(Runnable task) {
        executorService.execute(task);
    }
}


/**
 * 封装了Socket的任务对象
 */
class ServerRunnableTask implements Runnable {
    private Socket socket;

    public ServerRunnableTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        try {
            //1.通过Socket获取输入流
            InputStream socketInputStream = socket.getInputStream();
            //2.获取包装流
            bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            String message = null;
            //3.读取消息
            while((message = bufferedReader.readLine()) != null) {
                System.out.println("来自客户端的消息:" + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}