package com.wtgroup.ohm.annotation;

import java.lang.annotation.*;

/**
 * 声明所在pojo为映射hbase的实体类
 *
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-23-0:36
 */
@Target(ElementType.TYPE)       //可以指定多个位置
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HEntity {

    /**约定: 每个entity对应一个table
     * @return
     */
    String table();     //指定表名, 必选

    /**若全表只有一个column family, 可以在这里统一指定一个family, 就很方便.
     * @return
     */
    String defaultFamily() default "defaultFamily";


}
