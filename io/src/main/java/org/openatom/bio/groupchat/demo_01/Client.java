package org.openatom.bio.groupchat.demo_01;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO聊天室(群聊)
 * 目标：BIO模式下的端口转发思想-Client实现
 * 服务端实现需求：
 * 1.注册端口
 * 2.接收客户端的socket连接，交给一个独立的线程来处理
 * 3.把当前连接的客户端socket存入到一个所谓的在线socket集合中保存
 * 4.接收客户端的消息，然后推送给当前所有的在线socket接收
 */
public class Client {
    public static void main(String[] args){
        try {
            //1.请求与服务端的Socket对象连接
            Socket socket = new Socket("127.0.0.1", 8001);
            System.out.println(socket.hashCode());
            //收消息
            Thread clientThread = new ClientThread(socket);
            clientThread.start();
            while (true){
                //发消息
                OutputStream os = socket.getOutputStream();
                PrintStream ps = new PrintStream(os);
                //3. 使用循环不断的发送消息给服务端接收
                Scanner sc = new Scanner(System.in);
                //System.out.print("client send message：");
                String msg =sc.nextLine();
                ps.println(msg);
                ps.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

/**
 * 用于接收服务端消息的线程
 */
class ClientThread extends Thread {
    private Socket socket;
    public ClientThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try{
            while (true){
                InputStream is = socket.getInputStream();
                //4.把字节输入流包装成一个缓存字符输入流
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String msg;
                /*
                while ((msg = br.readLine()) != null){
                System.out.println("client recive message：" + msg);
                }*/
                if((msg = br.readLine()) != null){
                    System.out.println(msg);
                }
            }
        }catch (Exception e){
        }
    }
}