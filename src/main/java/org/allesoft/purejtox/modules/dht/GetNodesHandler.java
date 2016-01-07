package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.NetworkHandler;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 1/7/16.
 */
class GetNodesHandler implements NetworkHandler {

    private DHTImpl dht;

    public GetNodesHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(IPPort senderIPPort, byte[] data) throws Exception {
        System.out.printf("getnodes parsing\n");
        byte[] plain_text = dht.getDhtPacketAdapter().decryptCrypto(data);

        Parser general = new Parser(plain_text)
                .field(Const.crypto_box_PUBLICKEYBYTES)
                .field(8)
                .parse();

        System.out.println("Peer key: " + NaCl.asHex(general.getField(0)));
        System.out.println("Ping id: " + NaCl.asHex(general.getField(1)));

        dht.sendnodes(senderIPPort, general.getField(0), general.getField(1));
    }
}
