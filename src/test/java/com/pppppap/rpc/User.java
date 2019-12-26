package com.pppppap.rpc;

import java.io.Serializable;

/**
 * TODO
 *
 * @author liujinrui
 * @since 2019/12/25 10:33
 */
public class User implements Serializable {
    private static final long serialVersionUID = -5200544950401574700L;
    private long id;
    private String name;
    private int age;
    private String addr;

    public User() {
    }

    public User(long id, String name, int age, String addr) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.addr = addr;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", addr='" + addr + '\'' +
                '}';
    }
}
