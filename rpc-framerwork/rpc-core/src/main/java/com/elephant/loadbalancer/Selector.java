package com.elephant.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/23/11:09
 * @Description: TODO
 */
public interface Selector {


    /**
     * 根据相应的负载均衡器获取一个节点
     * @return 可用服务节点
     */
    InetSocketAddress getNext();

}
