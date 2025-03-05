package com.elephant;

import com.elephant.netty.MyWatch;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/05/16:08
 * @Description: TODO
 */
public class ZookeeperTest {

    ZooKeeper zooKeeper;

    //TODO
    CountDownLatch countDownLatch = new CountDownLatch(1);
    public void createZK(){
        String connectString = "192.168.200.130:2181,192.168.200.129:2181,192.168.200.128:2181";
        int timeOut = 10000;
        try {
            //默认的 watch()
            zooKeeper = new ZooKeeper(connectString, timeOut, event -> {
                //只有连接成功才放行
                if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateZK(){
        createZK();
        try {
            //等待客户端连接成功
            countDownLatch.await();
            String result = zooKeeper.create("/ElephantRPC", "hello".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("result: " + result);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if(zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testDelZK(){
        createZK();
        try {
            //等待客户端连接成功
            countDownLatch.await();
            zooKeeper.delete("/ElephantRPC",-1);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if(zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testWatch(){
        createZK();
        try {
            //等待客户端连接成功
            countDownLatch.await();
            //以下方法可以注册 watcher,可以是使用 true 来生成默认的 watcher
            Stat stat = zooKeeper.exists("/ElephantRPC", true);
//            zooKeeper.getChildren();
//            zooKeeper.getData();

            while (true){
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }finally {
            if(zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
