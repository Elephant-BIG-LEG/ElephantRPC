package com.elephant.impl;

import com.elephant.HelloYrpc;
import com.elephant.HelloYrpc2;
import com.elephant.annotation.ElephantAPI;
import com.elephant.annotation.YrpcAPi;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/15/16:16
 * @Description: 服务提供方接口的具体实现
 */
@YrpcAPi(group = "default")
@ElephantAPI
public class HelloYrpcImpl2 implements HelloYrpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg + "Is is default";
    }
}
