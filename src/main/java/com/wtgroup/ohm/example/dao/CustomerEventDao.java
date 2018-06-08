package com.wtgroup.ohm.example.dao;

import com.wtgroup.ohm.core.HBaseSupport;
import com.wtgroup.ohm.example.entity.CustomerEvent;

/**继承HBaseSupport并制定hbase库表对应的实体类的泛型, 即可使用内置的CRUD方法.
 * 关键是能够自动帮你将查询结果封装成java bean.
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-23:38
 */
public class CustomerEventDao extends HBaseSupport<CustomerEvent>{

}
