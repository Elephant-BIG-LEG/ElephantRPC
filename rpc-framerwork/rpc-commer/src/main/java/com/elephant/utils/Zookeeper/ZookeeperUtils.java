package com.elephant.utils.Zookeeper;

import com.elephant.Constants;
import com.elephant.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/16/10:32
 * @Description: TODO
 */
@Slf4j
public class ZookeeperUtils {
    public static ZooKeeper createZookeeper(){
        String connectString = Constants.DEFAULT_CONNECTSTRING;

        int timeout = Constants.DEFAULT_TIMEOUT;

        return createZk(connectString,timeout,null);
    }

    /**
     * 创建一个Zookeeper客户端实例
     * @param connectString
     * @param timeout
     * @param watcher
     * @return
     */
    private static ZooKeeper createZk(String connectString, int timeout, Watcher watcher) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            final ZooKeeper zooKeeper = new ZooKeeper(connectString,timeout,null);
            if(zooKeeper != null){
                log.info("创建节点成功");
                countDownLatch.countDown();
            }
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个节点
     * @param zooKeeper
     * @param node
     * @param watcher
     * @param createMode
     * @return true 成功创建 false 节点已存在 exception 创建异常
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher,CreateMode createMode){
        if(zooKeeper == null){
            throw new RuntimeException("该服务实例不存在，请重试");
        }
        try {
            if(zooKeeper.exists(node.getNodePath(),watcher) == null){
                zooKeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("创建节点：【{}】成功",node.getNodePath());
                return true;
            }else {
                log.info("该节点:【{}】已存在，不用再次创建",node.getNodePath());
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建节点:{}异常",node.getNodePath());
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断节点是否存在
     * @param zk zk实例
     * @param node  节点路径
     * @param watcher watcher
     * @return ture 存在 | false 不存在
     */
    public static boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            if(zk.exists(node,watcher) != null){
                log.info("该节点已存在：【{}】",node);
                return true;
            }else{
                log.info("该节点不存在：【{}】",node);
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在是发生异常",node,e);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 关闭 Zookeeper 服务实例
     * @param zooKeeper
     * @return true 成功关闭 exception 关闭异常
     */
    public static boolean close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询一个节点的子元素
     * @param zooKeeper zk实例
     * @param serviceNode 服务节点
     * @return 子元素列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher){
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
