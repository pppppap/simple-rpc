package com.pppppap.rpc.codec;

/**
 * 编解码器
 *
 * @author liujinrui
 * @since 2019/12/25 10:10
 */
public interface Codec {
    /**
     * 把对象编码
     *
     * @param o 对象
     * @return 字节数组
     */
    byte[] code(Object o);

    /**
     * 从字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @return 对象
     */
    Object decode(byte[] bytes);
}
