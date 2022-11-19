package org.openatom.bio.file.demo_02;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args)throws Exception {
        ServerSocket serverSocket = new ServerSocket(8002);
        System.out.println("server start");
        while (true){
            //阻塞，等待连接到来
            Socket socket = serverSocket.accept();

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            try {
                byte[] bytes = new byte[4096];
                while (true){
                    int readCount = dataInputStream.read(bytes);
                    if(readCount == -1){
                        break;
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}

