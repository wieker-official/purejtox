package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;
import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;

/**
 * Created by wieker on 12/19/15.
 */
public class Ping {

    DHT dht;

    public Ping(DHT dht) {
        this.dht = dht;
    }

    public void ping(byte[] peerPublicKey) throws Exception {
        byte[] ping_id = {1, 1, 1, 1, 1, 1, 1, 1, };
        byte[] pk = new byte[Const.DHT_PING_SIZE];
        byte[] ping_plain = new byte[Const.PING_PLAIN_SIZE];

        CryptoCore nacl = dht.getEncrypter(peerPublicKey);

        ping_plain[0] = 0;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length);
        nacl.encrypt(ping_plain);

        pk[0] = 0;
        System.arraycopy(dht.getPublicKey(), 0, pk, 1, dht.getPublicKey().length);
        System.arraycopy(nacl.getNonce(), 0, pk, 1 + dht.getPublicKey().length, nacl.getNonce().length);
        System.arraycopy(nacl.getCypherText(), 0, pk, 1 + dht.getPublicKey().length + nacl.getNonce().length, nacl.getCypherText().length);

        byte[] receiveData = new byte[1024];
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");

        DatagramPacket sendPacket = new DatagramPacket(pk, pk.length, IPAddress, 33445);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = NaCl.asHex(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();
    }
}
