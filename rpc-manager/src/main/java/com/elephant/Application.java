package com.elephant;

import com.elephant.utils.Zookeeper.ZookeeperNode;
import com.elephant.utils.Zookeeper.ZookeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/16/10:26
 * @Description: 注册中心的管理页面 --- TODO 添加管理端界面
 */
public class Application {

    public static void main(String[] args) {
        //创建服务实例
        ZooKeeper zk = ZookeeperUtils.createZookeeper();

        //TODO 后期修改成管理界面动态传参
        //定义节点数据
        String basePath = "/elephant-metadata";
        String providerPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);

        //启动相关服务
        List.of(baseNode,consumersNode,providersNode).forEach(node ->{
            ZookeeperUtils.createNode(zk,node,null, CreateMode.PERSISTENT);
        });

        //关闭连接
        ZookeeperUtils.close(zk);
    }
}
