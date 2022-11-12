package org.openatom.io.nio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO使用Channel完成读写功能
 *  注意: 是关闭FileInputStream和FileOutputStream,而不是关闭Channel
 */
public class NIOFileChannel {
    @Test
    public void fun1() throws IOException {
        String filePath = "D:\\a.txt";
        write(filePath);
        read(filePath);
    }

    @Test
    public void fun2() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        //目标文件路径
        String destPath = "D:\\a-copy.txt";
        readAndWrite(srcPath,destPath);
    }

    @Test
    public void fun3() throws IOException {
        //源文件路径
        String srcPath = "D:\\b.txt";
        //目标文件路径
        String destPath = "D:\\c.txt";
        //使用NIO拷贝文件(文本文件、图片文件)
        nioCopyFile(srcPath,destPath);
    }

    /**
     * MappedByteBuffer:直接让文件在内存(堆外内存)中进行修改,即操作系统无需将文件拷贝一次,效率比较高
     */
    @Test
    public void fun4() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        mappedByteBuffer(srcPath);
    }

    /**
     * Buffer的分散和集聚集
     * @throws IOException
     */
    @Test
    public void fun5() throws IOException {

    }
    private void mappedByteBuffer(String srcPath) throws IOException {
        //创建流对象
        RandomAccessFile randomAccessFile = new RandomAccessFile(srcPath, "rw");
        //创建通道
        FileChannel randomAccessFileChannel = randomAccessFile.getChannel();

        /**
         * 参数说明
         *  参数0: 读写模式
         *  参数1: 可以直接修改的起始位置
         *  参数2: 映射到内存的大小,将a.txt的多少个字节映射到内存中
         */
        MappedByteBuffer buffer = randomAccessFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4);
        //修改第0个位置
        buffer.put(0,(byte) 'T');
        buffer.put(1,(byte) 'E');
        buffer.put(2,(byte) 'S');
        buffer.put(3,(byte) 'T');

        //关闭流
        randomAccessFile.close();
    }


    private void nioCopyFile(String srcPath,String destPath) throws IOException {
        //创建输入流对象
        FileInputStream fileInputStream = new FileInputStream(srcPath);
        //获取输入流通道
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        //创建输出流对象
        FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        //获取输入流通道
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();

        //拷贝图片
        fileOutputStreamChannel.transferFrom(fileInputStreamChannel,0,fileInputStreamChannel.size());
        fileInputStreamChannel.close();
        fileOutputStreamChannel.close();
        fileInputStream.close();
        fileOutputStream.close();
    }

    /**
     * NIO读取和写入数据功能
     * @throws IOException
     */
    private void readAndWrite(String srcPath,String destPath) throws IOException {
        //创建输入流对象
        FileInputStream fileInputStream = new FileInputStream(srcPath);
        //获取输入流通道
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        //创建输出流对象
        FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        //获取输入流通道
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();

        //创建Buffer
        //ByteBuffer buffer = ByteBuffer.allocate(512);
        ByteBuffer buffer = ByteBuffer.allocate(5);
        while(true) {
            //清空buffer,否则buffer中永远都是上一次从channle中读取到的值,而且read的
            //值一直为0(positon和limit的值是相等的),循环永远无法退出
            buffer.clear();
            int read = fileInputStreamChannel.read(buffer);
            System.out.println(read);
            if(read == -1) {
                break;
            }
            //切换读写模式
            buffer.flip();
            //将buffer中的数据写入到输出流通道中
            fileOutputStreamChannel.write(buffer);
        }
        fileInputStream.close();
        fileOutputStream.close();
    }

    /**
     * NIO读取数据功能
     * @param srcPath
     * @throws IOException
     */
    private static void read(String srcPath) throws IOException {
        //创建输入流对象
        FileInputStream fileInputStream = new FileInputStream(srcPath);
        //通过这个FileInputStream获取对应的FileChannel
        FileChannel channel = fileInputStream.getChannel();
        //创建一个缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //将channel的数据读取到buffer
        channel.read(byteBuffer);
        //将buffer中的数据装换为String类型数据并进行输出
        System.out.println(new String(byteBuffer.array(),0,byteBuffer.position()));
        //关闭输入流
        fileInputStream.close();
    }

    /**
     * NIO写入数据功能
     * @param destPath
     * @throws IOException
     */
    private static void write(String destPath) throws IOException {
        String str = "Hello,西安财经学院！";
        //创建一个输出流对象
        FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        //通过这个FileOutputStream获取对应的FileChannel
        FileChannel channel = fileOutputStream.getChannel();
        //创建一个缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //将str放入ByteBuffer
        byteBuffer.put(str.getBytes());
        //切换读写模式
        byteBuffer.flip();
        //把缓冲区的数据写入到文件中
        channel.write(byteBuffer);
        //关闭输出流
        fileOutputStream.close();
    }

}
