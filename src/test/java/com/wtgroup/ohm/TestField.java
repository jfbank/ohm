package com.wtgroup.ohm;

import com.wtgroup.ohm.utils.HbaseBeanUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/5/27 21:06
 */
public class TestField {
    @Test
    public void fun1() throws IllegalAccessException {
        Person p = new Person();
        String name = "libai";

        byte[] nameBytes = name.getBytes();

        System.out.println(String.valueOf(nameBytes));


        String nameStr = new String(nameBytes, Charset.forName("UTF-8"));


        try {
            BeanUtils.setProperty(p, "name", nameStr);
            System.out.println(p.getName());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Field[] fields = Person.class.getDeclaredFields();
        for (Field f : fields) {

            Type genericType = f.getGenericType();
            System.out.println(genericType);
            Class<?> type = f.getType();
            System.out.println(type);

        }
    }

    @Test
    public void fun2() throws InvocationTargetException, IllegalAccessException {
        boolean sex = true;
        Date date = new Date();
        System.out.println(date.toString());
        Person p = new Person();
        BeanUtils.setProperty(p, "birthday", date.toString());
        BeanUtils.setProperty(p, "sex", sex);

        System.out.println(p.getAge());
    }

    @Test
    public void fun3() {
        Person p = new Person();

        byte[] sexB = Bytes.toBytes(true);
        byte[] height = Bytes.toBytes(180);
        byte[] birthday = Bytes.toBytes(new Date().toString());

        try {
            Field sexF = p.getClass().getDeclaredField("sex");
            HbaseBeanUtils.setProperty(p, sexF,sexB);


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        System.out.println(p.getAge());
    }
}
