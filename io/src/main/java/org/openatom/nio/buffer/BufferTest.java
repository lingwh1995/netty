package org.openatom.nio.buffer;

import org.junit.Test;
import org.openatom.utils.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BufferTest {

    /**
     * Buffer基础使用
     */
    @Test
    public void fun1() {
        //1.分配一个缓冲区,容量设置为10
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        getBufferInfo(byteBuffer);

        //2.给Buffer中添加数据
        String str = "Hello NIO";
        byteBuffer.put(str.getBytes());
        getBufferInfo(byteBuffer);

        //3.切换读写模式(将position设置为0并从0开始读取数据)
        byteBuffer.flip();

        //4.设置读取元素的起始位置和结束位置
        //读取元素起始位置
        //byteBuffer.position(1);
        //读取元素结束位置
        //byteBuffer.limit(4);

        getBufferInfo(byteBuffer);
        while(byteBuffer.hasRemaining()) {
            System.out.println((char)byteBuffer.get());
        }

    }

    /**
     * clear()
     */
    @Test
    public void fun2() {
        //1.分配一个缓冲区,容量设置为10
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        getBufferInfo(byteBuffer);

        //2.给Buffer中添加数据
        String str = "Hello NIO";
        byteBuffer.put(str.getBytes());
        getBufferInfo(byteBuffer);

        //3.测试clear()方法,这个方法会将position重置为0,但是还没有真正的清除Buffer中的数据
        byteBuffer.clear();
        getBufferInfo(byteBuffer);

        //调用clear()方法后再调用put()方法才会有真正清除数据的效果
        byteBuffer.put("AAA".getBytes());

        while(byteBuffer.hasRemaining()) {
            System.out.println((char)byteBuffer.get());
        }
    }

    /**
     * 1.使用字节数组读取
     * 2.mark()+reset()使用
     */
    @Test
    public void fun3() {
        //1.分配一个缓冲区,容量设置为10
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        getBufferInfo(byteBuffer);

        //2.给Buffer中添加数据
        String str = "Hello NIO";
        byteBuffer.put(str.getBytes());
        getBufferInfo(byteBuffer);

        byteBuffer.flip();

        byte[] b1 = new byte[5];
        byteBuffer.get(b1);
        System.out.println(new String(b1));
        getBufferInfo(byteBuffer);

        //标记此刻位置,position位置为5
        byteBuffer.mark();

        byte[] b2 = new byte[2];
        byteBuffer.get(b2);
        System.out.println(new String(b2));
        //position位置为7
        getBufferInfo(byteBuffer);

        //回到刚才标记位置,position位置又回到5
        byteBuffer.reset();
        getBufferInfo(byteBuffer);
        while (byteBuffer.hasRemaining()) {
            System.out.println((char)byteBuffer.get());
        }
    }


    /**
     * 直接内存:物理内存,性能高,申请慢,适用于数据量大,IO生命周期长或者IO次数频繁
     * 非直接内容:jvm堆内存,性能地,申请快
     */
    @Test
    public void fun4(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println(byteBuffer.isDirect());
    }
    /**
     * 获取当前Buffer的信息
     * @param byteBuffer
     */
    private void getBufferInfo(ByteBuffer byteBuffer) {
        System.out.println("position:" + byteBuffer.position());
        System.out.println("limit:" + byteBuffer.limit());
        System.out.println("capacity:" + byteBuffer.capacity());
        //System.out.println("mark:" + byteBuffer.mark());
        System.out.println("-------------------------------");
    }

    /**
     * Buffer的类型化,即使用什么数据类型put(),就使用什么数据类型get()
     */
    @Test
    public void fun5() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(1);
        buffer.putChar('你');
        buffer.putDouble(2.5);
        buffer.putLong(10000000);

        //切换读写模式
        buffer.flip();

        //正确读取
//        System.out.println(buffer.getInt());
//        System.out.println(buffer.getChar());
//        System.out.println(buffer.getDouble());
//        System.out.println(buffer.getLong());

        //错误读取
        System.out.println(buffer.getInt());
        System.out.println(buffer.getInt());
        System.out.println(buffer.getInt());
        System.out.println(buffer.getInt());
    }


    @Test
    public void fun6() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(1);
        //将buffer设置为只读模式,如果再次写入内容,则会报下面的异常
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        //ReadOnlyBufferException
        readOnlyBuffer.putInt(2);
    }

    /**
     * 1.使用字节数组读取
     * 2.rewind()使用,让position位置归到0,用于重复读取
     */
    @Test
    public void fun7() {
        //1.分配一个缓冲区,容量设置为10
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        getBufferInfo(byteBuffer);

        //2.给Buffer中添加数据
        String str = "Hello NIO";
        byteBuffer.put(str.getBytes());
        getBufferInfo(byteBuffer);

        byteBuffer.flip();

        byte[] b1 = new byte[5];
        byteBuffer.get(b1);
        System.out.println(new String(b1));

        //将position位置重置为0
        byteBuffer.rewind();
        byteBuffer.get(b1);
        System.out.println(new String(b1));
    }

    /**
     * 把字符串转换为ByteBuffer
     */
    @Test
    public void fun8() {
        //1.字符串转换为ByteBuffer(没有直接将ByteBuffer切换为读模式,因为position值为5,limit值为16)
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("Hello".getBytes());
        ByteBufferUtil.debugAll(buffer1);

        //2.使用StandardCharsets转换(直接将ByteBuffer切换为读模式,因为position值为0,limit值为5)
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("Hello");
        ByteBufferUtil.debugAll(buffer2);


        //3.使用warp()方法(直接将ByteBuffer切换为读模式,因为position值为0,limit值为5)
        ByteBuffer buffer3 = ByteBuffer.wrap("Hello".getBytes());
        ByteBufferUtil.debugAll(buffer3);

        //decode()方法失效的原因是因为使用position往后找的,没有切换为读模式,position的位置没有归到0,所以读出来的数据是错误的
        System.out.println("错误的buffer1:" + StandardCharsets.UTF_8.decode(buffer1).toString());
        //正确读取,在decode()方法之前先使用flip()方法
        buffer1.flip();
        System.out.println("正确的buffer1:" + StandardCharsets.UTF_8.decode(buffer1).toString());
        System.out.println("buffer2:" + StandardCharsets.UTF_8.decode(buffer2).toString());
        System.out.println("buffer3:" + StandardCharsets.UTF_8.decode(buffer3).toString());
    }

    /**
     * 黏包，半包的解析
     */
    @Test
    public void fun9() {
        /*
         网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
         但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
             Hello,world\n
             I'm zhangsan\n
             How are you?\n
         变成了下面的两个 byteBuffer (黏包，半包)
             Hello,world\nI'm zhangsan\nHo
             w are you?\n
         现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据
         */
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you?\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                ByteBufferUtil.debugAll(target);
            }
        }
        source.compact();
    }
}
