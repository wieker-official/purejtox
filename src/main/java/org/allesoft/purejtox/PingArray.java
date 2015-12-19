package org.allesoft.purejtox;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by wieker on 12/19/15.
 */
public class PingArray {
    Set<byte[]> keys = new HashSet<byte[]>();

    public byte[] add(byte[] key, IPPort ipPort) {
        keys.add(key);
        SecureRandom random = new SecureRandom();
        byte[] pingId = new byte[8];
        random.nextBytes(pingId);
        pingId[7] = (byte) keys.size();
        return pingId;
    }
}
