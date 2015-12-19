package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;

/**
 * Created by wieker on 12/19/15.
 */
public class Ping {

    DHT dht;
    PingArray pingArray = new PingArray();

    public Ping(DHT dht) {
        this.dht = dht;
    }

    public void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] pk = new byte[Const.DHT_PING_SIZE];
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];

        CryptoCore nacl = dht.getEncrypter(peerPublicKey);

        byte[] ping_id = pingArray.add(peerPublicKey.clone());

        ping_plain[0] = 0;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);
        nacl.encrypt(ping_plain);

        pk[0] = 0;
        System.arraycopy(dht.getPublicKey(), 0, pk, 1, dht.getPublicKey().length);
        System.arraycopy(nacl.getNonce(), 0, pk, 1 + dht.getPublicKey().length, nacl.getNonce().length);
        System.arraycopy(nacl.getCypherText(), 0, pk, 1 + dht.getPublicKey().length + nacl.getNonce().length, nacl.getCypherText().length);

        dht.getNetwork().send(ipPort, pk);
    }
}
