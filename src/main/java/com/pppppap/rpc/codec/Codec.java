package com.pppppap.rpc.codec;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 10:10
 */
public interface Codec {
    byte[] code(Object o);

    Object decode(byte[] bytes);
}
