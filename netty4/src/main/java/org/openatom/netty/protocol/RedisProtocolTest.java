package org.openatom.netty.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;

/**
 *  netty协议设计与解析: 使用Redis协议给Redis Server发送Redis操作命令
 *  netty实现Redis Client,可以向Redis Server发送命令
 *  set name zhangsan
 *     *3
 *     $3
 *     set
 *     $4
 *     name
 *     $8
 *     zhangsan
 */
public class RedisProtocolTest {
    public static void main(String[] args) {
        //回车:13 换行:10
        final byte[] LINE = {13, 10};
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf byteBuf = ctx.alloc().buffer();
                                byteBuf.writeBytes("*3".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("$3".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("set".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("$4".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("name".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("$8".getBytes());
                                byteBuf.writeBytes(LINE);
                                byteBuf.writeBytes("zhangsan".getBytes());
                                byteBuf.writeBytes(LINE);
                                ctx.writeAndFlush(byteBuf);
                            }

                            /**
                             * 接收Redis返回的结果
                             * @param ctx
                             * @param msg
                             * @throws Exception
                             */
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
