package com.elephant.loadbalancer.impl;

import com.elephant.YrpcBootstrap;
import com.elephant.loadbalancer.AbstractLoadBalancer;
import com.elephant.loadbalancer.Selector;
import com.elephant.transport.message.YrpcRequest;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/24/16:08
 * @Description: 一致性 Hash 算法
 * @Function：
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {

    /**
     * 获取一个负载均衡器
     *
     * @param serviceList 服务列表
     * @return
     */
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentHashSelector implements Selector {

        // hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            // 我们应该尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                // 建立 Hash 环：需要把每一个节点加入到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        /**
         * 获取一个可用服务节点
         *
         * @return 服务节点
         */
        @Override
        public InetSocketAddress getNext() {
            // 1、hash环已经建立好了，接下来需要对请求的要素做处理我们应该选择什么要素来进行hash运算
            // 有没有办法可以获取，到具体的请求内容  --> threadLocal
            YrpcRequest yrpcRequest = YrpcBootstrap.REQUEST_THREAD_LOCAL.get();

            // 根据请求的一些特征来选择服务器  id【这里的 id 有可能会出现连续,所以后续不使用 hash 算法】
            String requestId = Long.toString(yrpcRequest.getRequestId());

            // 请求的 id 做 hash，字符串默认的 hash 不太好【因为字符串的 Hash 保存的是地址并且是连续的】
            int hash = hash(requestId);

            // 判断该hash值是否能直接落在一个服务器上，和服务器的hash一样
            // 找到的是虚拟服务节点
            if (!circle.containsKey(hash)) {
                /**
                 * tailMap【红黑树】：返回一个视图，其中包含所有键大于或等于指定键的键值对
                 */
                // 寻找离我最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            log.info("使用一致性hash的负载均衡策略");
            return circle.get(hash);
        }

        /**
         * Hash 环：将每个服务节点挂载到 hash 环上
         *
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            int positiveNum = 0,negativeNum = 0;
            for (int i = 0; i < virtualNodes; i++) {
                // 不能将 InetSocketAddress 直接作为key，因为 InetSocketAddress 可能是连续的
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                if(hash > 0){
                    positiveNum++;
                }else{
                    negativeNum++;
                }
                // 关在到hash环上
                circle.put(hash, inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("hash 值为：【{}】的节点已经挂载到了哈希环上.", hash);
                }
            }
            if(log.isDebugEnabled()){
                log.debug("分布在正数范围的数量：【{}】",positiveNum);
                log.debug("分布在负数范围的数量：【{}】",negativeNum);
            }
        }

        /**
         * 移除节点
         *
         * @param inetSocketAddress 服务地址
         */
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.remove(hash);
            }
        }

        /**
         * 具体的 hash 算法,使用 MD5 进行处理 TODO 算出的 hash 值都是大数字
         *
         * @param s 字符串
         * @return 摘取前四个字节的 hash 值
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());

            // md5得到的结果是一个字节数组，但是我们想要int 4个字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                //左移 8 位，为下一个字节让出位置【一个字节是 8 位】
                res = res << 8;
                if (digest[i] < 0) {
                    // 处理负数
                    // 按位或插入
                    res = res | (digest[i] & 255);
                } else {
                    // 处理非负数
                    // 按位或插入
                    res = res | digest[i];
                }
            }
            return res;
        }

        /**
         * 将整数 i 转成 32 位的二进制数
         *
         * @param i 整数
         * @return 二进制字符串
         */
        private String toBinary(int i) {
            String s = Integer.toBinaryString(i);
            int index = 32 - s.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(s);
            return sb.toString();
        }
    }

}
