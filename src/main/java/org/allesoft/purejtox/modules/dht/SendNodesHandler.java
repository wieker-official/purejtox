package org.allesoft.purejtox.modules.dht;

import org.allesoft.purejtox.Const;
import org.allesoft.purejtox.IPPort;
import org.allesoft.purejtox.packet.Parser;

import java.util.Arrays;

/**
 * Created by wieker on 1/7/16.
 */
class SendNodesHandler implements DHTNetworkHandler {

    private DHTImpl dht;

    public SendNodesHandler(DHTImpl dht) {
        this.dht = dht;
    }

    @Override
    public void handle(DHTNodeInfo dhtNode, byte[] sendNodesPlainText) throws Exception {
        //System.out.printf("sendnodes response parsing\n");
        dhtNode.sendReceived ++;
        dhtNode.sendTimestamp = System.currentTimeMillis();

        Parser parsedSendNodesResponse = new Parser(sendNodesPlainText)
                .field(1)
                .field(sendNodesPlainText.length - 1 - 8)
                .field(8)
                .parse();

        int sendNodesResponseEntriesCount = (int) parsedSendNodesResponse.getField(0)[0];
        /*System.out.println("Count: " + sendNodesResponseEntriesCount);
        System.out.println("Count: " + NaCl.asHex(parsedSendNodesResponse.getField(0)));
        System.out.println("Comeback: " + NaCl.asHex(parsedSendNodesResponse.getField(2)));*/
        byte[] sendNodesResponseEntriesPayload = parsedSendNodesResponse.getField(1);
        for (int i = 0; i < sendNodesResponseEntriesCount; i++) {
            int sendNodesResponseEntrySize = 1 + 4 + 2 + Const.crypto_box_PUBLICKEYBYTES;
            byte[] responseEntry = Arrays.copyOfRange(sendNodesResponseEntriesPayload, i * sendNodesResponseEntrySize, (i + 1) * sendNodesResponseEntrySize);

            Parser parsedNodeEntry = new Parser(responseEntry)
                    .field(1)
                    .field(4)
                    .field(2)
                    .field(Const.crypto_box_PUBLICKEYBYTES)
                    .parse();
            int nodeAddressFamily = (int) parsedNodeEntry.getField(0)[0];
            String nodeIp = (0xff & (int) parsedNodeEntry.getField(1)[0]) + "." +
                    (0xff & (int) parsedNodeEntry.getField(1)[1]) + "." +
                    (0xff & (int) parsedNodeEntry.getField(1)[2]) + "." +
                    (0xff & (int) parsedNodeEntry.getField(1)[3]);
            int nodePort = ((0xff & (int) parsedNodeEntry.getField(2)[0]) * 256 + (0xff & (int) parsedNodeEntry.getField(2)[1]));
            byte[] nodePublicKey = parsedNodeEntry.getField(3);
            /*System.out.println("Ip family: " + nodeAddressFamily);
            System.out.println("Ip: " + nodeIp);
            System.out.println("Port: " + nodePort);
            System.out.println("Peer key: " + NaCl.asHex(nodePublicKey));*/

            dht.ping(new IPPort(nodeIp, nodePort), nodePublicKey);
            dht.getnodes(dht.add(new IPPort(nodeIp, nodePort), nodePublicKey), dht.getDHTPublicKey());
        }
    }
}
