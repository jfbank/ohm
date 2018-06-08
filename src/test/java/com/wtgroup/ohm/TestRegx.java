package com.wtgroup.ohm;

import org.junit.Test;

/**
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/5/23 13:45
 */
public class TestRegx {



    @Test
    public void fun01(){
        String s = "setNane";
        System.out.println( s.matches("^[sg]et.*") );
    }
}
