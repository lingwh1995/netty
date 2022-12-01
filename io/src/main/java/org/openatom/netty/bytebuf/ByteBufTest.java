package org.openatom.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import org.junit.Test;
import org.openatom.utils.ByteBufUtil;


public class ByteBufTest {

    /**
     * ByteBuf入门
     *      1.ByteBuf分配内存
     *      2.ByteBuf池化
     *          4.1以后默认池化,4.1以前技术不成熟,默认非池化
     */
    @Test
    public void fun1() {
        //默认获取的是直接内存
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        //在堆上分配内存
        //ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        ByteBufUtil.log(byteBuf);
        //PooledUnsafeDirectByteBuf:池化+直接内存
        System.out.println(byteBuf.getClass());
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<32; i++) {
            builder.append("a");
        }
        byteBuf.writeBytes(builder.toString().getBytes());
        ByteBufUtil.log(byteBuf);
    }

    /**
     * ByteBuf 的slice()和release()方法
     */
    @Test
    public void fun2() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        ByteBufUtil.log(byteBuf);

        //在切片过程中,并没有发生数据复制
        ByteBuf slice1 = byteBuf.slice(0, 5);
        ByteBuf slice2 = byteBuf.slice(5, 5);
        ByteBufUtil.log(slice1);
        ByteBufUtil.log(slice2);

        System.out.println("=================================================================");
        //setXxx(),验证: 在切片过程中,并没有发生数据复制
        slice1.setByte(0,'1');
        ByteBufUtil.log(slice1);
        ByteBufUtil.log(byteBuf);

        //release(),调用后再执行写入会报:io.netty.util.IllegalReferenceCountException: refCnt: 0
        byteBuf.release();
        byteBuf.writeByte('x');
    }

    /**
     * 零拷贝方法一:
     * ByteBuf 的duplicate()方法
     *      截取了原始ByteBuf的所有内容,与原始ByteBuf使用同一块内存,只是读写指针是独立的
     */
    @Test
    public void fun3() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        ByteBufUtil.log(byteBuf);
        ByteBuf duplicate = byteBuf.duplicate();
        ByteBufUtil.log(duplicate);
    }

    /**
     * 零拷贝方法二:
     * ByteBuf 的copy()方法
     *      对原始ByteBuf进行深拷贝,因此操作拷贝后的ByteBuf不会影响到原来的ByteBuf
     */
    @Test
    public void fun4() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        ByteBufUtil.log(byteBuf);
        ByteBuf copy = byteBuf.copy();
        ByteBufUtil.log(copy);
    }

    /**
     * 零拷贝方法三:
     * ByteBuf 的compositeBuffer()方法
     *      零拷贝将两个小的ByteBuf合并为一个大的ByteBuf
     * 优点:零拷贝
     * 缺点:带来了更复杂的维护
     */
    @Test
    public void fun5() {
        ByteBuf byteBuf1 = ByteBufAllocator.DEFAULT.buffer(5);
        byteBuf1.writeBytes(new byte[]{'a','b','c','d','e'});
        ByteBuf byteBuf2 = ByteBufAllocator.DEFAULT.buffer(5);
        byteBuf2.writeBytes(new byte[]{'f','g','h','i','j'});

        //非零拷贝合并两个ByteBuf
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(byteBuf1).writeBytes(byteBuf2);
        ByteBufUtil.log(byteBuf);

        //使用compositeBuffer()零拷贝合并两个ByteBuf
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(true,byteBuf1,byteBuf2);
        ByteBufUtil.log(compositeByteBuf);
    }
}
