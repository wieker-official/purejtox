package com.neilalexander.jnacl;

import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;
import org.testng.annotations.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;

/**
 * Created by wieker on 12/19/15.
 */
public class ToxTest {

    public static final int PLAIN_SIZE = 32 + 8; // uint8_t plain[crypto_box_PUBLICKEYBYTES + sizeof(ping_id)];
    public static final int ENCRYPT_SIZE = PLAIN_SIZE + 16; // uint8_t encrypt[sizeof(plain) + crypto_box_MACBYTES];
    public static final int DATA_SIZE = ENCRYPT_SIZE + 1 + 32 + 24; // uint8_t data[1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + sizeof(encrypt)];
    public static final int SHARED_SIZE = 32; // uint8_t shared_key[crypto_box_BEFORENMBYTES];
    public static final int NONCE_SIZE = 24; // uint8_t nonce[crypto_box_NONCEBYTES];

    byte[] plain = new byte[PLAIN_SIZE];
    byte[] encrypt = new byte[ENCRYPT_SIZE];
    byte[] data = new byte[DATA_SIZE];
    byte[] shared_key = new byte[SHARED_SIZE];
    byte[] nonce = new byte[NONCE_SIZE];

    @Test
    public void testUdp() throws Exception {

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        byte[] myPublic = new byte[SHARED_SIZE];
        byte[] myPrivate = new byte[SHARED_SIZE];
        curve25519xsalsa20poly1305.crypto_box_keypair(myPrivate, myPublic);
        System.arraycopy(myPublic, 0, plain, 0, SHARED_SIZE); // memcpy(plain, client_id, crypto_box_PUBLICKEYBYTES);
        System.arraycopy(myPublic, 0, plain, SHARED_SIZE, PLAIN_SIZE - SHARED_SIZE); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));

        String peerKey = "5A390B1F5B13461C7BEE076BBB4C3AFF70B607CA211B0981FEA01F644F64F557";
        byte[] peerPublic = NaCl.getBinary(peerKey);
        NaCl nacl = new NaCl(myPrivate, peerPublic); // DHT_get_shared_key_sent(dht, shared_key, public_key);

        SecureRandom rng = new SecureRandom(); // new_nonce(nonce);
        rng.nextBytes(nonce);

        byte[] encrypt1 = nacl.encrypt(plain, nonce);
        System.out.println("enc1.len" + encrypt1.length);
        System.out.println("enc.len" + encrypt.length);

        DatagramPacket sendPacket = new DatagramPacket(encrypt1, encrypt1.length, IPAddress, 33445);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = NaCl.asHex(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();
    }

    public static final int crypto_box_MACBYTES = 16;
    public static final int crypto_box_NONCEBYTES = 24;
    public static final int crypto_box_PUBLICKEYBYTES = 32;
    public static final int PING_PLAIN_SIZE = 1 + 8; // #define PING_PLAIN_SIZE (1 + sizeof(uint64_t))
    public static final int DHT_PING_SIZE = (1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + PING_PLAIN_SIZE + crypto_box_MACBYTES); // #define DHT_PING_SIZE (1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + PING_PLAIN_SIZE + crypto_box_MACBYTES)
    public static final int PING_DATA_SIZE = (crypto_box_PUBLICKEYBYTES + 11); // #define PING_DATA_SIZE (crypto_box_PUBLICKEYBYTES + sizeof(IP_Port))

    @Test
    public void testPing() throws Exception {
        byte[] ping_id = {1, 1, 1, 1, 1, 1, 1, 1, };
        byte[] pk = new byte[DHT_PING_SIZE];
        byte[] ping_plain = new byte[PING_PLAIN_SIZE];

        byte[] myPublic = new byte[SHARED_SIZE];
        byte[] myPrivate = new byte[SHARED_SIZE];
        curve25519xsalsa20poly1305.crypto_box_keypair(myPublic, myPrivate);
        String peerKey = "5A390B1F5B13461C7BEE076BBB4C3AFF70B607CA211B0981FEA01F644F64F557";
        byte[] peerPublic = NaCl.getBinary(peerKey);
        NaCl nacl = new NaCl(myPrivate, peerPublic);
        SecureRandom rng = new SecureRandom(); // new_nonce(nonce);
        rng.nextBytes(nonce);

        ping_plain[0] = 0;
        System.arraycopy(ping_id, 0, ping_plain, 1, ping_id.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
        System.out.println("TO SERVER: " + NaCl.asHex(ping_plain));
        nacl.print_key();
        System.out.println("private: " + NaCl.asHex(myPrivate));

        pk[0] = 0;
        System.arraycopy(myPublic, 0, pk, 1, myPublic.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
        System.arraycopy(nonce, 0, pk, 1 + myPublic.length, nonce.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
        byte[] crypt = nacl.encrypt(ping_plain, nonce);
        System.arraycopy(ping_plain, 0, pk, 1 + myPublic.length + nonce.length, ping_plain.length); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));
        System.arraycopy(crypt, 16, pk, 1 + myPublic.length + nonce.length, crypt.length - 16); // memcpy(plain + crypto_box_PUBLICKEYBYTES, &ping_id, sizeof(ping_id));

        System.out.println("TO SERVER: " + NaCl.asHex(pk));
        System.out.println("Encrypted: " + NaCl.asHex(crypt));

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

    @Test
    public void checkShared() throws Exception {
        String privateKey = "4d54609c9c9611f9715b0408c47f0ffff54c48c72d372c3659819f917bf90404";
        String publicKey = "9b96e5ae11854bdcfecef22ea9af3c7dd0f69b83d7a54e13d53b6631c0f9c949";
        NaCl nacl = new NaCl(NaCl.getBinary(privateKey), NaCl.getBinary(publicKey));
        nacl.print_key();
    }

    @Test
    public void checkShared2() throws Exception {
        String privateKey2 = "ca18b3e641caf86fac88808e8ad05ce85fed70dce7cc74834796c98def57a33d";
        String publicKey2 = "5A390B1F5B13461C7BEE076BBB4C3AFF70B607CA211B0981FEA01F644F64F557";
        NaCl nacl2 = new NaCl(NaCl.getBinary(privateKey2), NaCl.getBinary(publicKey2));
        nacl2.print_key();
    }

    @Test
    public void checkShared3() throws Exception {
        String privateKey2 = "34e61442565ac9375758e0391a5eef51a558b4c2996ac11f666c18b217a0054d";
        byte[] key = new byte[32];
        curve25519xsalsa20poly1305.crypto_box_getpublickey(key, NaCl.getBinary(privateKey2));
        System.out.println(NaCl.asHex(key));
    }
}
