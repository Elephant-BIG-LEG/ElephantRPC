package com.elephant.protection;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/02/10:47
 * @Description: TODO
 */
public interface RateLimiter {

    /**
     * 是否允许新的请求进入
     * @return true 可以进入  false  拦截
     */
    boolean allowRequest();
}
