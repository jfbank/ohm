package com.wtgroup.ohm;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wtgroup.ohm.utils.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-19:36
 */
public class TestResourceUtil {
    @Test
    public void fun01(){
        String name = ResourceUtil.get("hbase.zookeeper.quorum");
        System.out.println(name);

        System.out.println(ResourceUtil.getBoolean("sex"));
    }

    @Test
    public void fun02(){
        System.out.println(ResourceUtil.getDouble("age"));
    }

//    @Test
//    public void fun03(){
//        ResourceUtil.on("ohm.properties");
//        ResourceUtil.on("xx.prop");
//        System.out.println(ResourceUtil.get("name"));
//    }

    @Test
    public void fun03(){
        //Assert.assertNotNull("为null",null);
//        String.valueOf(null);
        System.out.println(Boolean.valueOf(null));
    }
}
