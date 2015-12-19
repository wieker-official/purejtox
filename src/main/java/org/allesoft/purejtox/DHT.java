package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;

/**
 * Created by wieker on 12/19/15.
 */
public class DHT {
    byte[] myPublicKey = new byte[Const.SHARED_SIZE];
    byte[] myPrivateKey = new byte[Const.SHARED_SIZE];

    public DHT() {
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublicKey, myPrivateKey);
    }

    public CryptoCore getEncrypter(byte[] peerPublicKey) throws Exception {
        return new CryptoCore(myPrivateKey, peerPublicKey);
    }

    public byte[] getPublicKey() {
        return myPublicKey;
    }
}