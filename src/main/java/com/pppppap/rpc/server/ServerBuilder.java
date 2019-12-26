package com.pppppap.rpc.server;

import com.pppppap.rpc.Dispatcher;
import com.pppppap.rpc.Handler;
import com.pppppap.rpc.RpcException;
import com.pppppap.rpc.codec.Codec;
import com.pppppap.rpc.codec.JdkCodec;

/**
 * 通过此类调整参数构造服务器
 *
 * @author liujinrui
 * @since 2019/12/26 11:48
 */
public class ServerBuilder {
    /** 默认处理读写、执行业务的线程为3个 */
    private int workerThreads = 3;
    /** 默认为{@link JdkCodec} ,可通过实现{@link Codec}接口实现自定义的解编码器 */
    private Class<? extends Codec> codecClass = JdkCodec.class;

    public static ServerBuilder newBuilder() {
        return new ServerBuilder();
    }

    public NonblockingServer build() {
        try {
            final Codec codec = codecClass.newInstance();
            return new NonblockingServer(workerThreads, codec);
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
}
