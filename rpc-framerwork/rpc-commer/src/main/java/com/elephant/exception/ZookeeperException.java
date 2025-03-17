package com.elephant.exception;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/17/11:41
 * @Description: TODO
 */
public class ZookeeperException extends RuntimeException{

    public ZookeeperException() {
        super();
    }

    public ZookeeperException(String message) {
        super(message);
    }

    public ZookeeperException(Throwable cause) {
        super(cause);
    }
}
