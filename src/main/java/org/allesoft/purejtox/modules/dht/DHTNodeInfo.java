package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.IPPort;

/**
 * Created by wieker on 1/7/16.
 */
class DHTNodeInfo {
    IPPort ipPort;
    byte[] publicKey;
    long timestamp;

    int pingReceived = 0;
    int pongReceived = 0;
    int getReceived = 0;
    int sendReceived = 0;

    long pingTimestamp;
    long pongTimestamp;
    long getTimestamp;
    long sendTimestamp;

    int pingSent = 0;
    int pongSent = 0;
    int getSent = 0;
    int sendSent = 0;

    boolean received = false;

    public DHTNodeInfo() {

    }
}
