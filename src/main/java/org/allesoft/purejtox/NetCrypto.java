package org.allesoft.purejtox;

import org.allesoft.purejtox.packet.Builder;

/**
 * Created by wieker on 12/19/15.
 */
public class NetCrypto {

    PacketHandler packetHandler;

    public NetCrypto(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public void sendCookie(IPPort ipPort, byte[] peerPublicKey) throws Exception {
        byte[] plain = new Builder()
                .field(packetHandler.myPublicKey)
                .field(new byte[Const.crypto_box_PUBLICKEYBYTES])
                .field(new byte[8])
                .build();

        CryptoCore nacl = packetHandler.getEncrypter(peerPublicKey);
        nacl.encrypt(plain);

        byte[] packet = new Builder()
                .field(new byte[]{24})
                .field(packetHandler.getPublicKey())
                .field(nacl.getNonce())
                .field(nacl.getCypherText())
                .build();

        packetHandler.getNetwork().send(ipPort, packet);
    }
}
