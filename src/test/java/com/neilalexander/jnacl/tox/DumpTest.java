package com.neilalexander.jnacl.tox;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.*;
import org.testng.annotations.Test;

/**
 * Created by wieker on 12/28/15.
 */
public class DumpTest {

    @Test
    public void firstTest() throws Exception {
        Network network = new NetworkImplForTest();

        network.registerHandler((byte) 0, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_PING_REQUEST");
            }
        });

        network.registerHandler((byte) 1, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_PING_RESPONSE");
            }
        });

        network.registerHandler((byte) 2, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_GET_NODES");
            }
        });

        network.registerHandler((byte) 4, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_SEND_NODES_IPV6");
            }
        });

        network.registerHandler((byte) 24, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_COOKIE_REQUEST");
            }
        });

        network.registerHandler((byte) 25, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_COOKIE_RESPONSE");
            }
        });

        network.registerHandler((byte) 26, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_CRYPTO_HS");
            }
        });

        network.registerHandler((byte) 27, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_CRYPTO_DATA");
            }
        });

        network.registerHandler((byte) 32, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_CRYPTO");
            }
        });

        network.registerHandler((byte) 33, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_LAN_DISCOVERY");
            }
        });

        network.registerHandler((byte) 128, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_SEND_INITIAL");
            }
        });

        network.registerHandler((byte) 129, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_SEND_1");
            }
        });

        network.registerHandler((byte) 130, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_SEND_2");
            }
        });

        network.registerHandler((byte) 131, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ANNOUNCE_REQUEST");
            }
        });

        network.registerHandler((byte) 132, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ANNOUNCE_RESPONSE");
            }
        });

        network.registerHandler((byte) 133, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_DATA_REQUEST");
            }
        });

        network.registerHandler((byte) 134, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_DATA_RESPONSE");
            }
        });

        network.registerHandler((byte) 140, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_RECV_3");
            }
        });

        network.registerHandler((byte) 141, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_RECV_2");
            }
        });

        network.registerHandler((byte) 142, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {
                System.out.print("To: " + senderIPPort.port + ": ");
                System.out.println("NET_PACKET_ONION_RECV_1");
            }
        });

        network.poll();
    }

}
