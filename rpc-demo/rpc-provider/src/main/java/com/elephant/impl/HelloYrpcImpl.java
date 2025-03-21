package com.elephant.impl;

import com.elephant.HelloYrpc;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/16:16
 * @Description: 响应给服务调用端的数据
 */
public class HelloYrpcImpl implements HelloYrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
