package org.allesoft.purejtox;

import org.allesoft.purejtox.modules.dht.DHTPacketAdapter;

/**
 * Created by wieker on 12/19/15.
 */
public class NetCrypto {

    DHTPacketAdapter DHTPacketAdapter;

    public NetCrypto(DHTPacketAdapter DHTPacketAdapter) {
        this.DHTPacketAdapter = DHTPacketAdapter;
    }

    public void sendCookie(IPPort ipPort, byte[] peerPublicKey) throws Exception {

    }
}
