package com.pppppap.rpc;

import com.pppppap.rpc.codec.Codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<Type, List<Handler<?>>> handlers = new HashMap<>();
    private ChannelContext context;

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void handle(Object o) {
        final List<Handler<?>> list = handlers.get(o.getClass());
        if (list == null) {
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

    public static void addHandler(Handler handler) {
        if (handler == null) {
            throw new NullPointerException("handler不能为null");
        }
        final Type target = getGeneric(handler);
        handlers.computeIfAbsent(target, k -> new ArrayList<>());
        handlers.get(target).add(handler);
    }

    /**
     * 获取Handler的泛型类型
     *
     * @return 泛型Type
     */
    private static Type getGeneric(Handler<?> handler) {
        for (Type type : handler.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType t = (ParameterizedType) type;
                if (Handler.class.equals(t.getRawType())) {
                    final Type[] types = t.getActualTypeArguments();
                    if (types != null && types.length > 0) {
                        return types[0];
                    }
                }
            }
        }
        return Object.class;
    }
}
