package org.openatom.bio.socket.demo_05;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO模型示例代码
 *  使用BIO模型编写一个服务器端,监听6666端口,当客户端连接时,就启动一个线程与之通信
 *  并且使用线程池改善机制,使之可以连接多个客户端
 *
 * 测试方法: cmd -> telnet 127.0.0.1 8005 ->直接输入内容/按下Ctrl+]后输入 send +内容 ->查看idea控制台接收到的信息
 * 测试结论:
 *      1.使用BIO模型时,有多少个客户端,服务端就会产生多少个线程,即服务端会为每一个客户端创建一个线程
 *      2.BIO会阻塞
 */
public class Server {

    public static void main(String[] args) throws IOException {
        //创建线程池
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(8005);
        System.out.println("ServerSocket 启动成功了!");

        while(true) {
            System.out.println("当前线程信息:" + Thread.currentThread().getName()
                    + "," + Thread.currentThread().getId());
            //accept()会阻塞
            System.out.println("等待连接......");
            final Socket socket = serverSocket.accept();
            System.out.println("ServerSocket 连接到一个客户端!");

            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    handle(socket);
                }
            });
        }
    }

    /**
     * 和客户端通信的方法
     * @param socket
     */
    public static void handle(Socket socket) {
        try {
            System.out.println("当前线程信息:" + Thread.currentThread().getName()
                    + "," + Thread.currentThread().getId());
            byte[] bytes = new byte[1024];
            InputStream inputStream = socket.getInputStream();
            while (true) {
                System.out.println("当前线程信息:" + Thread.currentThread().getName()
                        + "," + Thread.currentThread().getId());
                //read()会阻塞
                System.out.println("等待读取数据......");
                int read = inputStream.read(bytes);
                if(read != -1) {
                    System.out.println(new String(bytes,0,read));
                }else {
                    break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("ServerSocket 关闭和客户端的连接!");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
