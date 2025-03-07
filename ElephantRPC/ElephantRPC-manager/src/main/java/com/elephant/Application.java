package com.elephant;

import com.elephant.utils.Zookeeper.ZookeeperNote;
import com.elephant.utils.Zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import java.util.List;

import static com.elephant.utils.Zookeeper.ZookeeperUtil.createNode;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/13:27
 * @Description: 创建基础目录
 */
@Slf4j
public class Application {

    public static void main(String[] args) {
        //默认的 watch()
        //创建 zookeeper 实例
        ZooKeeper zooKeeper = ZookeeperUtil.createZookeeper();
        //等待客户端连接成功

        //定义节点和数据
        String basePath = "/elephantRPC-metadata";
        String providePath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";

        ZookeeperNote baseNote = new ZookeeperNote(basePath,null);
        ZookeeperNote provideNote = new ZookeeperNote(providePath,null);
        ZookeeperNote consumerNote = new ZookeeperNote(consumerPath,null);

        List.of(baseNote,provideNote,consumerNote).forEach(node ->{
            //创建节点
            ZookeeperUtil.createNode(zooKeeper,node,null,CreateMode.PERSISTENT);
        });

        ZookeeperUtil.zookeeperClose(zooKeeper);
    }
}
