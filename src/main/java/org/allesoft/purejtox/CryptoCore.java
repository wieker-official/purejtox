package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;

import java.security.SecureRandom;

/**
 * Created by wieker on 12/19/15.
 */
public class CryptoCore extends NaCl {
    byte[] nonce;
    SecureRandom rng = new SecureRandom(); // new_nonce(nonce);
    byte[] result;

    public CryptoCore(byte[] privatekey, byte[] publickey) throws Exception {
        super(privatekey, publickey);
    }

    public void encrypt(byte[] plainText) throws Exception {
        nonce = new byte[Const.NONCE_SIZE];
        rng.nextBytes(nonce);
        byte[] crypt = encrypt(plainText, nonce);
        result = new byte[crypt.length - 16];
        System.arraycopy(crypt, 16, result, 0, crypt.length - 16); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
    }

    public void decryptx(byte[] nonce, byte[] cypherText) throws Exception {
        byte[] crypt = new byte[cypherText.length + 16];
        System.arraycopy(cypherText, 0, crypt, 16, cypherText.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
        byte[] plainText = decrypt(crypt, nonce);
        result = new byte[cypherText.length - Const.crypto_box_MACBYTES];
        System.arraycopy(plainText, 0, result, 0, plainText.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
    }

    public byte[] getNonce() {
        return nonce;
    }

    public byte[] getCypherText() {
        return result;
    }

    public byte[] getPlainText() {
        return result;
    }
}
