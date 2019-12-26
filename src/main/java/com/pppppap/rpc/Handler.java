package com.pppppap.rpc;

/**
 * 处理器接口。
 * 泛型T代表想要处理的实体类型，如果为Object则处理所有消息。
 * 注意：此对象是非线程安全的
 *
 * @author liujinrui
 * @since 2019/12/24 17:56
 */
public interface Handler<T> {
    void handle(ChannelContext context, T data) throws Exception;
}
