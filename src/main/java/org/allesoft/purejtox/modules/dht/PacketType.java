package org.allesoft.purejtox.modules.dht;

/**
 * Created by wieker on 1/6/16.
 */
public enum PacketType {
    PING_REQUEST(0),
    PING_RESPONSE(1),
    GET_NODES(2),
    SEND_NODES(4);

    byte code;

    private PacketType(Integer code) {
        this.code = (byte) (int) code;
    }

    public byte getCode() {
        return code;
    }
}
