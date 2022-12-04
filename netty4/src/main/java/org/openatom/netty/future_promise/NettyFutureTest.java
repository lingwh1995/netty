package org.openatom.netty.future_promise;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Netty中Future演示
 *   线程之间传数据,Fulture可以作为容器
 */
@Slf4j
public class NettyFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Future<Integer> future = group.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("线程池中的线程执行运算......");
                Thread.sleep(1000);
                return 70;
            }
        });
        //Netty中Future比JDK中Future增强的地方,增加了异步支持
            //同步等待并获取结果
//        log.debug("主线程等待结果......");
//        log.debug("结果是" + future.get());

            //异步获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
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
