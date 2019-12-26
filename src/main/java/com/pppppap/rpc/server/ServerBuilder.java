package com.pppppap.rpc.server;

import com.pppppap.rpc.Dispatcher;
import com.pppppap.rpc.Handler;
import com.pppppap.rpc.RpcException;
import com.pppppap.rpc.codec.Codec;
import com.pppppap.rpc.codec.JdkCodec;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/26 11:48
 */
public class ServerBuilder {
    private int workerThreads = 3;
    private Class<? extends Codec> codecClass = JdkCodec.class;
    private Map<Type, List<Handler<?>>> map = new HashMap<>();

    public static ServerBuilder newBuilder() {
        return new ServerBuilder();
    }

    public NonblockingServer build() {
        try {
            final Codec codec = codecClass.newInstance();
            NonblockingServer server = new NonblockingServer(workerThreads, codec);

            return server;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    public ServerBuilder workerThreads(int n) {
        this.workerThreads = n;
        return this;
    }

    public ServerBuilder codec(Class<? extends Codec> codecClass) {
        this.codecClass = codecClass;
        return this;
    }

    public ServerBuilder addLast(Handler<?> handler) {
        Dispatcher.addHandler(handler);
        return this;
    }

    public Map<Type, List<Handler<?>>> getMap() {
        return map;
    }
}
