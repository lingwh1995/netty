package org.openatom.netty.future_promise;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * JDK中Promise演示
 *   线程之间传数据,Promise可以作为容器,在Future的基础上增加了一些方法,是增强版的Future
 */
@Slf4j
public class NettyPromiseTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.创建EventLoopGroup
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        //2.获取EventLoop
        EventLoop eventLoop = eventLoopGroup.next();
        //3.主动创建存放结果的容器
        DefaultPromise<Integer> promise = new DefaultPromise<Integer>(eventLoop);

        //4.任意一个线程执行计算,计算完成后向Promise填充结果
        new Thread(() -> {
            log.debug("开始运算......");
            try {
                //将正常执行返回的结果传递给其他线程
                promise.setSuccess(80);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //将正异常执行产生的异常传递给其他线程
                promise.setFailure(e);
                e.printStackTrace();
            }
        }).start();

        //4.接收结果
        //Netty中Promise
        //同步等待并获取结果
//        log.debug("主线程等待结果......");
//        log.debug("结果是" + promise.get());

        //异步获取结果
        promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                //获取执行结果,不阻塞。我们都知道 java.util.concurrent.Future中的get()是阻塞的
                log.debug("结果是" + future.getNow());
                //阻塞获取任务执行结果
                log.debug("结果是" + future.get());
            }
        });
    }
}
