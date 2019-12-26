package com.pppppap.rpc;

import com.pppppap.rpc.codec.Codec;

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
    private Codec codec;

    public ChannelContext(SocketChannel channel, Codec codec) {
        this.channel = channel;
        this.codec = codec;
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

    public void send(Object obj) {
        final byte[] bytes = codec.code(obj);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length + 4);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw new RpcException("发送数据失败", e);
        }
    }

    public Codec codec() {
        return codec;
    }
}
