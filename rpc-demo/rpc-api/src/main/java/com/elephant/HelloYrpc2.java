package com.elephant;

/**
 * @author Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * 06-27
 */
public interface HelloYrpc2 {

    /**
     * 通用接口，server和client都需要依赖
     * @param msg 发送的具体的消息
     * @return 返回的结果
     */
    String sayHi(String msg);

}
