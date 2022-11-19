package org.openatom.bio.socket.demo_03;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO模型网络通信Client端
 * 目标:服务端接收多个客户端多条消息发送和接收需求(一个客户端对应一个线程)
 */
public class Client {
    public static void main(String[] args) {
        OutputStream socketOutputStream = null;
        PrintStream printStream = null;
        try {
            //1.获取Socket对象,从Socket对象中可以获得与服务端的连接
            Socket socket = new Socket("127.0.1.1", 8003);
            System.out.println(socket.hashCode());
            //2.从Socket中获取输出流
            socketOutputStream = socket.getOutputStream();
            //3.获取打印流
            printStream = new PrintStream(socketOutputStream);
            //4.从键盘录入数据并且通过打印流输出
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                printStream.println(line);
                //5.把数据从内存中刷出
                printStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            printStream.close();
            try {
                if(socketOutputStream != null) {
                    socketOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
