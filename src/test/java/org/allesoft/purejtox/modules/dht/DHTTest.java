package org.allesoft.purejtox.modules.dht;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.modules.dht.DHT;
import org.allesoft.purejtox.modules.dht.DHTImpl;
import org.allesoft.purejtox.modules.network.Network;
import org.allesoft.purejtox.modules.network.NetworkImpl;
import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

/**
 * Created by wieker on 1/8/16.
 */
public class DHTTest {

    @Test
    public void firstTest() throws Exception {
        Network network = new NetworkImpl();
        DHT dht = new DHTImpl(network);

        Network net2 = new NetworkImpl(33446);
        DHT dht2 = new DHTImpl(net2);

        IPPort ipPort = new IPPort("144.76.60.215", 33445);
        String peerKey = "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F";
        byte[] peerPublic = NaCl.getBinary(peerKey);
        //dht.ping(ipPort, peerPublic);

        dht.bootstrap(ipPort, peerPublic);
        dht2.bootstrap(ipPort, peerPublic);

        dht.addSearch(dht2.getDHTPublicKey());
        dht2.addSearch(dht.getDHTPublicKey());

        for (;;) {
            for (int i = 0; i < 100; i++) {
                try {
                    network.poll();
                    net2.poll();
                } catch (SocketTimeoutException e) {
                    //System.out.println("timeout");
                }
                dht.do_();
                dht2.do_();
                //System.out.println("DHT size: " + dht.getSize());
            }
            dht.printStat();
            dht2.printStat();
        }
    }

    public void btst() throws Exception {

    }

}
