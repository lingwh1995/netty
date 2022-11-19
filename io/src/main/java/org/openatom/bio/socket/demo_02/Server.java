package org.openatom.bio.socket.demo_02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO模型网络通信Server端
 * 目标:服务端接收单个客户端多条消息发送和接收需求
 */
public class Server {
    public static void main(String[] args) {
        BufferedReader bufferedReader = null;
        try {
            System.out.println("ServerSocket启动......");
            //1.创建ServerSocket
            ServerSocket serverSocket = new ServerSocket(8002);
            //2.获取Socket
            Socket socket = serverSocket.accept();
            //3.获取输入流
            InputStream socketInputStream = socket.getInputStream();
            //4.获取包装后的输入流
            bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            //5.读取数据
            String message = null;
            while((message = bufferedReader.readLine()) != null) {
                System.out.println("来自客户端的消息:" + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //使用装饰流时,只需要关闭最后的装饰流即可
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
