package org.openatom.netty.eventloop;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.TimeUnit;

/**
 * EventLoop就是netty中的线程池
 * 使用EventLoop可以处理: IO事件、处理普通任务、处理定时任务
 *   本示例中演示: 处理普通任务、处理定时任务
 */
public class EventLoopTest {

    public static void main(String[] args) {
        /**
         * 创建事件循环对象
         */
        //第一种实现: 处理IO事件、处理普通任务、处理定时任务
        EventLoopGroup group = new NioEventLoopGroup(2);
        //第二种实现: 处理普通任务、处理定时任务
        //DefaultEventLoopGroup group = new DefaultEventLoopGroup();
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());

        /**
         * 处理普通任务
         */
        group.next().submit(() -> {
            System.out.println("执行普通任务......");
        });

        /**
         * 处理定时任务
         */
        group.next().scheduleAtFixedRate(() -> {
            System.out.println("执行定时任务......");
        },0,1, TimeUnit.SECONDS);
        System.out.println("main......");

    }
}
