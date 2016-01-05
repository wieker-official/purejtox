package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.Network;
import org.allesoft.purejtox.NetworkHandler;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

import java.util.Arrays;

/**
 * Created by wieker on 12/19/15.
 */
public class DHT {

    private DHTPacketHandler dhtPacketHandler;

    public DHT(Network network) throws Exception {
        dhtPacketHandler = new DHTPacketHandler(network);
        curve25519xsalsa20poly1305.crypto_box_keypair(dhtPacketHandler.myPublicKey, dhtPacketHandler.myPrivateKey);
        System.out.println(NaCl.asHex(dhtPacketHandler.myPublicKey));
        network.registerHandler((byte) 4, new SendNodesHandler());
        network.registerHandler((byte) 2, new GetNodesHandler());;
        network.registerHandler((byte) 1, new PongHandler());
        network.registerHandler((byte) 0, new PingHandler());
    }

    public void bootstrap(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        getnodes(ipPort, peerPublicKey, dhtPacketHandler.myPublicKey);
    }

    void getnodes(IPPort ipPort, byte[] peerPublicKey, byte[] keyToResolve) throws Exception {
        byte[] pingId = new byte[] { 1, 0, 1, 0, 0, 0, 0, 1, };
        byte[] plain = new Builder()
                .field(keyToResolve)
                .field(pingId)
                .build();
        dhtPacketHandler.encryptAndSend(ipPort, peerPublicKey, plain);
    }

    void sendnodes(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] plain = new Builder()
                .field(new byte[] { 0 })
                .field(back)
                .build();
        dhtPacketHandler.encryptAndSend(ipPort, peerPublicKey, plain);
    }

    void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] pingId = new byte[] { 1, 0, 1, 0, 0, 0, 0, 1, };
        ping_plain[0] = 0;
        System.arraycopy(pingId, 0, ping_plain, 1, pingId.length);

        dhtPacketHandler.encryptAndSend(ipPort, peerPublicKey, ping_plain);
    }

    void pong(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] ping_id = back;
        ping_plain[0] = 1;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);

        dhtPacketHandler.encryptAndSend(ipPort, peerPublicKey, ping_plain);
    }

    class SendNodesHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("sendnodes response parsing\n");
            byte[] sendNodesPlainText = dhtPacketHandler.decryptCrypto(data);

            Parser parsedSendNodesResponse = new Parser(sendNodesPlainText)
                    .field(1)
                    .field(sendNodesPlainText.length - 1 - 8)
                    .field(8)
                    .parse();

            int sendNodesResponseEntriesCount = (int) parsedSendNodesResponse.getField(0)[0];
            /*System.out.println("Count: " + sendNodesResponseEntriesCount);
            System.out.println("Count: " + NaCl.asHex(parsedSendNodesResponse.getField(0)));
            System.out.println("Comeback: " + NaCl.asHex(parsedSendNodesResponse.getField(2)));*/
            byte[] sendNodesResponseEntriesPayload = parsedSendNodesResponse.getField(1);
            for (int i = 0; i < sendNodesResponseEntriesCount; i ++) {
                int sendNodesResponseEntrySize = 1 + 4 + 2 + Const.crypto_box_PUBLICKEYBYTES;
                byte[] responseEntry = Arrays.copyOfRange(sendNodesResponseEntriesPayload, i * sendNodesResponseEntrySize, (i + 1) * sendNodesResponseEntrySize);

                Parser parsedNodeEntry = new Parser(responseEntry)
                        .field(1)
                        .field(4)
                        .field(2)
                        .field(Const.crypto_box_PUBLICKEYBYTES)
                        .parse();
                int nodeAddressFamily = (int) parsedNodeEntry.getField(0)[0];
                String nodeIp = (0xff & (int) parsedNodeEntry.getField(1)[0]) + "." +
                        (0xff & (int) parsedNodeEntry.getField(1)[1]) + "." +
                        (0xff & (int) parsedNodeEntry.getField(1)[2]) + "." +
                        (0xff & (int) parsedNodeEntry.getField(1)[3]);
                int nodePort = ((0xff & (int) parsedNodeEntry.getField(2)[0]) * 256 + (0xff & (int) parsedNodeEntry.getField(2)[1]));
                byte[] nodePublicKey = parsedNodeEntry.getField(3);
                /*System.out.println("Ip family: " + nodeAddressFamily);
                System.out.println("Ip: " + nodeIp);
                System.out.println("Port: " + nodePort);
                System.out.println("Peer key: " + NaCl.asHex(nodePublicKey));*/

                //ping(new IPPort(nodeIp, nodePort), nodePublicKey);
                getnodes(new IPPort(nodeIp, nodePort), nodePublicKey, dhtPacketHandler.myPublicKey);
            }
        }
    }

    class GetNodesHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("getnodes parsing\n");
            byte[] plain_text = dhtPacketHandler.decryptCrypto(data);

            Parser general = new Parser(plain_text)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .field(8)
                    .parse();

            System.out.println("Peer key: " + NaCl.asHex(general.getField(0)));
            System.out.println("Ping id: " + NaCl.asHex(general.getField(1)));

            sendnodes(senderIPPort, general.getField(0), general.getField(1));
        }
    }

    class PongHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Pong parsing\n");
            byte[] plain_text = dhtPacketHandler.decryptCrypto(data);
            System.out.println("Payload: " + NaCl.asHex(plain_text));
        }
    }

    class PingHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Ping parsing\n");
            byte[] plain_text = dhtPacketHandler.decryptCrypto(data);
            System.out.println("Payload: " + NaCl.asHex(plain_text));

            pong(senderIPPort, dhtPacketHandler.getLastPeerPublicKey(), Arrays.copyOfRange(plain_text, 1, 9));
        }
    }
}
