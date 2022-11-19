package org.openatom.nio.channel;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO Channel
 * 1.通道类似于IO中的流,但是不能直接访问数据,只能与Buffer进行交互
 * 2.通道可以同时读或者写,是双向的,流不能同时读或者写,是单向的
 * 3.通道可以实现异步读写数据
 * 4.通道可以从Buffer中读取数据,也可以从Buffer中把数据读出来
 *
 * 注意: 是关闭FileInputStream和FileOutputStream,而不是关闭Channel
 */
public class ChannelTest {

    /**
     * 读取、写入文件
     * @throws IOException
     */
    @Test
    public void fun1() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        //目标文件路径
        String destPath = "D:\\a1.txt";
        write(destPath);
        read(srcPath);
    }

    /**
     * 读取和写入
     * @throws IOException
     */
    @Test
    public void fun2() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        //目标文件路径
        String destPath = "D:\\a1.txt";
        readAndWrite(srcPath,destPath);
    }

    /**
     * 使用transferFrom()和transferTo()拷贝文件/使用通道传输文件
     * @throws IOException
     */
    @Test
    public void fun3() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        //目标文件路径
        String destPath1 = "D:\\a1.txt";
        String destPath2 = "D:\\a2.txt";
        //使用transferFrom()方法
        nioCopyFileTransferFrom(srcPath,destPath1);
        //使用transferTo()方法
        nioCopyFileTransferTo(srcPath,destPath2);
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
     * 分散读取(Scatter):把通道数据读入到多个Buffer中
     * 聚集写入(Gathering):将多个Buffer中数据聚集到Channel中
     * 可以减少数据从channel复制到buffer中的数据
     * @throws IOException
     */
    @Test
    public void fun5() throws IOException {
        //源文件路径
        String srcPath = "D:\\a.txt";
        //目标文件路径
        String destPath = "D:\\a1.txt";
        //使用NIO拷贝文件(文本文件、图片文件)
        scatterAndGathering(srcPath,destPath);
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


    private void nioCopyFileTransferFrom(String srcPath,String destPath) throws IOException {
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

    private void nioCopyFileTransferTo(String srcPath,String destPath) throws IOException {
        //创建输入流对象
        FileInputStream fileInputStream = new FileInputStream(srcPath);
        //获取输入流通道
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        //创建输出流对象
        FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        //获取输入流通道
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();

        //拷贝图片
        fileInputStreamChannel.transferTo(fileInputStreamChannel.position(),fileInputStreamChannel.size(),fileOutputStreamChannel);
        fileInputStreamChannel.close();
        fileOutputStreamChannel.close();
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
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        while (true) {
            //将channel的数据读取到buffer
            int len = channel.read(byteBuffer);
            if(len == -1) {
                break;
            }
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                byte b = byteBuffer.get();
                System.out.print((char) b);
            }
            //清空buffer中的内容
            byteBuffer.clear();
            //将buffer中的数据装换为String类型数据并进行输出
            //System.out.println(new String(byteBuffer.array(),0,byteBuffer.remaining()));
        }
        //关闭通道
        channel.close();
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
        //关闭通道
        channel.close();
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
     * Buffer的分散和集聚集
     * @param srcPath
     * @param destPath
     * @throws IOException
     */
    private void scatterAndGathering(String srcPath,String destPath) throws IOException {
        //创建输入流对象
        FileInputStream fileInputStream = new FileInputStream(srcPath);
        //获取输入流通道
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        //创建输出流对象
        FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        //获取输入流通道
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();

        //定义多个缓冲区做数据分散
        ByteBuffer buffer1 = ByteBuffer.allocate(2);
        ByteBuffer buffer2 = ByteBuffer.allocate(2);
        ByteBuffer[] buffers = {buffer1,buffer2};
        while (true) {
            buffer1.clear();
            buffer2.clear();
            //从通道中读取数据分散到各个缓冲区
            long read = fileInputStreamChannel.read(buffers);
            if(read == -1) {
                break;
            }
            //切换读写模式
            buffer1.flip();
            buffer2.flip();

            //从每个缓冲区中查看数据是否读取到了
            for (ByteBuffer buffer : buffers) {
                System.out.println("当前buffer中的数据:" + new String(buffer.array(), 0, buffer.remaining()));
            }
            //将多个Buffer中数据聚集到Channel中
            fileOutputStreamChannel.write(buffers);
        }

        fileInputStream.close();
        fileOutputStream.close();
    }
}
