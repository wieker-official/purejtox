package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by wieker on 12/19/15.
 */
public class Network {
    DatagramSocket clientSocket;

    public Network() throws Exception {
        clientSocket = new DatagramSocket();
    }

    public void send(IPPort ipPort, byte[] packet) throws Exception {
        InetAddress IPAddress = InetAddress.getByName(ipPort.ip);

        DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAddress, ipPort.port);
        clientSocket.send(sendPacket);
    }

    public void poll() throws Exception {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String modifiedSentence = NaCl.asHex(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();
    }
}
