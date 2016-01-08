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

    List<DHTNodeInfo> knownNodes = new ArrayList<DHTNodeInfo>();
    List<byte[]> searchList = new ArrayList<byte[]>();

    public DHTImpl(Network network) throws Exception {
        dhtPacketAdapter = new DHTPacketAdapter(network, this);
        System.out.println(NaCl.asHex(dhtPacketAdapter.getPublicKey()));
        network.registerHandler(PacketType.SEND_NODES, dhtPacketAdapter);
        dhtPacketAdapter.registerHandler(PacketType.SEND_NODES, new SendNodesHandler(this));
        network.registerHandler(PacketType.GET_NODES, dhtPacketAdapter);
        dhtPacketAdapter.registerHandler(PacketType.GET_NODES, new GetNodesHandler(this));

        network.registerHandler(PacketType.PING_RESPONSE, dhtPacketAdapter);
        dhtPacketAdapter.registerHandler(PacketType.PING_RESPONSE, new PongHandler(this));
        network.registerHandler(PacketType.PING_REQUEST, dhtPacketAdapter);
        dhtPacketAdapter.registerHandler(PacketType.PING_REQUEST, new PingHandler(this));
    }

    @Override
    public void bootstrap(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        getnodes(add(ipPort, peerPublicKey), dhtPacketAdapter.getPublicKey());
    }

    @Override
    public void do_() throws Exception {
        for (DHTNodeInfo entry : knownNodes) {
            if (System.currentTimeMillis() - entry.timestamp > 1000l && entry.received) {
                try {
                    getnodes(entry, dhtPacketAdapter.getPublicKey());
                    if (searchList.size() > 0) {
                        getnodes(entry, searchList.get(0));
                    }
                } catch (Exception e) {
                    throw new Exception("Exception for IP: " + entry.ipPort.ip, e);
                }
            }
        }
    }

    @Override
    public Integer getSize() {
        return knownNodes.size();
    }

    @Override
    public void printStat() {
        for (DHTNodeInfo info : knownNodes) {
            System.out.println("this: " + this + " IP: " + info.ipPort.ip + " Received Info: " + info.sendReceived + " Ping: " + info.pingReceived +
                    " Pong: " + info.pongReceived + " Sent: " + info.getSent);
        }
    }

    void getnodes(DHTNodeInfo node, byte[] keyToResolve) throws Exception {
        byte[] pingId = new byte[]{1, 0, 1, 0, 0, 0, 0, 1,};
        byte[] plain = new Builder()
                .field(keyToResolve)
                .field(pingId)
                .build();
        dhtPacketAdapter.encryptAndSend(PacketType.GET_NODES, node.ipPort, node.publicKey, plain);
        //System.out.println("Sent to: " + ipPort.ip);
        node.getSent ++;
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

    public byte[] getDHTPublicKey() {
        return dhtPacketAdapter.getPublicKey();
    }

    DHTNodeInfo add(IPPort ip, byte[] remotePeerKey) throws Exception {
        DHTNodeInfo entry = null;
        for (DHTNodeInfo search : knownNodes) {
            if (search.ipPort.port.equals(ip.port) &&
                    search.ipPort.ip.equals(ip.ip)) {
                entry = search;
            }
        }
        if (entry == null) {
            entry = new DHTNodeInfo();
            knownNodes.add(entry);

            System.out.println("IP: " + ip.ip + " DHT size: " + getSize());
        }
        entry.ipPort = ip;
        entry.publicKey = remotePeerKey;
        entry.timestamp = System.currentTimeMillis();

        return entry;
    }

    @Override
    public void addSearch(byte[] key) {
        searchList.add(key);
    }
}
