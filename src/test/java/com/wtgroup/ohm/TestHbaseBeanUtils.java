package com.wtgroup.ohm;

import com.wtgroup.ohm.utils.HbaseBeanUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/5/27 22:35
 */
public class TestHbaseBeanUtils
{
    @Test
    public void fun1() throws IllegalAccessException, NoSuchFieldException {
        Person p = new Person();

        // -- boolean --
        Field f = Person.class.getDeclaredField("sex");

        HbaseBeanUtils.setProperty(p,f, Bytes.toBytes(true));

        System.out.println(p);

        // -- boolean --
        Field f2 = Person.class.getDeclaredField("sex2");

        HbaseBeanUtils.setProperty(p,f2, Bytes.toBytes(true));

        System.out.println(p);



    }


    @Test
    public void fun2() throws NoSuchFieldException, IllegalAccessException, ParseException {
        Person p = new Person();
        p.setName("libai");
        p.setBirthday(new Date());
        Field f = Person.class.getDeclaredField("birthday");
        Class<?> type = f.getType();
        System.out.println(type.getName());

//        HbaseBeanUtils.setProperty(p,f, Bytes.toBytes("2008-8"));


        String dtStr = "2018-6-30";
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        SimpleDateFormat sdf = new SimpleDateFormat();

        Date parse = sdf.parse(dtStr);
        System.out.println(parse);


    }












}
