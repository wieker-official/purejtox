package org.allesoft.purejtox.modules.dht;

/**
 * Created by wieker on 1/7/16.
 */
public interface DHTNetworkHandler {
    public void handle(DHTNodeInfo node, byte[] plain_text) throws Exception;
}
