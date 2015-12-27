package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/19/15.
 */
public class DHT extends PacketHandler {

    public DHT(Network network) throws Exception {
        super(network);
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublicKey, myPrivateKey);
        System.out.println(NaCl.asHex(myPublicKey));
        this.network.registerHandler((byte) 4, new SendNodesHandler());
        this.network.registerHandler((byte) 2, new GetNodesHandler());
    }

    public void getnodes(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] pingId = new byte[] { 1, 0, 1, 0, 0, 0, 0, 1, };
        byte[] plain = new Builder()
                .field(myPublicKey)
                .field(pingId)
                .build();
        encryptAndSend(ipPort, peerPublicKey, plain);
    }

    public void sendnodes(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] plain = new Builder()
                .field(new byte[] { 0 })
                .field(back)
                .build();
        encryptAndSend(ipPort, peerPublicKey, plain);
    }

    class SendNodesHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            if (data.length <= (1 + Const.crypto_box_PUBLICKEYBYTES + Const.crypto_box_NONCEBYTES + Const.PING_PLAIN_SIZE + Const.crypto_box_MACBYTES)) {
                System.out.println("Fatal error");
            }

            System.out.printf("sendnodes response parsing\n");
            byte[] plain_text = decryptCrypto(data);

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
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            if (data.length < (1 + Const.crypto_box_PUBLICKEYBYTES + Const.crypto_box_NONCEBYTES + Const.crypto_box_PUBLICKEYBYTES + 8 + Const.crypto_box_MACBYTES)) {
                System.out.println("Fatal error: " + data.length);
            }

            System.out.printf("getnodes request parsing\n");
            byte[] plain_text = decryptCrypto(data);

            Parser general = new Parser(plain_text)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(8)
                    .parse();

            System.out.println("Peer key: " + NaCl.asHex(general.getField(0)));
            System.out.println("Ping id: " + NaCl.asHex(general.getField(1)));

            sendnodes(senderIPPort, general.getField(0), general.getField(1));
        }
    }
}
