package com.elephant.enumeration;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/09/21:35
 * @Description: 枚举请求类型
 */
public enum RequestType {

    REQUEST((byte)1,"普通请求"), HEART_BEAT((byte)2,"心跳检测请求");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
