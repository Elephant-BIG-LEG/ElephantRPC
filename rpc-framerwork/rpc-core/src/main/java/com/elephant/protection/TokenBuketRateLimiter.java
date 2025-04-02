package com.elephant.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/02/10:37
 * @Description: 服务提供方的自我保护 -- 令牌桶算法
 */
@Slf4j
public class TokenBuketRateLimiter implements RateLimiter {
    // 思考，令牌是个啥？令牌桶是个啥？
    // String,Object?  list? ,map?

    // 代表令牌的数量. token > 0 说明有令牌，能放行，放行就减一. token == 0,无令牌  阻拦
    private int tokens;

    // 限流的本质就是 令牌数
    private final int capacity;

    // 令牌桶的令牌，如果没了要怎么办？ 按照一定的速率给令牌桶加令牌,如每秒加500个，不能超过总数
    // 可以用定时任务去加--> 启动一个定时任务，每秒执行一次 tokens+500 不能超过 capacity （不好 -- 额外的性能开销、时间不准确、）
    // 按照上一次请求令牌的时间与当前时间来计算补充的令牌数量
    private final int rate;

    // 上一次放令牌的时间
    private Long lastTokenTime;

    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    /**
     * 判断请求是否可以放行
     *
     * @return true 放行  false  拦截
     */
    public synchronized boolean allowRequest() {
        log.info("令牌桶算法正在判断是否放行");
        // 1、给令牌桶添加令牌
        // 计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        // 如果间隔时间超过一秒，放令牌
        if (timeInterval >= 1000 / rate) {
            int needAddTokens = (int) (timeInterval * rate / 1000);
            System.out.println("needAddTokens = " + needAddTokens);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            System.out.println("tokens = " + tokens);

            // 标记最后一个放入令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }

        // 2、自己获取令牌,如果令牌桶中有令牌则放行，否则拦截
        if (tokens > 0) {
            tokens--;
            System.out.println("请求被放行---------------");
            return true;
        } else {
            System.out.println("请求被拦截---------------");
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBuketRateLimiter rateLimiter = new TokenBuketRateLimiter(10, 10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}