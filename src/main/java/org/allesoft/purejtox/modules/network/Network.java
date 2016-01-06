package org.allesoft.purejtox.modules.network;

import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.dht.PacketType;

/**
 * Created by wieker on 12/28/15.
 */
public interface Network {
    void send(IPPort ipPort, byte[] packet) throws Exception;

    void poll() throws Exception;

    void registerHandler(PacketType code, NetworkHandler handler);
}
