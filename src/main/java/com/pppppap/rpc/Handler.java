package com.pppppap.rpc;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 17:56
 */
public interface Handler<T> {
    void handle(ChannelContext context, T data) throws Exception;
}
