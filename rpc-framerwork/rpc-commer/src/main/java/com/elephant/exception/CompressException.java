package com.elephant.exception;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/22/11:55
 * @Description: TODO
 */
public class CompressException extends RuntimeException{
    public CompressException() {
        super();
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
