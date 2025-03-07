package com.elephant.discovery.impl;

import com.elephant.Constant;
import com.elephant.ServiceConfig;
import com.elephant.discovery.AbstractRegistry;
import com.elephant.utils.NetUtils;
import com.elephant.utils.Zookeeper.ZookeeperNote;
import com.elephant.utils.Zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/21:42
 * @Description: TODO
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();
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
            ZookeeperNote zookeeperNote = new ZookeeperNote(parentNode,null);
            //是临时节点
            ZookeeperUtil.createNode(zookeeper, zookeeperNote,
                    null, CreateMode.EPHEMERAL);
            log.info("生成临时节点:{}");
        }
        if(log.isDebugEnabled()){
            log.debug("服务{}，已被注册",service.getInterface().getName());
        }
    }
}
