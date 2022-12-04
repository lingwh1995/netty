package org.openatom.netty.msg_package.demo_5;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 使用LengthFieldBasedFrameDecoder解码器解决黏包和半包问题
 */
@Slf4j
public class EmbeddedChannelTest {

    /**
     * 不带版本号的消息
     *      最终获取的消息: hello, world
     */
    @Test
    public void fun1() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * int maxFrameLength: 每一帧最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024,0,4,0,4),
                new LoggingHandler(LogLevel.DEBUG)
        );
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "hi");
        send(buffer, "hello, world");
        embeddedChannel.writeInbound(buffer);
    }

    /**
     * 带有版本号的消息
     *      最终获取的消息: .hello, world(前面哪个点代表版本号)
     */
    @Test
    public void fun2() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * int maxFrameLength: 每一帧数据最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024,0,4,1,4),
                new LoggingHandler(LogLevel.DEBUG)
        );
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        sendWithVersion(buffer, "hello, world");
        embeddedChannel.writeInbound(buffer);
    }

    /**
     * 发送消息的方法
     * @param buffer
     * @param msg
     */
    private static void send(ByteBuf buffer, String msg) {
        byte[] bytes = msg.getBytes();
        int length = bytes.length;
        //发送内容长度
        buffer.writeInt(length);
        //发送的内容
        buffer.writeBytes(bytes);
    }

    /**
     * 发送消息的方法
     * @param buffer
     * @param msg
     */
    private static void sendWithVersion(ByteBuf buffer, String msg) {
        byte[] bytes = msg.getBytes();
        int length = bytes.length;
        //发送内容长度
        buffer.writeInt(length);
        //发送内容版本号(一个byte长度为1,所以lengthAdjustment值为1)
        buffer.writeByte(1);
        //发送的内容
        buffer.writeBytes(bytes);
    }
}
