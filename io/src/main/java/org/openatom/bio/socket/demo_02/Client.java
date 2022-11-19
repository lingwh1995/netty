package org.openatom.bio.socket.demo_02;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO模型网络通信Client端
 * 目标:服务端接收单个客户端多条消息发送和接收需求
 */
public class Client {
    public static void main(String[] args) {
        OutputStream socketOutputStream = null;
        PrintStream printStream = null;
        try {
            System.out.println("Client启动......");
            //1.获取Socket对象,从Socket对象中可以获得与服务端的连接
            Socket socket = new Socket("127.0.0.1",8002);
            //2.获取输出流
            socketOutputStream = socket.getOutputStream();
            //3.根据输出流获取打印流
            printStream = new PrintStream(socketOutputStream);
            //4.使用打印流打印一行数据
            Scanner scanner = new Scanner(System.in);
            while(true) {
                String line = scanner.nextLine();
                printStream.println(line);
                //5.把数据从内存中写出,一般用在close()方法之前
                printStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socketOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            printStream.close();
        }
    }
}
