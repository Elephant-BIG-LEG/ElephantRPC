package com.elephant.exception;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/04/02/15:35
 * @Description: TODO
 */
public class ResponseException extends RuntimeException {

    private byte code;
    private String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
