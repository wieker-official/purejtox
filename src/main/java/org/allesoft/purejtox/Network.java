package org.allesoft.purejtox;

/**
 * Created by wieker on 12/28/15.
 */
public interface Network {
    void send(IPPort ipPort, byte[] packet) throws Exception;

    void poll() throws Exception;

    void registerHandler(byte code, NetworkHandler handler);
}
