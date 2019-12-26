package com.pppppap.rpc.client;

import com.pppppap.rpc.ByteBufferUtils;
import com.pppppap.rpc.codec.Codec;
import com.pppppap.rpc.codec.JdkCodec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 18:27
 */
public class Client {
    private SocketChannel channel;
    private Selector selector;
    private Codec codec;
    private BlockingQueue<Object> queue;
    private volatile boolean stop;
    /** 保存接收到但未处理的数据 */
    private ByteBuffer buffer;

    private Client() {
        codec = new JdkCodec();
        queue = new LinkedBlockingQueue<>();
    }

    public static Client open(String host, int port) {
        try {
            Client client = new Client();
            client.connect(new InetSocketAddress(host, port));
            return client;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connect(SocketAddress remote) throws IOException {
        channel = SocketChannel.open(remote);
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        Runnable runnable = () -> {
            while (!stop) {
                try {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            handRead();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                }
            }
        };
        final Thread thread = new Thread(runnable, "client-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void handRead() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int read = channel.read(buffer);
        if (read == -1) {
            close();
            return;
        }
        buffer.flip();
        if (this.buffer == null || !this.buffer.hasRemaining()) {
            this.buffer = buffer;
        } else {
            this.buffer = ByteBufferUtils.merge(this.buffer, buffer);
            this.buffer.flip();
        }
        decode(this.buffer);
    }

    private void decode(ByteBuffer buffer) {
        if (buffer == null) return;
        while (buffer.hasRemaining()) {
            int oldPosition = buffer.position();
            Object o = callDecode(buffer);
            if (o != null) {
                queue.offer(o);
            }
            if (oldPosition == buffer.position()) {
                break;
            }
        }
    }

    private Object callDecode(ByteBuffer buffer) {
        if (buffer.remaining() < 4) return null;
        buffer.mark();
        final int len = buffer.getInt();
        if (buffer.remaining() < len) {
            buffer.reset();
            return null;
        }
        byte[] bytes = ByteBufferUtils.read(buffer, len);
        return codec.decode(bytes);
    }

    public void send(Object o) {
        final byte[] bytes = codec.code(o);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        try {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            // 这里要转换为读
            buffer.flip();
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public Object get() throws InterruptedException {
        return queue.take();
    }

    public void close() {
        try {
            stop = true;
            System.out.println("断开连接");
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
