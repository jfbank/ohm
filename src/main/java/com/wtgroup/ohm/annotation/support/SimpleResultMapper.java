package com.wtgroup.ohm.annotation.support;

import com.wtgroup.ohm.bean.Column;
import com.wtgroup.ohm.bean.HEntityDescriptor;
import com.wtgroup.ohm.utils.HbaseBeanUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 结果封装处理器
 *
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/5/23 15:57
 */
public class SimpleResultMapper<T> implements ResultMapper<T> {

    public SimpleResultMapper() {}

    @Override
    public T mapping(Result result, HEntityDescriptor<T> descriptor) throws IllegalAccessException {
        Set<Column> columns = descriptor.getColumns();
        //获取实例(多例的)
        T hEntity = descriptor.getInstance();
        Field rowKeyField = descriptor.getRowKey().getField();
        rowKeyField.setAccessible(true);
//        rowKeyField.set(hEntity, Bytes.toString(result.getRow()));
        HbaseBeanUtils.setProperty(hEntity,rowKeyField,result.getRow());
        for (Column column : columns) {
            Cell cell = result.getColumnLatestCell(column.getFamily().getBytes(), column.getName().getBytes());
            byte[] cellVal = CellUtil.cloneValue(cell);
            Field field = column.getField();
            field.setAccessible(true);
            //根据java bean字段类型, 将byte[]转换成对应的类型  , 不同类型分别处理 类型要匹配, 否则, 报`参数非法`异常
            //?Hbase支持日期类型吗? 日期类型转换比价麻烦点?
            //TODO 待测试完善, 日期类型没有尝试
            HbaseBeanUtils.setProperty(hEntity,field,cellVal);
        }

        return hEntity;
    }
}



