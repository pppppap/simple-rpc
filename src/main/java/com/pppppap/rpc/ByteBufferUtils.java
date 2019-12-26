package com.pppppap.rpc;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 15:01
 */
public class ByteBufferUtils {
    public static byte[] read(ByteBuffer buffer) {
        return read(buffer, buffer.remaining());
    }

    public static byte[] read(ByteBuffer buffer, int length) {
        length = Math.min(buffer.remaining(), length);
        final byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    public static String toString(ByteBuffer buffer) {
        return toString(buffer, StandardCharsets.UTF_8);
    }

    public static String toString(ByteBuffer buffer, Charset charset) {
        final byte[] bytes = read(buffer);
        return new String(bytes, charset);
    }

    /**
     * 对于已经读过数据的ByteBuffer，采用此方法写入新数据
     *
     * @param buffer 已经读过数据的ByteBuffer
     * @param bytes  大小不要超过buffer的容量
     */
    public static void write(ByteBuffer buffer, byte[] bytes) {
        if (buffer.limit() == buffer.capacity()) {
            buffer.put(bytes);
        } else {
            int readIndex = buffer.position();
            // position跳有效数据的最后
            buffer.position(buffer.limit());
            // limit设置为capacity
            buffer.limit(buffer.capacity());
            buffer.put(bytes);
            // limit设置为最大有效数据
            buffer.limit(buffer.position());
            // position回到最初
            buffer.position(readIndex);
        }
    }

    /**
     * 按顺序把两个ByteBuffer合并成一个ByteBuffer。
     * b1的内容在b2的前面
     *
     * @return 新的ByteBuffer
     */
    public static ByteBuffer merge(ByteBuffer b1, ByteBuffer b2) {
        ByteBuffer newBuffer;
        if (b1.isDirect() && b2.isDirect()) {
            newBuffer = ByteBuffer.allocateDirect(b1.remaining() + b2.remaining());
        } else {
            newBuffer = ByteBuffer.allocate(b1.remaining() + b2.remaining());
        }
        write(newBuffer, read(b1));
        write(newBuffer, read(b2));
        return newBuffer;
    }

    public static byte[] intToBits(int n) {
        final byte[] bytes = new byte[4];
        bytes[3] = (byte) (n & 0xFF);
        bytes[2] = (byte) ((n >>> 8) & 0xFF);
        bytes[1] = (byte) (n >> 16);
        bytes[0] = (byte) (n >> 24);
        return bytes;
    }

    public static int bitsToInt(byte[] bytes) {
        return (bytes[3] & 0xFF)
                | ((bytes[2] & 0xFF) << 8)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[0] & 0xFF) << 24);
    }
}
