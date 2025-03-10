package com.elephant.exception;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/14:20
 * @Description: 捕获 Zookeeper 运行时的异常
 */
public class ZookeeperException extends RuntimeException{

    public ZookeeperException(Throwable cause) {
        super(cause);
    }

    public ZookeeperException() {
    }
}
