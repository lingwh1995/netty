package org.openatom.io.nio;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * NIO Buffer相关使用
 */
public class NIOBuffer {

    /**
     * 一个简单的NIO Buffer使用示例
     */
    @Test
    public void fun1() {
        //创建一个buffer,可以存放5个int
        IntBuffer intBuffer = IntBuffer.allocate(5);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i);
        }

        intBuffer.flip();
        //读取元素起始位置
        intBuffer.position(2);
        //读取元素结束位置
        intBuffer.limit(4);
        while (intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }
    }

    /**
     * Buffer的类型化,即使用什么数据类型put(),就使用什么数据类型get()
     */
    @Test
    public void fun2() {
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
    public void fun3() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(1);
        //将buffer设置为只读模式,如果再次写入内容,则会报下面的异常
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        //ReadOnlyBufferException
        readOnlyBuffer.putInt(2);
    }

}