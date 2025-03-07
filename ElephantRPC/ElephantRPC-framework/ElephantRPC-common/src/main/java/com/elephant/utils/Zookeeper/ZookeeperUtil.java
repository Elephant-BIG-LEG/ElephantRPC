package com.elephant.utils.Zookeeper;

import com.elephant.Constant;
import com.elephant.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/14:15
 * @Description: 创建 Zookeeper
 */
@Slf4j
public class ZookeeperUtil {

    public static ZooKeeper createZookeeper() {
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;

        return createZookeeper(connectString, timeout);
    }

    public static ZooKeeper createZookeeper(String connectString,int timeout) {

        //保证线程安全
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            //默认的 watch()
            //创建 zookeeper 实例
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, event -> {
                //只有连接成功才放行
                if (event.getState() == org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            //等待客户端连接成功
            countDownLatch.await();
            return zooKeeper;

        } catch (InterruptedException | IOException e) {
            log.error("**** Creating the zookeeper Instance occur exception", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点
     * @param zooKeeper zookeeper 实例
     * @param node 节点
     * @param watcher watcher 实例
     * @param createMode 节点的类型
     * @return true：成功创建 or false：已经存在 or Exception：创建失败
     */
    public static Boolean createNode(ZooKeeper zooKeeper,ZookeeperNote node,Watcher watcher,CreateMode createMode){
        //创建节点
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,createMode);
                log.info("**** Creating this node:【{}】succeed!!!", result);
                return true;
            }else {
                log.info("**** This Node ：【{}】has exists！！！",node.getNodePath());
                return false;
            }
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭 Zookeeper
     * @param zookeeper
     */
    public static void zookeeperClose(ZooKeeper zookeeper){
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            log.error("Closing the zookeeper occur the exception",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     * @param zk zk 实例
     * @param node 节点路径
     * @param watcher watcher 实例
     * @return true：存在 or Exception：节点不存在
     */
    public static Boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            return zk.exists(node,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("**** This zookeeper node:【{}】 occur exception！！！",node);
            throw new ZookeeperException(e);
        }
    }
}
