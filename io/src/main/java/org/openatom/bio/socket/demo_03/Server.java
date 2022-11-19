package org.openatom.bio.socket.demo_03;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO模型网络通信Server端
 * 目标:服务端接收多个客户端多条消息发送和接收需求(一个客户端对应一个线程)
 */
public class Server {
    public static void main(String[] args) {
        try {
            //1.获取ServerSocket
            ServerSocket serverSocket = new ServerSocket(8003);
            System.out.println("Server端启动......");
            while(true) {
                System.out.println("服务端同步阻塞,等待客户端连接中......");
                //2.获取Socket
                Socket socket = serverSocket.accept();
                System.out.println("客户端成功连接到服务端......");
                //3.启动多个客户端连接同一个服务端多线程类
                new Thread(new ServerThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 用于处理多个客户端连接同一个服务端的多线程类
 */
class ServerThread implements Runnable{
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        try {
            //3.从Socket中获取输入流
            InputStream socketInputStream = socket.getInputStream();
            //4.获取包装流
            bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            //5.读取数据
            String message = null;
            while ((message = bufferedReader.readLine()) != null) {
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
