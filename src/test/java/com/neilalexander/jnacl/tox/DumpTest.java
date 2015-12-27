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
        DHT dht = new DHT(new NetworkImplForTest());
        Ping ping = new Ping(dht);

        IPPort ipPort = new IPPort("localhost", 33445);
        String peerKey = "5A390B1F5B13461C7BEE076BBB4C3AFF70B607CA211B0981FEA01F644F64F557";
        byte[] peerPublic = NaCl.getBinary(peerKey);
        ping.ping(ipPort, peerPublic);

        dht.getnodes(ipPort, peerPublic);
        dht.getNetwork().registerHandler((byte) 33, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {

            }
        });
        dht.getNetwork().registerHandler((byte) 32, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {

            }
        });
        dht.getNetwork().registerHandler((byte) 128, new NetworkHandler() {
            @Override
            public void handle(IPPort senderIPPort, byte[] data) throws Exception {

            }
        });

        dht.getNetwork().poll();
    }

}
