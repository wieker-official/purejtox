package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.IPPort;

/**
 * Created by wieker on 1/6/16.
 */
public interface DHT {
    void bootstrap(IPPort ipPort, byte[] peerPublicKey) throws Exception;

    void do_() throws Exception;

    Integer getSize();
}
