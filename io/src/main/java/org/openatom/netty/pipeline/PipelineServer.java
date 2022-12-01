package org.openatom.netty.pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PipelineServer {
    public static void main(String[] args) {
        new ServerBootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    //1.通过channel获取pipeline
                    ChannelPipeline pipeline = nioSocketChannel.pipeline();
                    //2.添加处理器 head(netty自带)->h1->h2->h3->h4->h5->h6->tail(netty自带)
                    //入站: head->tail(h1->h2->h3)
                    //出站: tail->head(h6->h5->h4)
                    pipeline.addLast("h1", new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("1");
                            super.channelRead(ctx, msg);
                        }
                    });
                    pipeline.addLast("h2", new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("2");
                            super.channelRead(ctx, msg);
                        }
                    });
                    pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                            log.debug("4");
                            super.write(ctx, msg, promise);
                        }
                    });
                    pipeline.addLast("h3", new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("3");
                            //super.channelRead(ctx, msg);
                            //nioSocketChannel.writeAndFlush()从链表尾部从后往前找出站的handler
//                            nioSocketChannel.writeAndFlush(
//                                    nioSocketChannel.alloc().buffer().writeBytes("server......".getBytes()));
                            //ctx.writeAndFlush()从当前位置从后往前找出站的handler
                            ctx.writeAndFlush(
                                    nioSocketChannel.alloc().buffer().writeBytes("server......".getBytes()));
                        }
                    });
                    pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                            log.debug("5");
                            super.write(ctx, msg, promise);
                        }
                    });
                    pipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                            log.debug("6");
                            super.write(ctx, msg, promise);
                        }
                    });
                }
            }).bind(8001);
    }
}
