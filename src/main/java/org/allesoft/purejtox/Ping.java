package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

/**
 * Created by wieker on 12/19/15.
 */
public class Ping extends PacketHandler {

    PacketHandler packetHandler;
    PingArray pingArray = new PingArray();

    public Ping(PacketHandler packetHandler) {
        super(packetHandler.getNetwork());
        this.packetHandler = packetHandler;
        this.packetHandler.getNetwork().registerHandler((byte) 1, new PongHandler());
        this.packetHandler.getNetwork().registerHandler((byte) 0, new PingHandler());
    }

    public void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] ping_id = pingArray.add(peerPublicKey.clone(), ipPort);
        ping_plain[0] = 0;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);

        packetHandler.encryptAndSend(ipPort, peerPublicKey, ping_plain);
    }

    class PongHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Ping response parsing\n");
            byte[] plain_text = packetHandler.decryptCrypto(data);
            System.out.println("Payload: " + NaCl.asHex(plain_text));
        }
    }

    public void pong(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] ping_id = pingArray.add(peerPublicKey.clone(), ipPort);
        ping_plain[0] = 1;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);

        packetHandler.encryptAndSend(ipPort, peerPublicKey, ping_plain);
    }

    class PingHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Ping request parsing\n");
            byte[] plain_text = packetHandler.decryptCrypto(data);
            System.out.println("Payload: " + NaCl.asHex(plain_text));
        }
    }
}
