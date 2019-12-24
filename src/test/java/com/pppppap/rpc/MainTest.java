package com.pppppap.rpc;

import com.pppppap.rpc.transport.NonblockingServer;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 18:19
 */
public class MainTest {
    public static void main(String[] args) {
        NonblockingServer server = new NonblockingServer();
        server.listen("127.0.0.1", 1234);
    }
}
