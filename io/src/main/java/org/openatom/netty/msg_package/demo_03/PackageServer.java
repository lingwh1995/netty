package org.openatom.netty.msg_package.demo_03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 演示Netty使用FixedLengthFrameDecoder(定长消息解码器)解决黏包和半包问题 Server端
 *      原理是每次不管接收多少数据,都会把数据拼接成完整的一帧数据然后再进行处理
 * 特别注意:处理黏包和半包的处理器一定要放在第一个,因为要在最开始就把数据解析为一帧,然后才能进行后续处理
 *      ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
 * 缺点: 浪费空间,长度不足一帧的数据,也会被当成一帧数据处理
 */
public class PackageServer {
    static final Logger log = LoggerFactory.getLogger(PackageServer.class);
    void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            //测试半包放开下面一行注释,测试黏包注释下面一行代码
            //设置Server端滑动窗口大小(接收缓冲区大小)
            //serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);

            //调整netty ByteBuf大小,用于测试半包现象,因为客户端发送的每一条数据长度为17,这里ByteBuf长度只为16,所以必然发生半包现象
            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator(16,16,16));
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //这个构造参数的大小是,客户端发送的一条完整信息的最大长度,如:客户端第一次发送a,第二次发送abc,共发送两条数据,则这个值取3
                    //特别注意: 这行代码要放在第一行,放在非第一行,在这个处理器之前的处理器读取到的数据不是正确的一帧数据
                    ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
                    //调试打印出服务器端收到的数据
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    //把接收的数据组装成一个完整的帧再进行处理
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8003);
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.debug("stoped");
        }
    }

    public static void main(String[] args) {
        new PackageServer().start();
    }
}
