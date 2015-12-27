package com.neilalexander.jnacl.tox;

import com.neilalexander.jnacl.NaCl;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.Network;
import org.allesoft.purejtox.NetworkHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by wieker on 12/28/15.
 */
public class NetworkImplForTest implements Network {
    InputStream in;
    Map<Byte, NetworkHandler> networkHandlerMap = new TreeMap<Byte, NetworkHandler>();

    public NetworkImplForTest() throws Exception {
        in = getClass().getResourceAsStream("/fulldump");
    }

    @Override
    public void send(IPPort ipPort, byte[] packet) throws Exception {

    }

    @Override
    public void poll() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        for (;;) {
            String line = reader.readLine();
            if (line.contains("Data sent")) {
                line = line.substring(25);
                byte[] data = NaCl.getBinary(line);

                byte key = data[0];
                NetworkHandler handler = networkHandlerMap.get(key);
                if (handler != null) {
                    IPPort ipPort = new IPPort("127.0.0.1", 10);
                    handler.handle(ipPort, data);
                } else {
                    String modifiedSentence = NaCl.asHex(data);
                    System.out.println("Unknown FROM SERVER:" + modifiedSentence);
                    System.exit(0);
                }
            }
        }
    }

    @Override
    public void registerHandler(byte code, NetworkHandler handler) {
        networkHandlerMap.put(code, handler);
    }
}
