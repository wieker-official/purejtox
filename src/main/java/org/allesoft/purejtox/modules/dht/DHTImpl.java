package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.Network;
import org.allesoft.purejtox.packet.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wieker on 12/19/15.
 */
public class DHTImpl implements DHT {

    private DHTPacketAdapter dhtPacketAdapter;

    public DHTImpl(Network network) throws Exception {
        dhtPacketAdapter = new DHTPacketAdapter(network);
        curve25519xsalsa20poly1305.crypto_box_keypair(dhtPacketAdapter.myPublicKey, dhtPacketAdapter.myPrivateKey);
        System.out.println(NaCl.asHex(dhtPacketAdapter.myPublicKey));
        network.registerHandler(PacketType.SEND_NODES, new SendNodesHandler(this));
        network.registerHandler(PacketType.GET_NODES, new GetNodesHandler(this));
        ;
        network.registerHandler(PacketType.PING_RESPONSE, new PongHandler(this));
        network.registerHandler(PacketType.PING_REQUEST, new PingHandler(this));
    }

    @Override
    public void bootstrap(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        getnodes(ipPort, peerPublicKey, dhtPacketAdapter.myPublicKey);
    }

    List<KnownDHTNode> knownNodes = new ArrayList<KnownDHTNode>();

    void add(IPPort ip, byte[] remotePeerKey) throws Exception {
        KnownDHTNode entry = null;
        for (KnownDHTNode search : knownNodes) {
            if (search.ipp.port.equals(ip.port) &&
                    search.ipp.ip.equals(ip.ip)) {
                entry = search;
            }
        }
        if (entry == null) {
            entry = new KnownDHTNode();
            knownNodes.add(entry);

            System.out.println("IP: " + ip.ip + " DHT size: " + getSize());
        }
        entry.ipp = ip;
        entry.publicKey = remotePeerKey;
        entry.timestamp = System.currentTimeMillis();
    }

    @Override
    public void do_() throws Exception {
        for (KnownDHTNode entry : knownNodes) {
            if (System.currentTimeMillis() - entry.timestamp > 1000l) {
                try {
                    getnodes(entry.ipp, entry.publicKey, dhtPacketAdapter.myPublicKey);
                } catch (Exception e) {
                    throw new Exception("Exception for IP: " + entry.ipp.ip, e);
                }
            }
        }
    }

    @Override
    public Integer getSize() {
        return knownNodes.size();
    }

    void getnodes(IPPort ipPort, byte[] peerPublicKey, byte[] keyToResolve) throws Exception {
        byte[] pingId = new byte[]{1, 0, 1, 0, 0, 0, 0, 1,};
        byte[] plain = new Builder()
                .field(keyToResolve)
                .field(pingId)
                .build();
        dhtPacketAdapter.encryptAndSend(PacketType.GET_NODES, ipPort, peerPublicKey, plain);
        //System.out.println("Sent to: " + ipPort.ip);
    }

    void sendnodes(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] plain = new Builder()
                .field(new byte[]{0})
                .field(back)
                .build();
        dhtPacketAdapter.encryptAndSend(PacketType.SEND_NODES, ipPort, peerPublicKey, plain);
    }

    void ping(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] pingId = new byte[]{1, 0, 1, 0, 0, 0, 0, 1,};
        ping_plain[0] = 0;
        System.arraycopy(pingId, 0, ping_plain, 1, pingId.length);

        dhtPacketAdapter.encryptAndSend(PacketType.PING_REQUEST, ipPort, peerPublicKey, ping_plain);
    }

    void pong(IPPort ipPort, byte[] peerPublicKey, byte[] back) throws Exception {
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];
        byte[] ping_id = back;
        ping_plain[0] = 1;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);

        dhtPacketAdapter.encryptAndSend(PacketType.PING_RESPONSE, ipPort, peerPublicKey, ping_plain);
    }

    DHTPacketAdapter getDhtPacketAdapter() {
        return dhtPacketAdapter;
    }

}