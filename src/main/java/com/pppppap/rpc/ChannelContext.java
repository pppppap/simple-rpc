package com.pppppap.rpc;

import java.nio.channels.SocketChannel;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/26 12:03
 */
public class ChannelContext {
    private SocketChannel channel;

    public ChannelContext(SocketChannel channel) {
        this.channel = channel;
    }

    void send(byte[] bytes) {

    }
}
