package com.wtgroup.ohm.annotation.support;


import com.wtgroup.ohm.bean.HEntityDescriptor;
import org.apache.hadoop.hbase.client.Result;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-24-0:30
 */
public interface ResultMapper<T> {

    /**根据描述者的信息, 随意获取结果中数据, 处理成自己想要的样子.
     * @param result
     * @param descriptor
     * @return
     */
    public T mapping(Result result, HEntityDescriptor<T> descriptor) throws IllegalAccessException;


}
