package org.allesoft.purejtox;

/**
 * Created by wieker on 12/19/15.
 */
public class Const {

    public static final int PLAIN_SIZE = 32 + 8; // uint8_t plain[crypto_box_PUBLICKEYBYTES + sizeof(ping_id)];
    public static final int ENCRYPT_SIZE = PLAIN_SIZE + 16; // uint8_t encrypt[sizeof(plain) + crypto_box_MACBYTES];
    public static final int DATA_SIZE = ENCRYPT_SIZE + 1 + 32 + 24; // uint8_t data[1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + sizeof(encrypt)];
    public static final int SHARED_SIZE = 32; // uint8_t shared_key[crypto_box_BEFORENMBYTES];
    public static final int NONCE_SIZE = 24; // uint8_t nonce[crypto_box_NONCEBYTES];

    public static final int crypto_box_MACBYTES = 16;
    public static final int crypto_box_NONCEBYTES = 24;
    public static final int crypto_box_PUBLICKEYBYTES = 32;
    public static final int PING_PLAIN_SIZE = 1 + 8; // #define PING_PLAIN_SIZE (1 + sizeof(uint64_t))
    public static final int DHT_PING_SIZE = (1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + PING_PLAIN_SIZE + crypto_box_MACBYTES); // #define DHT_PING_SIZE (1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + PING_PLAIN_SIZE + crypto_box_MACBYTES)
    public static final int PING_DATA_SIZE = (crypto_box_PUBLICKEYBYTES + 11); // #define PING_DATA_SIZE (crypto_box_PUBLICKEYBYTES + sizeof(IP_Port))
}
