package com.elephant.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/03/17:02
 * @Description: TODO
 */
public class ShutDownHolder {
    // 用来标识请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 用来请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
