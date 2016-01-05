package org.allesoft.purejtox;

import org.allesoft.purejtox.modules.dht.DHTPacketHandler;

/**
 * Created by wieker on 12/19/15.
 */
public class NetCrypto {

    org.allesoft.purejtox.modules.dht.DHTPacketHandler DHTPacketHandler;

    public NetCrypto(DHTPacketHandler DHTPacketHandler) {
        this.DHTPacketHandler = DHTPacketHandler;
    }

    public void sendCookie(IPPort ipPort, byte[] peerPublicKey) throws Exception {

    }
}
