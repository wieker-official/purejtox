package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.IPPort;

/**
 * Created by wieker on 1/7/16.
 */
class KnownDHTNode {
    IPPort ipPort;
    byte[] publicKey;
    long timestamp;

    public KnownDHTNode(IPPort ipPort, byte[] publicKey) {
        this.ipPort = ipPort;
        this.publicKey = publicKey;
    }

    public KnownDHTNode() {

    }
}
