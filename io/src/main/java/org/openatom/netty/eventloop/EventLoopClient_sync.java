package org.openatom.netty.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 使用EventLoop处理 IO事件 :Client端
 *  演示sync()方法的作用
 */
@Slf4j
public class EventLoopClient_sync {
    public static void main(String[] args) throws InterruptedException {
        // 1. 启动类,注意: 带有ChannelFuture和Promise的都需要和sync()方法配合使用
        ChannelFuture channelFuture = new Bootstrap()
                // 2. 添加 EventLoop
                .group(new NioEventLoopGroup())
                // 3. 选择客户端 channel 实现
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 在连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5. 连接到服务器,注意:真正执行connect()的是nio线程
                .connect(new InetSocketAddress("127.0.0.1", 8002));
        //阻塞方法.直到连接建立,如果不调用,作用是阻塞住当前线程,直到nio线程建立连接成功
        //channelFuture.sync();
            //代表连接对象
        Channel channel = channelFuture.channel();
        // 6. 向服务器发送数据
        log.debug("{}", channel);
        channel.writeAndFlush("hello, world");
    }
}
