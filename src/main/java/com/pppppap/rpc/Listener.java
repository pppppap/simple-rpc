package com.pppppap.rpc;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 17:56
 */
public interface Listener<T> {
    Object handle(T data) throws Exception;
}
