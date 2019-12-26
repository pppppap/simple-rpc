package com.pppppap.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 连接上下文，用来发送数据和获取连接信息
 *
 * @author liujinrui
 * @since 2019/12/26 12:03
 */
public class ChannelContext {
    private SocketChannel channel;

    public ChannelContext(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel channel() {
        return this.channel;
    }

    public InetSocketAddress remoteAddress() {
        try {
            return (InetSocketAddress) channel.getRemoteAddress();
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    public InetSocketAddress localAddress() {
        try {
            return (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    public void send(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.flip();
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw new RpcException("发送数据失败", e);
        }
    }
}
