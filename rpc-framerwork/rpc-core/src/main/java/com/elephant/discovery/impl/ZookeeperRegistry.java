package com.elephant.discovery.impl;

import com.elephant.Constants;
import com.elephant.ServiceConfig;
import com.elephant.YrpcBootstrap;
import com.elephant.discovery.AbstractRegistry;
import com.elephant.exception.DiscoveryException;
import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNode;
import com.elephant.utils.Zookeeper.ZookeeperUtils;
import com.elephant.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/17/16:23
 * @Description: TODO
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    //全局维护一个 Zookeeper 实例
    private ZooKeeper zookeeper;

    /**
     * 注册服务
     */
    public ZookeeperRegistry(String connectString,String host,boolean isDefault) {
        if(isDefault){
            this.zookeeper = ZookeeperUtils.createZookeeper();
        }else{
            this.zookeeper = ZookeeperUtils.createZookeeper();
        }
    }
    @Override
    public void register(ServiceConfig<?> service) {
        String parentNode = Constants.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        //发布父节点【持久节点】
        if (!ZookeeperUtils.exists(zookeeper, parentNode,new UpAndDownWatcher())) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            Boolean node = ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.PERSISTENT);
            if (node) {
                log.info("成功发布持久节点服务：【{}】", service.getInterface().getName());
            }
        }
        //创建本机临时节点
        //发布服务提供方子节点【临时节点】
        // TODO 动态修改端口节点
        String nodePath = parentNode + "/" + NetUtils.getIp() + ":" + YrpcBootstrap.getInstance().configuration.getPort();
        if (!ZookeeperUtils.exists(zookeeper, nodePath, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(nodePath, null);
            Boolean node = ZookeeperUtils.createNode(zookeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
            if (node) {
                log.info("成功发布临时节点服务：【{}】", service.getInterface().getName());
            }
        }
    }

    /**
     * 返回相关的服务地址
     * @param serviceName 服务的名称
     * @param group
     * @return IP + PORT 的服务集合
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        String path = Constants.BASE_PROVIDERS_PATH + "/" + serviceName;
        List<String> children = ZookeeperUtils.getChildren(zookeeper, path, null);
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString ->{
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip,port);
        }).collect(Collectors.toList());

        if(inetSocketAddresses == null){
            throw new DiscoveryException("未找到相关服务");
        }
        return inetSocketAddresses;
    }



}
