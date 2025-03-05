package com.elephant.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.nio.file.WatchEvent;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/05/19:04
 * @Description: TODO
 */
public class MyWatch implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.None){
            if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                System.out.println(watchedEvent.getPath() + "连接成功");
            } else if (watchedEvent.getState() == Event.KeeperState.AuthFailed){
                System.out.println("认证失败");
            } else if (watchedEvent.getState() == Event.KeeperState.Disconnected){
                System.out.println("断开连接");
            }

        } else {
            System.out.println("其他情况");
        }
    }
}
