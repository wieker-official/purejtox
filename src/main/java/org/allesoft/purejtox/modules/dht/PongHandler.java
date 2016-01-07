package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.NetworkHandler;

import java.util.Arrays;

/**
 * Created by wieker on 1/7/16.
 */
class PongHandler implements NetworkHandler {

    private DHTImpl dht;

    public PongHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(IPPort senderIPPort, byte[] data) throws Exception {
        System.out.printf("Pong parsing\n");
        byte[] plain_text = dht.getDhtPacketAdapter().decryptCrypto(data);
        System.out.println("Payload: " + NaCl.asHex(plain_text));

        dht.add(senderIPPort, Arrays.copyOf(dht.getDhtPacketAdapter().getLastPeerPublicKey(), Const.crypto_box_PUBLICKEYBYTES));
    }
}
