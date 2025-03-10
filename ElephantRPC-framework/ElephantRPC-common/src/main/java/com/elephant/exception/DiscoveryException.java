package com.elephant.exception;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/08/11:24
 * @Description: 注册时发生的异常
 */
public class DiscoveryException extends RuntimeException {
    public DiscoveryException() {
        super();
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
