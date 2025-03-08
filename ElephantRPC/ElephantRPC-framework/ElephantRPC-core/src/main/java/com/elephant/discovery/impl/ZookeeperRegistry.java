package com.elephant.discovery.impl;

import com.elephant.Constant;
import com.elephant.ServiceConfig;
import com.elephant.discovery.AbstractRegistry;
import com.elephant.exception.DiscoveryException;
import com.elephant.exception.NetworkException;
import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNote;
import com.elephant.utils.Zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import java.net.InetSocketAddress;
import java.util.List;
/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/21:42
 * @Description: TODO
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    //保证只有一个 ZK 实例
    private ZooKeeper zookeeper;

    public ZookeeperRegistry() {
        this.zookeeper = ZookeeperUtil.createZookeeper();
    }

    public ZookeeperRegistry(String connectString,int timeout) {
        this.zookeeper = ZookeeperUtil.createZookeeper(connectString,timeout);
    }

    @Override
    public void registry(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();

        //这个节点应该是一个持久节点
        //如果为空进行创建
        if (!ZookeeperUtil.exists(zookeeper,parentNode,null)){
            ZookeeperNote zookeeperNote = new ZookeeperNote(parentNode,null);
            //持久节点
            ZookeeperUtil.createNode(zookeeper,zookeeperNote,null, CreateMode.PERSISTENT);
        }

        //创建 ** 本机 ** 的临时节点 格式: ip:port
        //服务提供方的端口，所以我们需要一个获取 ip 的方法
        //这个 IP 通常是需要一个局域网 ip
        //TODO 后续处理端口的问题
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8088;
        //判断该节点在不在
        if (!ZookeeperUtil.exists(zookeeper,node,null)){
            ZookeeperNote zookeeperNote = new ZookeeperNote(node,null);
            //是临时节点
            ZookeeperUtil.createNode(zookeeper, zookeeperNote,
                    null, CreateMode.EPHEMERAL);
            log.info("生成临时节点:{}");
        }
        if(log.isDebugEnabled()){
            log.debug("服务{}，已被注册",service.getInterface().getName());
        }
    }

    @Override
    public InetSocketAddress searchService(String name) {
        //拼接节点路径
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + name;
        //获取子节点列表
        List<String> childrenNodes = ZookeeperUtil.getChildrenNodes(zookeeper,parentNode,null);
        //拿到可用主机的列表
        List<InetSocketAddress> collect = childrenNodes.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();
        if(collect.size() == 0){
            throw new DiscoveryException("**** Not founded every service host!!!");
        }

        //TODO : 每次调用相关方法都需要去注册中心拉去列表吗？ 本地缓存 + watcher机制
        //        如何合理选择一个服务，而不是只获取第一个？ 负载均衡
        return collect.get(0);
    }
}
