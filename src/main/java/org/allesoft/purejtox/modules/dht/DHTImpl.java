package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.Network;
import org.allesoft.purejtox.modules.network.NetworkHandler;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wieker on 12/19/15.
 */
public class DHTImpl implements DHT {

    private DHTPacketHandler dhtPacketHandler;

    public DHTImpl(Network network) throws Exception {
        dhtPacketHandler = new DHTPacketHandler(network);
        curve25519xsalsa20poly1305.crypto_box_keypair(dhtPacketHandler.myPublicKey, dhtPacketHandler.myPrivateKey);
        System.out.println(NaCl.asHex(dhtPacketHandler.myPublicKey));
        network.registerHandler(PacketType.SEND_NODES, new SendNodesHandler());
        network.registerHandler(PacketType.GET_NODES, new GetNodesHandler());
        ;
        network.registerHandler(PacketType.PING_RESPONSE, new PongHandler());
        network.registerHandler(PacketType.PING_REQUEST, new PingHandler());
    }

    @Override
    public void bootstrap(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        getnodes(ipPort, peerPublicKey, dhtPacketHandler.myPublicKey);
    }

    class KeyEntry {
        IPPort ipp;
        byte[] publicKey;
        long timestamp;
    }

    List<KeyEntry> closest = new ArrayList<KeyEntry>();

    void add(IPPort ip, byte[] remotePeerKey) throws Exception {
        KeyEntry entry = null;
        for (KeyEntry search : closest) {
            if (search.ipp.port.equals(ip.port) &&
                    search.ipp.ip.equals(ip.ip)) {
                entry = search;
            }
        }
        if (entry == null) {
            entry = new KeyEntry();
            closest.add(entry);
        }
        entry.ipp = ip;
        entry.publicKey = remotePeerKey;
        entry.timestamp = System.currentTimeMillis();

        System.out.println("DHT size: " + getSize());
    }

    @Override
    public void do_() throws Exception {
        for (KeyEntry entry : closest) {
            if (System.currentTimeMillis() - entry.timestamp > 1000l) {
                getnodes(entry.ipp, entry.publicKey, dhtPacketHandler.myPublicKey);
            }
        }
    }

    @Override
    public Integer getSize() {
        return closest.size();
    }

    void getnodes(IPPort ipPort, byte[] peerPublicKey, byte[] keyToResolve) throws Exception {
        byte[] pingId = new byte[]{1, 0, 1, 0, 0, 0, 0, 1,};
        byte[] plain = new Builder()
                .field(keyToResolve)
                .field(pingId)
                .build();
        dhtPacketHandler.encryptAndSend(PacketType.GET_NODES, ipPort, peerPublicKey, plain);
        //System.out.println("Sent to: " + ipPort.ip);
    }

    void sendnodes(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] plain = new Builder()
                .field(new byte[]{0})
                .field(back)
                .build();
        dhtPacketHandler.encryptAndSend(PacketType.SEND_NODES, ipPort, peerPublicKey, plain);
    }

    void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] pingId = new byte[]{1, 0, 1, 0, 0, 0, 0, 1,};
        ping_plain[0] = 0;
        System.arraycopy(pingId, 0, ping_plain, 1, pingId.length);

        dhtPacketHandler.encryptAndSend(PacketType.PING_REQUEST, ipPort, peerPublicKey, ping_plain);
    }

    void pong(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] ping_id = back;
        ping_plain[0] = 1;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);

        dhtPacketHandler.encryptAndSend(PacketType.PING_RESPONSE, ipPort, peerPublicKey, ping_plain);
    }

    class SendNodesHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            //System.out.printf("sendnodes response parsing\n");
            byte[] sendNodesPlainText = dhtPacketHandler.decryptCrypto(data);
            add(senderIPPort, dhtPacketHandler.getLastPeerPublicKey());

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
            for (int i = 0; i < sendNodesResponseEntriesCount; i++) {
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

                ping(new IPPort(nodeIp, nodePort), nodePublicKey);
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

            add(senderIPPort, Arrays.copyOfRange(data, 1, 9));
        }
    }

    class PingHandler implements NetworkHandler {

        @Override
        public void handle(IPPort senderIPPort, byte[] data) throws Exception {
            System.out.printf("Ping parsing\n");
            byte[] plain_text = dhtPacketHandler.decryptCrypto(data);
            System.out.println("Payload: " + NaCl.asHex(plain_text));

            add(senderIPPort, Arrays.copyOfRange(data, 1, 9));
            pong(senderIPPort, dhtPacketHandler.getLastPeerPublicKey(), Arrays.copyOfRange(plain_text, 1, 9));
        }
    }
}
