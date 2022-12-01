package org.openatom.netty.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 使用EventLoop处理 IO事件 :Server端 2.0版本
 *      在netty中,一旦建立连接,channel就会和一个EventLoop绑定,在原生nio中,channel是和selector相绑定的
 */
@Slf4j
public class EventLoopServer_2 {
    public static void main(String[] args) {
        //细分1: 创建一个独立的EventLoopGroup来处理耗时较长的连接
        EventLoopGroup group = new DefaultEventLoopGroup();
        new ServerBootstrap()
            /**
             * 细分2
              */
            //对group()方法参数进行细分,将之前使用一个EventLoopGroup改为使用两个EventLoopGroup
            //第一个NioEventLoopGroup: boss,只负责ServerSocketChannel上的 accept事件
            //第二个NioEventLoopGroup: worker,只负责SocketChannel上的 读写事件,NioEventLoop数量根据自己实际需求确定
            .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf byteBuf = (ByteBuf) msg;
                            log.debug(byteBuf.toString(Charset.defaultCharset()) + "  handler1...");
                            //把消息传递给下一个handler
                            ctx.fireChannelRead(msg);
                        }
                    }).addLast(group,"handler2",new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf byteBuf = (ByteBuf) msg;
                            log.debug(byteBuf.toString(Charset.defaultCharset())  + "  handler2...");
                        }
                    });
                }
            }).bind(8002);
    }
}
