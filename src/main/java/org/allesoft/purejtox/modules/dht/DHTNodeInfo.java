package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.IPPort;

/**
 * Created by wieker on 1/7/16.
 */
class DHTNodeInfo {
    IPPort ipPort;
    byte[] publicKey;
    long timestamp;

    public DHTNodeInfo(IPPort ipPort, byte[] publicKey) {
        this.ipPort = ipPort;
        this.publicKey = publicKey;
    }

    public DHTNodeInfo() {

    }
}