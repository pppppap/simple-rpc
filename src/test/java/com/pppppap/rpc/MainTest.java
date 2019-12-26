package com.pppppap.rpc;

import com.pppppap.rpc.server.NonblockingServer;
import com.pppppap.rpc.server.ServerBuilder;

import java.io.Serializable;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/24 18:19
 */
public class MainTest {
    public static void main(String[] args) {
        NonblockingServer server = ServerBuilder.newBuilder()
//                .addLast(new MyHandler())
                .addLast(new Handler<Object>() {
                    @Override
                    public void handle(ChannelContext context, Object data) throws Exception {
                        System.out.println("xixi");
                    }
                })
                .build();
        server.listen(1234);
    }

    static class MyHandler implements Serializable, TestInterface<Object>, Handler<User> {
        @Override
        public void handle(ChannelContext context, User data) throws Exception {
            System.out.println("接收到[" + data.getName() + "]的数据");
        }
    }
}
