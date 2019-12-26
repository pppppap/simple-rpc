package com.pppppap.rpc.codec;

import com.pppppap.rpc.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 10:32
 */
public class JdkCodecTest {
    private User user;
    private Codec codec;

    @Before
    public void beforeClass() throws Exception {
        user = new User(12345, "cxk", 12, "beijing");
        codec = new JdkCodec();
    }

    @Test
    public void test001() {
        final byte[] bytes = codec.code(user);
        final User newUser = (User) codec.decode(bytes);
        Assert.assertEquals(user.getId(), newUser.getId());
        Assert.assertEquals(user.getAge(), newUser.getAge());
        Assert.assertEquals(user.getAddr(), newUser.getAddr());
        Assert.assertEquals(user.getName(), newUser.getName());
    }
}
