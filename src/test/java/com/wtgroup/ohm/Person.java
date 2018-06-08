package com.wtgroup.ohm;

import java.util.Date;

/**
 * 测试用的pojo
 *
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-0:20
 */
public class Person {
    private String name;
    private int age;
    private Integer height;
    private Boolean sex;
    private boolean sex2;

    public Date getBirthday() {
        return birthday;
    }

    public Person setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }

    private Date birthday;

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
}
