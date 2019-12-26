package com.pppppap.rpc.server;

import com.pppppap.rpc.ChannelContext;
import com.pppppap.rpc.Dispatcher;
import com.pppppap.rpc.codec.Codec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NonblockingServer {
    /** 处理客户端读写 */
    private SelectorThread[] selectorThreads;
    /** 建立新连接 */
    private AcceptThread acceptThread;
    /** 选择器 */
    private SelectNextSelectorThread nextSelectorThread;
    private Codec codec;

    NonblockingServer(int selectorThreadNum, Codec codec) {
        if (selectorThreadNum < 1) {
            throw new IllegalArgumentException("SelectorThread线程数量不能低于1");
        }
        this.codec = codec;
        selectorThreads = new SelectorThread[selectorThreadNum];
        try {
            for (int i = 0; i < selectorThreads.length; i++) {
                selectorThreads[i] = new SelectorThread("selector-thread-" + i);
            }
            acceptThread = new AcceptThread("accept-thread");
            nextSelectorThread = new SelectNextSelectorThread(Arrays.asList(selectorThreads));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void listen(int port) {
        listen("0.0.0.0", port);
    }

    public void listen(String host, int port) {
        try {
            acceptThread.listen(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理连接事件，把新连接注册到一个SelectorThread上
     */
    class AcceptThread extends Thread {
        private Selector selector;
        private ServerSocketChannel serverChannel;

        AcceptThread(String name) throws IOException {
            super(name);
            selector = Selector.open();
        }

        public void listen(String host, int port) throws IOException {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverChannel.bind(new InetSocketAddress(host, port));
            this.start();
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int select = selector.select();
                    if (select == 0) {
                        continue;
                    }
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            handAccept();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void handAccept() throws IOException {
            SocketChannel clientChannel = serverChannel.accept();
            SelectorThread selectorThread = nextSelectorThread.nextThread();
            // 懒加载
            if (!selectorThread.isStart()) {
                selectorThread.start();
            }
            selectorThread.register(clientChannel);
        }
    }

    /**
     * 处理客户端连接的读/写事件
     */
    class SelectorThread extends Thread {
        private Selector selector;
        private BlockingQueue<Runnable> tasks;
        private volatile boolean start = false;

        SelectorThread(String name) throws IOException {
            super(name);
            selector = Selector.open();
            tasks = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            while (start) {
                try {
                    selector.select();
                    handTask();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (start && iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) {
                            key.cancel();
                            continue;
                        }
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (key.isReadable()) {
                            final Dispatcher dispatcher = (Dispatcher) key.attachment();
                            handRead(channel, dispatcher);
                        } else if (key.isWritable()) {
                            handWrite(channel);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void handTask() {
            Runnable task;
            while (start && (task = tasks.poll()) != null) {
                task.run();
            }
        }

        /**
         * 这里简单的把输出打印出来
         */
        private void handRead(SocketChannel channel, Dispatcher dispatcher) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            try {
                int read = channel.read(buffer);
                if (read == -1) {
                    close(channel);
                } else {
                    // 转换为读
                    buffer.flip();
                    dispatcher.onRead(buffer);
                }
            } catch (IOException e) {
                close(channel);
            }
        }

        private void close(SocketChannel channel) throws IOException {
            System.out.println(channel.getRemoteAddress() + " 断开连接");
            channel.close();
        }

        private void handWrite(SocketChannel channel) {
            // nothing
        }

        /**
         * 把新连接注册到本线程
         */
        public void register(SocketChannel clientChannel) {
            submit(() -> {
                try {
                    clientChannel.configureBlocking(false);
                    final SelectionKey key = clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
                    // 每个连接都绑定一个Dispatcher
                    ChannelContext context = new ChannelContext(clientChannel, codec);
                    key.attach(new Dispatcher(context, codec));
                } catch (Exception e) {
                    try {
                        clientChannel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            selector.wakeup();
        }

        public void submit(Runnable task) {
            tasks.offer(task);
        }

        @Override
        public synchronized void start() {
            start = true;
            super.start();
        }

        public void shutdown() {
            this.start = false;
            this.interrupt();
        }

        public boolean isStart() {
            return start;
        }
    }

    static class SelectNextSelectorThread {
        private final Collection<? extends SelectorThread> threads;
        private Iterator<? extends SelectorThread> iterator;

        public <T extends SelectorThread> SelectNextSelectorThread(Collection<T> threads) {
            this.threads = threads;
            iterator = this.threads.iterator();
        }

        /**
         * 选择下一个SelectorThread，这里为轮询
         *
         * @return SelectorThread
         */
        public SelectorThread nextThread() {
            if (!iterator.hasNext()) {
                iterator = threads.iterator();
            }
            return iterator.next();
        }
    }
}
