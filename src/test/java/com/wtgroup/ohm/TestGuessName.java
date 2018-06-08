package com.wtgroup.ohm;

import com.wtgroup.ohm.utils.FieldNameUtil;
import org.junit.Test;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-0:16
 */
public class TestGuessName {

    @Test
    public void fun01(){


            FieldNameUtil.guessFieldFroGetterSetter("getNamexxx", Person.class);


    }


}
