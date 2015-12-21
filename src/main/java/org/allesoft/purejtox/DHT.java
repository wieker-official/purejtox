package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/19/15.
 */
public class DHT {
    byte[] myPublicKey = new byte[Const.SHARED_SIZE];
    byte[] myPrivateKey = new byte[Const.SHARED_SIZE];

    Network network;

    public DHT() throws Exception {
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublicKey, myPrivateKey);
        System.out.println(NaCl.asHex(myPublicKey));
        this.network = new Network();
        network.registerHandler((byte) 4, new SendNodesHandler());
        network.registerHandler((byte) 2, new GetNodesHandler());
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

    class SendNodesHandler implements NetworkHandler {

        @Override
        public void handle(byte[] data) throws Exception {
            if (data.length <= (1 + Const.crypto_box_PUBLICKEYBYTES + Const.crypto_box_NONCEBYTES + Const.PING_PLAIN_SIZE + Const.crypto_box_MACBYTES)) {
                System.out.println("Fatal error");
            }

            int dataSize = data.length - (1 + Const.crypto_box_PUBLICKEYBYTES + Const.crypto_box_NONCEBYTES + 1 + 8 + Const.crypto_box_MACBYTES);

            System.out.printf("sendnodes response parsing\n");
            Parser parser = new Parser(data)
                    .field(1)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(Const.crypto_box_NONCEBYTES)
                    .last()
                    .parse();
            CryptoCore nacl = getEncrypter(parser.getField(1));

            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));

            nacl.decryptx(parser.getField(2), parser.getField(3));
            byte[] plain_text = nacl.getPlainText();

            System.out.println("Plain text length: " + plain_text.length);
            System.out.println("1 + sizeof(uint64_t) + crypto_box_MACBYTES: " + (1 + 8 + Const.crypto_box_MACBYTES));
            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));
            System.out.println("Payload: " + NaCl.asHex(plain_text));

            Parser general = new Parser(plain_text)
                    .field(1)
                    .field(plain_text.length - 1 - 8)
                    .field(8)
                    .parse();

            int count = (int) general.getField(0)[0];
            System.out.println("Count: " + count);
            System.out.println("Count: " + NaCl.asHex(general.getField(0)));
            System.out.println("Comeback: " + NaCl.asHex(general.getField(2)));
            byte[] payload = general.getField(1);
            Parser addr = new Parser(payload)
                    .field(1)
                    .field(4)
                    .field(2)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .parse();
            System.out.println("Ip family: " + (int) addr.getField(0)[0]);
            System.out.println("Ip: " + (0xff & (int) addr.getField(1)[0]) + "." + (0xff & (int) addr.getField(1)[1]) + "." + (0xff & (int) addr.getField(1)[2]) + "." + (0xff & (int) addr.getField(1)[3]));
            System.out.println("Port: " + ((0xff & (int) addr.getField(2)[0]) * 256 + (0xff & (int) addr.getField(2)[1])));
            System.out.println("Peer key: " + NaCl.asHex(addr.getField(3)));
        }
    }

    class GetNodesHandler implements NetworkHandler {

        @Override
        public void handle(byte[] data) throws Exception {
            if (data.length < (1 + Const.crypto_box_PUBLICKEYBYTES + Const.crypto_box_NONCEBYTES + Const.crypto_box_PUBLICKEYBYTES + 8 + Const.crypto_box_MACBYTES)) {
                System.out.println("Fatal error: " + data.length);
            }

            System.out.printf("getnodes request parsing\n");
            Parser parser = new Parser(data)
                    .field(1)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(Const.crypto_box_NONCEBYTES)
                    .last()
                    .parse();
            CryptoCore nacl = getEncrypter(parser.getField(1));

            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));

            nacl.decryptx(parser.getField(2), parser.getField(3));
            byte[] plain_text = nacl.getPlainText();

            System.out.println("Plain text length: " + plain_text.length);
            System.out.println("1 + sizeof(uint64_t) + crypto_box_MACBYTES: " + (1 + 8 + Const.crypto_box_MACBYTES));
            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));
            System.out.println("Payload: " + NaCl.asHex(plain_text));

            Parser general = new Parser(plain_text)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(8)
                    .parse();

            System.out.println("Peer key: " + NaCl.asHex(general.getField(0)));
            System.out.println("Ping id: " + NaCl.asHex(general.getField(1)));
        }
    }
}
