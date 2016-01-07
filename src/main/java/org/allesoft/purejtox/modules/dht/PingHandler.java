package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.NetworkHandler;

import java.util.Arrays;

/**
 * Created by wieker on 1/7/16.
 */
class PingHandler implements NetworkHandler {

    private DHTImpl dht;

    public PingHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(IPPort senderIPPort, byte[] data) throws Exception {
        //System.out.printf("Ping parsing\n");
        byte[] plain_text = dht.getDhtPacketAdapter().decryptCrypto(data);
        //System.out.println("Payload: " + NaCl.asHex(plain_text));

        dht.add(senderIPPort, Arrays.copyOf(dht.getDhtPacketAdapter().getLastPeerPublicKey(), Const.crypto_box_PUBLICKEYBYTES));
        dht.pong(senderIPPort, dht.getDhtPacketAdapter().getLastPeerPublicKey(), Arrays.copyOfRange(plain_text, 1, 9));
    }
}
