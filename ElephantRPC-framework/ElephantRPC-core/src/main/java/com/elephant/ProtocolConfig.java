package com.elephant;

import lombok.Data;

/**
 * @Author: Elephant-FZY
 * @Email: https://github.com/Elephant-BIG-LEG
 * @Date: 2025/03/06/21:38
 * @Description: TODO
 */
@Data
public class ProtocolConfig {

    private String protocolName;

    public ProtocolConfig(String protocolName) {
        this.protocolName = protocolName;
    }
}
