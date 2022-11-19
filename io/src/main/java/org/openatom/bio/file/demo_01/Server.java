package org.openatom.bio.file.demo_01;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO模型网络通信Server端
 * 目标: 实现客户端上传任意类型的文件数据给服务端保存起来
 */
public class Server {
    public static void main(String[] args) {
        try {
            //1.获取ServerSocket
            ServerSocket serverSocket = new ServerSocket(8001);
            System.out.println("Server start......");
            while (true) {
                //2.获取Socket
                Socket socket = serverSocket.accept();
                //3.接收文件并存储到另一个地方
                new Thread(new ServerThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


class ServerThread implements Runnable {

    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        try {
            //1.获取输入流
            InputStream socketInputStream = socket.getInputStream();
            //2.获得数据输入流
            DataInputStream dataInputStream = new DataInputStream(socketInputStream);
            //3.定义输出流
            String suffix = dataInputStream.readUTF();
            System.out.println("服务端成功接收到文件后缀名......");
            String fileName = "a1" + suffix;
            fileOutputStream = new FileOutputStream("D:\\" + fileName);
            //4.写文件
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = dataInputStream.read(buffer)) > 0 ) {
                fileOutputStream.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}