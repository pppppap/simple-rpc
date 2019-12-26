package com.pppppap.rpc;

import com.pppppap.rpc.codec.Codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责处理消息的读写和转发。
 * 每个SocketChannel都绑定了一个Dispatcher对象。
 *
 * @author liujinrui
 * @since 2019/12/25 11:11
 */
public class Dispatcher {
    // 保存上次接收到而没有没读取的数据
    private ByteBuffer cumulation;
    private Codec codec;
    private ChannelContext context;
    private static final List<HandlerNode> handlers = new ArrayList<>();

    public Dispatcher(ChannelContext context, Codec codec) {
        this.context = context;
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

    /**
     * 解码。前4个字节是消息体的长度。
     *
     * @param in 接收到的数据
     * @return 解码后的对象
     */
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

    /**
     * 调用对应的处理器执行
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void handle(Object o) {
        final List<Handler<?>> list = findHandlers(o.getClass());
        if (list.isEmpty()) {
            String s = MessageFormat.format("warn:{0}没有对应的处理器", o.getClass());
            System.out.println(s);
        } else {
            for (Handler handler : list) {
                try {
                    handler.handle(context, o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加处理器
     */
    public static void addHandler(Handler<?> handler) {
        if (handler == null) {
            throw new NullPointerException("handler不能为null");
        }
        final HandlerNode node = getGeneric(handler);
        handlers.add(node);
    }

    /**
     * 获取Handler的泛型类型
     *
     * @return 泛型Type
     */
    private static HandlerNode getGeneric(Handler<?> handler) {
        Class<?> clazz = null;
        for (Type type : handler.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType t = (ParameterizedType) type;
                if (Handler.class.equals(t.getRawType())) {
                    final Type[] types = t.getActualTypeArguments();
                    if (types != null && types.length > 0) {
                        if (types[0] instanceof ParameterizedType) {
                            clazz = (Class<?>) ((ParameterizedType) types[0]).getRawType();
                        } else {
                            clazz = (Class<?>) types[0];
                        }
                    }
                }
            }
        }
        if (clazz == null) {
            clazz = Object.class;
        }
        return new HandlerNode(clazz, handler);
    }

    private static List<Handler<?>> findHandlers(Class<?> clazz) {
        List<Handler<?>> list = new ArrayList<>();
        for (HandlerNode node : handlers) {
            if (node.clazz.isAssignableFrom(clazz)) {
                list.add(node.handler);
            }
        }
        return list;
    }

    static class HandlerNode {
        private Class<?> clazz;
        private Handler<?> handler;

        public HandlerNode(Class<?> clazz, Handler<?> handler) {
            this.clazz = clazz;
            this.handler = handler;
        }
    }
}
