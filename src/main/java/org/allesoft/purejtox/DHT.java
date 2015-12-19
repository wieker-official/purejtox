package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.packet.Builder;

/**
 * Created by wieker on 12/19/15.
 */
public class DHT {
    byte[] myPublicKey = new byte[Const.SHARED_SIZE];
    byte[] myPrivateKey = new byte[Const.SHARED_SIZE];

    Network network;

    public DHT() throws Exception {
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublicKey, myPrivateKey);
        this.network = new Network();
    }

    public CryptoCore getEncrypter(byte[] peerPublicKey) throws Exception {
        return new CryptoCore(myPrivateKey, peerPublicKey);
    }

    public byte[] getPublicKey() {
        return myPublicKey;
    }

    public Network getNetwork() {
        return network;
    }

    public void getnodes(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] pingId = new byte[] { 1, 0, 1, 0, 0, 0, 0, 1, };
        byte[] plain = new Builder()
                .field(myPublicKey)
                .field(pingId)
                .build();
        CryptoCore nacl = getEncrypter(peerPublicKey);
        nacl.encrypt(plain);
        byte[] packet = new Builder()
                .field(new byte[] { 2 })
                .field(myPublicKey)
                .field(nacl.getNonce())
                .field(nacl.getCypherText())
                .build();
        getNetwork().send(ipPort, packet);
    }
}
