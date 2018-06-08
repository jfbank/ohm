package com.wtgroup.ohm.annotation;

import java.lang.annotation.*;

/**
 * 指定列字段映射
 *
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-23-0:47
 */
@Target({ElementType.METHOD,ElementType.FIELD})       //可以指定多个位置
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    /**列名, 缺省为字段名.*/
    String name() default "";
    /**所在的column family, 缺省为全局指定的默认列族名,
     * @see HEntity*/
    String family() default "";

}
