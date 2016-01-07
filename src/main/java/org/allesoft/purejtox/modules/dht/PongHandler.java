package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;

/**
 * Created by wieker on 1/7/16.
 */
class PongHandler implements DHTNetworkHandler {

    private DHTImpl dht;

    public PongHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(DHTNodeInfo dhtNode, byte[] plain_text) throws Exception {
        System.out.printf("Pong parsing\n");
        System.out.println("Payload: " + NaCl.asHex(plain_text));

        dhtNode.pongReceived ++;
        dhtNode.pongTimestamp = System.currentTimeMillis();
    }
}
