package org.openatom.netty.protocol.message.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;
import org.openatom.netty.protocol.message.LoginRequestMessage;

/**
 * 测试MessageCodec编解码
 * 测试LengthFieldBasedFrameDecoder解码器处理黏包半包问题
 * 抽取公共的Handler
 */
public class MessageCodecEmbeddedChannelTest {

    /**
     * 使用LengthFieldBasedFrameDecoder解决黏包半包问题
     * @throws Exception
     */
    @Test
    public void fun1() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * 配置帧解码器:解决黏包半包问题
                 * int maxFrameLength: 每一帧最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(LogLevel.DEBUG),
                new MessageCodec());
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 消息出站,测试encode()方法
        embeddedChannel.writeOutbound(message);

        // 消息入站,测试decode()方法
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,byteBuf);
        embeddedChannel.writeInbound(byteBuf);
    }

    /**
     * 验证不使用LengthFieldBasedFrameDecoder无法处理黏包半包问题
     *
     * @throws Exception
     */
    @Test
    public void fun2() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * 配置帧解码器:解决黏包半包问题
                 * int maxFrameLength: 每一帧最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(LogLevel.DEBUG),
                new MessageCodec());
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 消息出站,测试encode()方法
        embeddedChannel.writeOutbound(message);

        // 消息入站,测试decode()方法
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,byteBuf);

        // 对ByteBuf进行切片
        ByteBuf slice1 = byteBuf.slice(0, 100);
        ByteBuf slice2 = byteBuf.slice(100,byteBuf.readableBytes() - 100);
        // 小坑: 调用了writeInbound()后,byteBuf的引用计数会自动减去1
        // 解决: 在调用方法之前先给slice1的引用计数+1
        slice1.retain();
        embeddedChannel.writeInbound(slice1);
        embeddedChannel.writeInbound(slice2);
    }

    /**
     * 在fun1的基础上把Handler抽取出来,可以实现Handler复用
     * 注意事项:
     *      有的Handler在多线程环境下使用会出错,如: LengthFieldBasedFrameDecoder
     *      有的Handler支持多线程环境使用,如: LoggingHandler
     * 如何判断一个Handler是否支持多线程环境下使用:
     *      点进去具体的Handler源码查看,如果类上加了@Sharable这个注解,说明这个Handler支持多线程环境下使用
     *
     * @throws Exception
     */
    @Test
    public void fun3() throws Exception {
        /**
         * 多线程使用会出问题
         * 配置帧解码器:解决黏包半包问题
         * int maxFrameLength: 每一帧最大的长度
         * int lengthFieldOffset: 偏移量
         * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
         * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
         * int initialBytesToStrip: 剥离数据的长度
         */
        LengthFieldBasedFrameDecoder FRAME_DECODER = new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0);
        // 多线程使用不会出问题
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                FRAME_DECODER,
                LOGGING_HANDLER,
                new MessageCodec());
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 消息出站,测试encode()方法
        embeddedChannel.writeOutbound(message);

        // 消息入站,测试decode()方法
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,byteBuf);

        // 对ByteBuf进行切片
        ByteBuf slice1 = byteBuf.slice(0, 100);
        ByteBuf slice2 = byteBuf.slice(100,byteBuf.readableBytes() - 100);
        // 小坑: 调用了writeInbound()后,byteBuf的引用计数会自动减去1
        // 解决: 在调用方法之前先给slice1的引用计数+1
        slice1.retain();
        embeddedChannel.writeInbound(slice1);
        embeddedChannel.writeInbound(slice2);
    }

    /**
     * 在fun3的基础上把Handler抽取出来,可以实现Handler复用 + 处理某些Handler线程不安全问题
     * 注意事项:
     *      有的Handler在多线程环境下使用会出错,如: LengthFieldBasedFrameDecoder
     *      有的Handler支持多线程环境使用,如: LoggingHandler
     * 如何判断一个Handler是否支持多线程环境下使用:
     *      点进去具体的Handler源码查看,如果类上加了@Sharable这个注解,说明这个Handler支持多线程环境下使用
     *
     * @throws Exception
     */
    @Test
    public void fun4() throws Exception {
        // 多线程使用不会出问题
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * 多线程使用会出问题
                 * 配置帧解码器:解决黏包半包问题
                 * int maxFrameLength: 每一帧最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                LOGGING_HANDLER,
                new MessageCodec());
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 消息出站,测试encode()方法
        embeddedChannel.writeOutbound(message);

        // 消息入站,测试decode()方法
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,byteBuf);

        // 对ByteBuf进行切片
        ByteBuf slice1 = byteBuf.slice(0, 100);
        ByteBuf slice2 = byteBuf.slice(100,byteBuf.readableBytes() - 100);
        // 小坑: 调用了writeInbound()后,byteBuf的引用计数会自动减去1
        // 解决: 在调用方法之前先给slice1的引用计数+1
        slice1.retain();
        embeddedChannel.writeInbound(slice1);
        embeddedChannel.writeInbound(slice2);
    }

    /**
     * 在fun4的基础上更换线程不安全的消息解码器MessageCodec为线程安全的消息解码器MessageCodecSharable
     * 注意事项:
     *      有的Handler在多线程环境下使用会出错,如: LengthFieldBasedFrameDecoder
     *      有的Handler支持多线程环境使用,如: LoggingHandler
     * 如何判断一个Handler是否支持多线程环境下使用:
     *      点进去具体的Handler源码查看,如果类上加了@Sharable这个注解,说明这个Handler支持多线程环境下使用
     *
     * @throws Exception
     */
    @Test
    public void fun5() throws Exception {
        // 多线程使用不会出问题
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        // 多线程使用不会出问题
        MessageCodecSharable MESSAG_ECODEC_SHARABLE = new MessageCodecSharable();
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                /**
                 * 多线程使用会出问题
                 * 配置帧解码器:解决黏包半包问题
                 * int maxFrameLength: 每一帧最大的长度
                 * int lengthFieldOffset: 偏移量
                 * int lengthFieldLength: 单个字符的长度(如:byte占4个字节)
                 * int lengthAdjustment: 调整值,从长度之后要跳跃几个字节才是内容
                 * int initialBytesToStrip: 剥离数据的长度
                 */
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                LOGGING_HANDLER,
                MESSAG_ECODEC_SHARABLE);
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 消息出站,测试encode()方法
        embeddedChannel.writeOutbound(message);

        // 消息入站,测试decode()方法
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,byteBuf);

        // 对ByteBuf进行切片
        ByteBuf slice1 = byteBuf.slice(0, 100);
        ByteBuf slice2 = byteBuf.slice(100,byteBuf.readableBytes() - 100);
        // 小坑: 调用了writeInbound()后,byteBuf的引用计数会自动减去1
        // 解决: 在调用方法之前先给slice1的引用计数+1
        slice1.retain();
        embeddedChannel.writeInbound(slice1);
        embeddedChannel.writeInbound(slice2);
    }

}
