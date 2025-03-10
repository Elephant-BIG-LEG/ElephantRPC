package com.elephant.utils.Zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/07/14:27
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNote {
    private String nodePath;
    private byte[] data;
}
