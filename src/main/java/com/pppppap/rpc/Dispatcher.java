package com.pppppap.rpc;

import com.pppppap.rpc.codec.Codec;

import java.nio.ByteBuffer;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 11:11
 */
public class Dispatcher {
    // 保存上次接收到而没有没读取的数据
    private ByteBuffer cumulation;
    private Codec codec;

    public Dispatcher(Codec codec) {
        this.codec = codec;
    }

    public void onRead(ByteBuffer in) {
        if (cumulation == null || !cumulation.hasRemaining()) {
            cumulation = in;
        } else {
            // 合并上次剩余的数据
            cumulation = ByteBufferUtils.merge(cumulation, in);
            cumulation.flip();
        }
        // 只读
        cumulation = cumulation.asReadOnlyBuffer();
        while (cumulation.hasRemaining()) {
            int oldPosition = cumulation.position();
            final Object o = callDecode(cumulation);
            if (o != null) {
                handle(o);
            }
            // 没有读取数据就可以退出循环了
            if (oldPosition == cumulation.position()) {
                break;
            }
        }
    }

    protected Object callDecode(ByteBuffer in) {
        if (in.remaining() < 4) return null;
        in.mark();
        final int len = in.getInt();
        if (in.remaining() < len) {
            in.reset();
            return null;
        }
        final byte[] bytes = ByteBufferUtils.read(in, len);
        return codec.decode(bytes);
    }

    protected void handle(Object o) {
        System.out.println(o);
    }
}
