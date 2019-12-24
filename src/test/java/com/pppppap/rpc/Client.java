package com.pppppap.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 18:20
 */
public class Client {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 50; i++) {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 1234));
            socket.getOutputStream().write("hello!".getBytes());
            socket.close();
        }
    }
}
