package com.wtgroup.ohm.core;

import com.wtgroup.ohm.annotation.support.ResultMapper;
import com.wtgroup.ohm.annotation.support.SimpleResultMapper;
import com.wtgroup.ohm.bean.Column;
import com.wtgroup.ohm.bean.HEntityDescriptor;
import com.wtgroup.ohm.utils.HBaseUtilSingleton;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-23-1:29
 */
public abstract class HBaseSupport<T> {
    private final Logger log = LoggerFactory.getLogger(HBaseSupport.class);
    //    private T hEntity;
    private HEntityDescriptor<T> hEntityDescriptor;
    private HEntityFactory factory = HEntityFactory.gethEntityFactory();

    /* -- 构造方法 -- */
    public HBaseSupport() {
        //子类继承时, 获取泛型的class, 这里面含有注解信息
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType prtype = (ParameterizedType) type;
            Type[] args = prtype.getActualTypeArguments();
            Class<T> hEntityClass = (Class<T>) args[0];
//            try {
//                hEntity = hEntityClzz.newInstance();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }

            //交个HEntityFactory管理, 生成HEntityDescriptor实例
            hEntityDescriptor = factory.subscribe(hEntityClass).getHEntityDescriptor(hEntityClass);
        }
    }



    /* -- 内置CRUD方法 -- */

    /**
     * 给定rowKey前缀, 模糊匹配.
     * 采用内置的SimpleResultMapper映射结果
     * @param rowKeyPrefix
     * @param startRow
     * @param stopRow
     * @return
     * @version 1.0.0
     * @author Nisus Liu
     * @date 2018-5-26 16:18:56
     */
    public List<T> getByRowKeyPrefix(String rowKeyPrefix, String startRow, String stopRow) {
        return getByRowKeyPrefix(rowKeyPrefix, startRow, stopRow, new SimpleResultMapper<T>());
    }

    /**
     * @param rowKeyPrefix
     * @param startRow
     * @param stopRow
     * @param resultMapper 结果映射器, 为了方便将hbase的Result对象转换为java bean.
     * @return
     */
    public List<T> getByRowKeyPrefix(String rowKeyPrefix, String startRow, String stopRow, ResultMapper<T> resultMapper) {
        Connection conn = HBaseUtilSingleton.getConnection();
        Table table = null;
        List<T> list = new ArrayList<T>();
        ResultScanner scanner = null;
        try {
            long start = System.currentTimeMillis();
            table = conn.getTable(TableName.valueOf(hEntityDescriptor.getTable()));
            PrefixFilter filter = new PrefixFilter(rowKeyPrefix.getBytes());
            Scan scan = new Scan();
            scan.setFilter(filter);
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(stopRow.getBytes());

            Set<Column> columns = hEntityDescriptor.getColumns();
            for (Column col : columns) {
                log.debug("待查询列族: {}, 待查询列名: {}", col.getFamily(), col.getName());
                scan.addColumn(Bytes.toBytes(col.getFamily()), Bytes.toBytes(col.getName()));       //family, column
            }

            log.debug("即将开始scan查询");
            scanner = table.getScanner(scan);

            //结果封装
            Iterator<Result> results = scanner.iterator();
            while (results.hasNext()) {
                Result r = results.next();

                //调用结果映射器将hbase列映射为java bean的属性
               /* ResultMapper<T> mapper = new ResultMapper<T>() {
                    @Override
                    public T mapping(Result result, HEntityDescriptor<T> descriptor) throws IllegalAccessException {
                        //这里补充结果处理的逻辑代码
                        return null;
                    }
                };*/
                T hEntity = (T) resultMapper.mapping(r, hEntityDescriptor);

                list.add(hEntity);
            }
        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    /**
     * 获取所有数据
     * 指定表下多有family的所有字段, 所有行
     * @return  List<T>
     */
    public List<T> getAllRows() {
        Connection conn = HBaseUtilSingleton.getConnection();
        ResultScanner results = null;
        Table table = null;
        List<T> retList = new ArrayList<>();
        try {
            table = conn.getTable(TableName.valueOf(hEntityDescriptor.getTable()));
            Scan scan = new Scan();
            results = table.getScanner(scan);
            //结果集处理
            SimpleResultMapper<T> mapper = new SimpleResultMapper<>();
            for (Result result : results) {
                T hEntity = mapper.mapping(result, hEntityDescriptor);
                retList.add(hEntity);
            }

        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
                results.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return retList;
    }

}
