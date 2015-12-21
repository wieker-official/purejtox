package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/19/15.
 */
public class NetCrypto {

    DHT dht;

    public NetCrypto(DHT dht) {
        this.dht = dht;
    }

    public void sendCookie(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] plain = new Builder()
                .field(dht.myPublicKey)
                .field(new byte[Const.crypto_box_PUBLICKEYBYTES])
                .field(new byte[8])
                .build();

        CryptoCore nacl = dht.getEncrypter(peerPublicKey);
        nacl.encrypt(plain);

        byte[] packet = new Builder()
                .field(new byte[]{24})
                .field(dht.getPublicKey())
                .field(nacl.getNonce())
                .field(nacl.getCypherText())
                .build();

        dht.getNetwork().send(ipPort, packet);
    }
}
