package com.elephant;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/16:16
 * @Description: TODO
 */
public class HelloRPCImpl implements HelloRPC {
    @Override
    public String sayHi(String msg) {
        System.out.println(msg);
        return "收到消息";
    }
}
