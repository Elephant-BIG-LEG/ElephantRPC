package com.elephant.protection;

import lombok.extern.slf4j.Slf4j;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/02/10:47
 * @Description: 服务调用端的自我保护 -- 熔断器
 */
@Slf4j
public class CircuitBreaker {
    // TODO 添加 半开状态

    // 理论上：标准的断路器应该有三种状态  open close half_open，我们为了简单只选取两种
    private volatile boolean isOpen = false;

    // 需要搜集指标  异常的数量   比例
    // 总的请求数
    private AtomicInteger requestCount = new AtomicInteger(0);

    // 异常的请求数
    private AtomicInteger errorRequest = new AtomicInteger(0);

    // 异常的阈值
    private int maxErrorRequest;
    private float maxErrorRate;

    public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }


    // 断路器的核心方法，判断是否开启
    public boolean isBreak() {
        log.info("正在判断是否启动熔功能");
        // 优先返回，如果已经打开了，就直接返回true
        if (isOpen) {
            log.info("已经打开熔断功能了");
            return true;
        }

        // 需要判断数据指标，是否满足当前的阈值
        if (errorRequest.get() > maxErrorRequest) {
            log.info("错误请求超过阈值:{}，熔断生效", maxErrorRate);
            this.isOpen = true;
            return true;
        }

        if (errorRequest.get() > 0 && requestCount.get() > 0 &&
                errorRequest.get() / (float) requestCount.get() > maxErrorRate) {
            log.info("错误率超过{}，熔断生效", maxErrorRate);
            this.isOpen = true;
            return true;
        }

        log.info("条件未满足，不开启熔断功能");

        return false;
    }

    // 每次发生请求，获取发生异常应该进行记录
    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequest.getAndIncrement();
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);
    }


    public static void main(String[] args) {

        CircuitBreaker circuitBreaker = new CircuitBreaker(3, 1.1F);

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                // 模拟失败
                int num = new Random().nextInt(100);
                if (num > 70) {
                    circuitBreaker.recordErrorRequest();
                }

                // 判断是否开启熔断功能
                boolean aBreak = circuitBreaker.isBreak();

                String result = aBreak ? "断路器阻塞了请求" : "断路器放行了请求";

                System.out.println(result);

            }
        },"A").start();


        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("-------------------" + Thread.currentThread().getName() + "重置熔断器 ----------------------");
                circuitBreaker.reset();
            }
        },"B").start();

        try {
            // 这里要休眠，不然子线程会断开的。因为默认的线程是使用守护线程
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
