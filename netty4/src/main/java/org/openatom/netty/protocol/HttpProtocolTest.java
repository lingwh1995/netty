package org.openatom.netty.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *  netty协议设计与解析: Http协议编解码
 *  测试:(在浏览器地址栏输入)
 *      不包含参数:
 *          http://localhost:8001/index.html
 *      包含参数:
 *          http://localhost:8001/index.html?name=xx
 */
@Slf4j
public class HttpProtocolTest {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new HttpServerCodec());
                    //对上面编解码结果进行处理,关心请求头+请求体
//                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            log.debug("{}", msg.getClass());
//                            //请求行,请求头
//                            if(msg instanceof HttpRequest) {
//                                log.debug("请求行,请求头......");
//                            //请求体
//                            }else if(msg instanceof HttpContent) {
//                                log.debug("请求体......");
//                            }
//                        }
//                    });

                    /**
                     * 使用HttpRequest解析带有参数或者不带有参数的http请求
                     */
                    //只关心请求头或者只关心请求体
                    //对上面编解码结果进行处理
//                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
//                        @Override
//                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
//                            log.debug("{}", httpRequest.uri());
//                            //解析参数
////                            QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
////                            List<String> params = decoder.parameters().get("name");
////                            log.debug(params.get(0));
//                            //返回响应
//                            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
//                                    httpRequest.protocolVersion(),HttpResponseStatus.OK);
//                            byte[] bytes = ("<h1>hello world!</h1>").getBytes();
//                            httpResponse.content().writeBytes(bytes);
//                            //写入请求头
//                            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH,bytes.length);
//                            //写出响应
//                            channelHandlerContext.writeAndFlush(httpResponse);
//                        }
//                    });

                    /**
                     * 使用DefaultHttpRequest解析解析带有参数或者不带有参数的http请求
                     */
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<DefaultHttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {
                            log.debug("{}", msg.uri());
                            QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
                            List<String> name = decoder.parameters().get("name");
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            byte[] bytes = ("<h1>hello!" + name.get(0) + "</h1>").getBytes();
                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
                            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                            response.content().writeBytes(bytes);
                            ctx.writeAndFlush(response);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8001).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
