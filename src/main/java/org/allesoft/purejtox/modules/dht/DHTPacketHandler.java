package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.CryptoCore;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.Network;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/28/15.
 */
public class DHTPacketHandler {
    byte[] myPublicKey = new byte[Const.SHARED_SIZE];
    byte[] myPrivateKey = new byte[Const.SHARED_SIZE];
    Network network;

    byte[] lastPeerPublicKey;

    public DHTPacketHandler(Network network) {
        this.network = network;
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

    public void encryptAndSend(IPPort ipPort, byte[] peerPublicKey, byte[] plain) throws Exception {
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

    public byte[] decryptCrypto(byte[] data) throws Exception {
        Parser parser = new Parser(data)
                .field(1)
                .field(Const.crypto_box_PUBLICKEYBYTES)
                .field(Const.crypto_box_NONCEBYTES)
                .last()
                .parse();
        CryptoCore nacl = getEncrypter(parser.getField(1));

        /*System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
        System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));*/

        nacl.decryptx(parser.getField(2), parser.getField(3));
        byte[] plain_text = nacl.getPlainText();

        /*System.out.println("Plain text length: " + plain_text.length);
        System.out.println("1 + sizeof(uint64_t) + crypto_box_MACBYTES: " + (1 + 8 + Const.crypto_box_MACBYTES));
        System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
        System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));
        System.out.println("Payload: " + NaCl.asHex(plain_text));*/

        lastPeerPublicKey = parser.getField(1);

        return plain_text;
    }

    public byte[] getLastPeerPublicKey() {
        return lastPeerPublicKey;
    }
}
