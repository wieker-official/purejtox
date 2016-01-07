package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.CryptoCore;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.network.Network;
import org.allesoft.purejtox.modules.network.NetworkHandler;
import org.allesoft.purejtox.packet.Builder;
import org.allesoft.purejtox.packet.Parser;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wieker on 12/28/15.
 */
public class DHTPacketAdapter implements NetworkHandler {
    private byte[] myPublicKey = new byte[Const.SHARED_SIZE];
    private byte[] myPrivateKey = new byte[Const.SHARED_SIZE];
    private Network network;
    private DHTImpl dht;
    private Map<Byte, DHTNetworkHandler> networkHandlerMap = new TreeMap<Byte, DHTNetworkHandler>();

    private byte[] lastPeerPublicKey;

    public DHTPacketAdapter(Network network, DHTImpl dht) {
        this.network = network;
        this.dht = dht;
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublicKey, myPrivateKey);
    }

    private CryptoCore getEncrypter(byte[] peerPublicKey) throws Exception {
        return new CryptoCore(myPrivateKey, peerPublicKey);
    }

    public byte[] getPublicKey() {
        return myPublicKey;
    }

    private Network getNetwork() {
        return network;
    }

    public void encryptAndSend(PacketType type, IPPort ipPort, byte[] peerPublicKey, byte[] plain) throws Exception {
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

    private byte[] decryptCrypto(byte[] data) throws Exception {
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

    private byte[] getLastPeerPublicKey() {
        return lastPeerPublicKey;
    }

    @Override
    public void handle(IPPort senderIPPort, byte[] data) throws Exception {
        byte[] plain_text = decryptCrypto(data);
        DHTNodeInfo info = dht.add(senderIPPort,
                Arrays.copyOf(getLastPeerPublicKey(),
                        Const.crypto_box_PUBLICKEYBYTES));
        info.received = false;
        networkHandlerMap.get(data[0]).handle(info, plain_text);
    }

    public void registerHandler(PacketType code, DHTNetworkHandler handler) {
        networkHandlerMap.put(code.getCode(), handler);
    }
}
