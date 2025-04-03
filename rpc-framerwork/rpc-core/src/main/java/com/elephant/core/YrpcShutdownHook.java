package com.elephant.core;

import lombok.extern.slf4j.Slf4j;


/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/03/17:01
 * @Description: 优雅停机
 */
@Slf4j
public class YrpcShutdownHook extends Thread{
    @Override
    public void run() {
        // 打开挡板
        ShutDownHolder.BAFFLE.set(true);

        // 2、等待计数器归零（正常的请求处理结束）  AtomicInteger
        // 等待归零，继续执行  countdownLatch, 最多等十秒
        long start = System.currentTimeMillis();
        while (true) {
            log.info("在等待计数器归零后，执行一些清理操作");
            try {
                // 如果这里不进行睡眠 CPU 耗时很高
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //等待系统中正在处理的请求完成，确保当前没有活跃的请求。
            if (ShutDownHolder.REQUEST_COUNTER.sum() == 0L
                    || System.currentTimeMillis() - start > 10000) {
                break;
            }
        }

        // 3、阻塞结束后，放行。执行其他操作，如释放资源
        log.info("已处理所有请求，执行资源释放或其他清理工作");

    }
}
