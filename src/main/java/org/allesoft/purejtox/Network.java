package org.allesoft.purejtox;

import com.neilalexander.jnacl.NaCl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wieker on 12/19/15.
 */
public class Network {
    DatagramSocket clientSocket;
    Map<Byte, NetworkHandler> networkHandlerMap = new TreeMap<Byte, NetworkHandler>();

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
        for (;;) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            byte key = receiveData[0];
            NetworkHandler handler = networkHandlerMap.get(key);
            if (handler != null) {
                handler.handle(receiveData);
            } else {
                String modifiedSentence = NaCl.asHex(receivePacket.getData());
                System.out.println("Unknown FROM SERVER:" + modifiedSentence);
            }
        }
    }

    public void registerHandler(byte code, NetworkHandler handler) {
        networkHandlerMap.put(code, handler);
    }
}
