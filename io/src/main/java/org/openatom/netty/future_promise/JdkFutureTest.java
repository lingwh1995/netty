package org.openatom.netty.future_promise;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * JDK中Future演示
 */
@Slf4j
public class JdkFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //2.提交任务
        // 注意: 1.submit()可以接收返回值,execute()不能接收返回值
        //       2.Cable接口中call()方法有返回值,Runnable()中run()方法没有返回值
            //Future是在线程间传递运算结果的容器
        Future<Integer> future = pool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("线程池中的线程执行运算......");
                Thread.sleep(1000);
                return 50;
            }
        });
        log.debug("主线程等待结果......");
        //java.util.concurrent.Future中的get()是阻塞的
        log.debug("结果是" + future.get());
    }
}
