package org.allesoft.purejtox.modules.dht;

import java.util.Arrays;

/**
 * Created by wieker on 1/7/16.
 */
class PingHandler implements DHTNetworkHandler {

    private DHTImpl dht;

    public PingHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(DHTNodeInfo dhtNode, byte[] plain_text) throws Exception {
        //System.out.printf("Ping parsing\n");
        //System.out.println("Payload: " + NaCl.asHex(plain_text));

        dht.pong(dhtNode.ipPort, dhtNode.publicKey, Arrays.copyOfRange(plain_text, 1, 9));
    }
}
