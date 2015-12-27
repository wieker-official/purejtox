package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/19/15.
 */
public class Ping {

    PacketHandler packetHandler;
    PingArray pingArray = new PingArray();

    public Ping(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        this.packetHandler.getNetwork().registerHandler((byte) 1, new PongHandler());
    }

    public void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];

        CryptoCore nacl = packetHandler.getEncrypter(peerPublicKey);

        byte[] ping_id = pingArray.add(peerPublicKey.clone(), ipPort);

        ping_plain[0] = 0;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);
        nacl.encrypt(ping_plain);

        byte[] packet = new Builder()
                .field(new byte[]{0})
                .field(packetHandler.getPublicKey())
                .field(nacl.getNonce())
                .field(nacl.getCypherText())
                .build();

        packetHandler.getNetwork().send(ipPort, packet);
    }

    class PongHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Ping response parsing\n");
            Parser parser = new Parser(data)
                    .field(1)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(Const.crypto_box_NONCEBYTES)
                    .last()
                    .parse();
            CryptoCore nacl = packetHandler.getEncrypter(parser.getField(1));

            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));

            nacl.decryptx(parser.getField(2), parser.getField(3));
            byte[] plain_text = nacl.getPlainText();

            System.out.println("Peer public key: " + NaCl.asHex(parser.getField(1)));
            System.out.println("Cypher: " + NaCl.asHex(parser.getField(3)));
            System.out.println("Payload: " + NaCl.asHex(plain_text));
        }
    }
}
