package org.openatom.jdk.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 创建线程的第三种方式,实现 Callable接口,特点是:可以从FutureTask中获取线程运算结果
 */
@Slf4j
public class CallableTest {
    public static void main(String[] args) throws Exception {
        FutureTask futureTask = new FutureTask(new MyCallable());
        Thread thread = new Thread(futureTask);
        thread.start();
        log.debug("主线程等待结果......");
        System.out.println("从计算线程拿到的结果为：" + futureTask.get());

    }

}

@Slf4j
class MyCallable implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        log.debug("线程池中的线程执行运算......");
        Thread.sleep(1000);
        return 50;
    }
}