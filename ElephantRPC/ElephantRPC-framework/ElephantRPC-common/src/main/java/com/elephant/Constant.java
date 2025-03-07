package com.elephant;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/13:35
 * @Description: TODO
 */
public class Constant {
    //默认 zookeeper 连接地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";
    //默认 zookeeper 连接超时时间
    public static final int TIME_OUT = 1000;

    //服务提供方和调用方
    public static final String BASE_PROVIDER_PATH = "/elephantRPC_metadata/providers";
    public static final String BASE_CONSUMER_PATH = "/elephantRPC-metadata/consumers";
}
