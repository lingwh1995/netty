package org.openatom.bio.file.demo_02;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args)throws Exception {
        Socket socket = new Socket("127.0.0.1", 8002);

        String filePath = "D:\\a.txt";
        FileInputStream fileInputStream = new FileInputStream(filePath);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        byte[] bytes = new byte[4096];
        int readCount = 0;
        //读取数据的总数量
        int total = 0;

        //开始时间
        long startTime = System.currentTimeMillis();

        while ((readCount = fileInputStream.read(bytes)) >= 0){
            total += readCount;
            //向服务端写数据
            dataOutputStream.write(bytes);
        }

        System.out.println("发送总字节数: " + total + ", 耗时: " + (System.currentTimeMillis() - startTime));

        dataOutputStream.close();
        socket.close();
        fileInputStream.close();
    }
}
