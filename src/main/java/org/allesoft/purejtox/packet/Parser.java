package org.allesoft.purejtox.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wieker on 12/19/15.
 */
public class Parser {
    byte[] packet;
    List<Integer> fields = new ArrayList<Integer>();
    byte[][] bytes;
    int length = 0;

    public Parser(byte[] packet) {
        this.packet = packet;
    }

    public Parser field(int length) {
        fields.add(length);
        this.length += length;
        return this;
    }

    public Parser last() {
        fields.add(packet.length - length);
        length = packet.length;
        return this;
    }

    public Parser parse() {
        int i = 0;
        bytes = new byte[fields.size()][];
        int pos = 0;
        for (Integer length : fields) {
            bytes[i] = new byte[length];
            System.arraycopy(packet, pos, bytes[i], 0, length);
            pos += length;
            i ++;
        }
        return this;
    }

    public byte[] getField(int index) {
        return bytes[index];
    }
}
