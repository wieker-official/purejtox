package org.allesoft.purejtox;

/**
 * Created by wieker on 12/19/15.
 */
public interface NetworkHandler {
    public void handle(IPPort senderIPPort, byte[] data) throws Exception;
}
