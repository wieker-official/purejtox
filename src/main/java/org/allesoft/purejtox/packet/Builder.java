package org.allesoft.purejtox.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wieker on 12/19/15.
 */
public class Builder {
    byte[] packet;
    List<byte[]> fields = new ArrayList<byte[]>();

    public Builder field(byte[] field) {
        fields.add(field);
        return this;
    }

    public byte[] build() {
        int pos = 0;
        for (byte[] field : fields) {
            pos += field.length;
        }
        packet = new byte[pos];
        pos = 0;
        for (byte[] field : fields) {
            System.arraycopy(field, 0, packet, pos, field.length);
            pos += field.length;
        }
        return packet;
    }
}
