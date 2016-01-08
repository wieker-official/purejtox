package org.allesoft.purejtox.modules.network;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.dht.PacketType;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wieker on 12/19/15.
 */
public class NetworkImpl implements Network {
    DatagramSocket clientSocket;
    Map<Byte, NetworkHandler> networkHandlerMap = new TreeMap<Byte, NetworkHandler>();

    public NetworkImpl() throws Exception {
        clientSocket = new DatagramSocket(33445);
        clientSocket.setSoTimeout(100);
    }

    public NetworkImpl(Integer port) throws Exception {
        clientSocket = new DatagramSocket(port);
        clientSocket.setSoTimeout(100);
    }

    @Override
    public void send(IPPort ipPort, byte[] packet) throws Exception {
        //System.out.println(ipPort.ip + ":" + ipPort.port);
        InetAddress IPAddress = InetAddress.getByName(ipPort.ip);

        DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAddress, ipPort.port);
        clientSocket.send(sendPacket);
    }

    @Override
    public void poll() throws Exception {
        byte[] receiveData = new byte[102400];
        byte[] packet;
        for (int i = 0; i < 100; i ++) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            packet = new byte[receivePacket.getLength()];
            System.arraycopy(receiveData, 0, packet, 0, packet.length);

            byte key = receiveData[0];
            NetworkHandler handler = networkHandlerMap.get(key);
            if (handler != null) {
                IPPort ipPort = new IPPort(receivePacket.getAddress().getHostAddress(), receivePacket.getPort());
                handler.handle(ipPort, packet);
            } else {
                String modifiedSentence = NaCl.asHex(packet);
                System.out.println("Unknown FROM SERVER:" + modifiedSentence);
            }
        }
    }

    @Override
    public void registerHandler(PacketType code, NetworkHandler handler) {
        networkHandlerMap.put(code.getCode(), handler);
    }
}
