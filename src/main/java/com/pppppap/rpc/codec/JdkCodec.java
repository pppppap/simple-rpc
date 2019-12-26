package com.pppppap.rpc.codec;

import com.pppppap.rpc.RpcException;

import java.io.*;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 10:09
 */
public class JdkCodec implements Codec {
    @Override
    public byte[] code(Object o) {
        if (o == null) {
            return new byte[0];
        }
        if (!(o instanceof Serializable)) {
            throw new RpcException("使用Jdk序列化方式，对象必须实现Serializable接口");
        }
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             final ObjectOutputStream stream = new ObjectOutputStream(outputStream)) {
            stream.writeObject(o);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public Object decode(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        try (final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
             final ObjectInputStream stream = new ObjectInputStream(arrayInputStream)) {
            return stream.readObject();
        } catch (Exception e) {
            throw new RpcException("序列化失败", e);
        }
    }
}
