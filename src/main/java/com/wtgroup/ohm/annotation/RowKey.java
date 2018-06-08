package com.wtgroup.ohm.annotation;

import java.lang.annotation.*;

/**
 * 指定row key
 * 约定: 字段和方法上都可以使用, 但是方法优先级高于字段
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-23-0:21
 */
@Target({ElementType.METHOD,ElementType.FIELD})       //可以指定多个位置.
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RowKey {
    /**列名, 缺省为字段名.*/
    String name() default "";
}
