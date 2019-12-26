package com.pppppap.rpc;

import com.pppppap.rpc.client.Client;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 18:20
 */
public class ClientTest {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                final Client client = Client.open("127.0.0.1", 1234);
                try {
                    for (int j = 0; j < 30; j++) {
                        Thread.sleep(100);
                        client.send(new User(12345, "cxk", 12, "beijing"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.close();
            }).start();
        }
    }
}
