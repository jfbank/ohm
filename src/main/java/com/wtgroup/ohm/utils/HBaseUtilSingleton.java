package com.wtgroup.ohm.utils;


//import com.jfbank.bigdata.constant.Constant;
//import com.jfbank.bigdata.exception.GlobalConf;
//import com.jfbank.bigdata.exception.GlobalCustomException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.RegionSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Nisus Liu
 * @version 0.0.0
 * @email liuhejun108@163.com
 * @date 2018/5/17 12:44
 */
public class HBaseUtilSingleton {
//    public static final String HBASE_ZOOKEEPER_QUORUM = "offlinenode3,offlinenode4,offlinenode5";
    public static final int HBASE_TABLE_REGION_NUM = ResourceUtil.getInteger("hbase.table.region.num")==null?10:ResourceUtil.getInteger("hbase.table.region.num");
    public static final String COMMA = ",";
    public static final String HIVE_SEP = "\\x01";
    public static final String DEFAULT_COLUMNFAMILY = ResourceUtil.get("default_columnfamily")==null?"defaultColumnFamily":ResourceUtil.get("default_columnfamily");

    private static Logger LOG = LoggerFactory.getLogger(HBaseUtilSingleton.class);
    private static Configuration conf = null;
    private static Connection conn = null;
    private static ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private static Lock wlock = rwlock.writeLock();

    public static Connection getConnection() {
        if (conn == null || conn.isClosed()) {
            try {
                wlock.lock();
                if (conn == null || conn.isClosed()) {
                    conf = getHBaseConfig();
                    conn = ConnectionFactory.createConnection(conf);
                    LOG.info("HBase connection is created:::");
                }
            } catch (IOException e) {
                LOG.error("HBase create conn error...\n{}", e);
            } finally {
                wlock.unlock();
            }


        }
        return conn;
    }

    public static Configuration getHBaseConfig() {
        if (conf == null) {
            try {
                wlock.lock();
                if (conf == null) {
                    conf = HBaseConfiguration.create();
                    conf.set("hbase.zookeeper.quorum", ResourceUtil.get("hbase.zookeeper.quorum"));
                    conf.set("hbase.client.keyvalue.maxsize", ResourceUtil.get("hbase.client.keyvalue.maxsize"));

                }
            } catch (Exception e) {
                LOG.error("Getting config error:", e);
            } finally {
                wlock.unlock();
            }
        }
        return conf;
    }

    public static boolean closeConnection() {
        if (conn != null && !conn.isClosed()) {
            try {
                conn.close();
                LOG.info("hbase connection close success:::");
                return true;
            } catch (IOException e) {
                LOG.error("hbase connection close error:::", e);
                return false;
            }
        }
        return true;
    }

    /**
     * synchronized can only solve concurrency at thread level, in distributed system multiple processes
     * unavoidably will face concurrent operation, to avoid multiple processed concurrency you have to
     * implement distributed lock via zookeeper e.g.
     *
     * @param tableName
     * @param family
     */
    synchronized public static void createTable(String tableName, String family) {
        try {
            Admin admin = getConnection().getAdmin();
            if (!admin.tableExists(TableName.valueOf(tableName))) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
                HColumnDescriptor columnDescriptor = new HColumnDescriptor(family);
                columnDescriptor.setCompressionType(Compression.Algorithm.SNAPPY);      ////设置压缩算法
                tableDescriptor.addFamily(columnDescriptor);

                //分区  ?没有找到jar包?  若要创建表, 这个很重要
                byte[][] splits = new RegionSplitter.HexStringSplit().split(HBASE_TABLE_REGION_NUM);

//                admin.createTable(tableDescriptor, splits);
                admin.createTable(tableDescriptor);
                LOG.info("创建表 " + tableName + " 成功!");
            }
        } catch (Exception e) {
            LOG.error("Creating table {} error ", tableName, e);
//            throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
            //throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
        }
    }

    /**
     * 获取指定 table 的 columnFamily 下的 若干列
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param fields    多个字段用 ','分隔
     * @return
     */
    public static String getFieldsVal(String tableName, String rowKey, String family,
                                      String fields) {
        long start = System.currentTimeMillis();
        Connection conn = getConnection();
        StringBuilder sb = new StringBuilder();
        Table table = null;
        if (StringUtils.isBlank(fields)) {
            return "";
        }
        try {
            String[] fs = fields.split(COMMA);

            table = conn.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));

            for (String f : fs) {
                System.out.println("将要查询字段: " + f);
                get.addColumn(Bytes.toBytes(family), Bytes.toBytes(f));
            }

            Result rst = table.get(get);

            for (int i = 0; i < fs.length; i++) {
                String f = fs[i];
                String col_val = Bytes.toString(rst.getValue(Bytes.toBytes(family), Bytes.toBytes(f)));
                LOG.debug("结果: " + f + "->" + col_val);
                //将个字段值拼接成字符串
                sb.append(col_val);
                if (i < fs.length - 1) {
                    sb.append(COMMA);
                }
            }
            ;
            long end = System.currentTimeMillis();
            LOG.debug("查询耗时: {}ms", end - start);


            LOG.debug("拼接后的字段值: {}, 分隔符: {}", sb.toString(), COMMA);
            return sb.toString();

        } catch (Exception e) {
            LOG.error(e.toString());
//            return Constant.NULL;
            return null;
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    LOG.error("Closing table {} error ", tableName, e);
//                    throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
                }
            }
        }


    }

    /**
     * 对指定表下列族的若干列插入或更新
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param data
     */
    public static void insertOrUpdate(String tableName, String rowKey, String family,
                                      Map<String, String> data) {

        long startTime = System.currentTimeMillis();

        Connection conn = getConnection();
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            for (String key : data.keySet()) {
                String value = String.valueOf(data.get(key));
                if (value == null) {
                    continue;
                } else {
                    put.addColumn(Bytes.toBytes(family), Bytes.toBytes(key), Bytes.toBytes(value));
                }
            }
            if (put.size() > 0) {
                table.put(put);
                LOG.debug("Table {} insert or update with rowkey {} succeeded", tableName, rowKey);
            }
        } catch (TableNotFoundException | RetriesExhaustedWithDetailsException e) {
            // If table not exists, create it
            LOG.warn("Table {} not found, it will be created soon.", tableName);
            createTable(tableName, family);
            // run the method again
            insertOrUpdate(tableName, rowKey, family, data);
        } catch (Exception e) {
            LOG.error("Insert or update data error, table: {} ,pk :{}", tableName, rowKey);
            LOG.error("Insert or update data error, error:", e);
//            throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    LOG.error("Closing table {} error ", tableName, e);
//                    throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
                }
            }

            long endTime = System.currentTimeMillis();
            LOG.info("hbase success.spend time(mm):" + (endTime - startTime));
        }

    }

    /**
     * @param tableName  表名
     * @param rowKeyName rowkey在Map中的Key名称, 将作为hbase中rowKey
     * @param batchNo    批量提交的批次号：可以用transNo
     * @param family     列族名
     * @param datas      列表
     */
    public static void insertOrUpdateBatch(String tableName, String rowKeyName, String batchNo, String family,
                                           List<Map<String, String>> datas) {
        long startTime = System.currentTimeMillis();
        Connection conn = getConnection();
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            List<Put> list = new ArrayList<>();
            for (Map<String, String> data : datas) {
                String rk = data.get(rowKeyName);       //指定数据集中某一键的值作为 rowKey
                Put put = new Put(Bytes.toBytes(rk));
                for (String key : data.keySet()) {
//                    if (!key.equals(rowKeyName)) {
                    if (!rowKeyName.equals(key)) {      //map的key可以是null, 避免空指针异常 && 排除指定为rowKey的字段(避免重复存储)
                        String value = data.get(key);
                        if (StringUtils.isNotBlank(value)) {        //?""要不要存?
                            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(key), Bytes.toBytes(value));
                        }
                    }
                }
                list.add(put);
            }

            if (list.size() > 0) {
                table.put(list);
                LOG.info("Table {},batch={} insert or update with num :{}  succeeded", new String[]{tableName, batchNo, datas.size() + ""});
            }
        } catch (TableNotFoundException | RetriesExhaustedWithDetailsException e) {
            // If table not exists, create it
            LOG.warn("Table {} not found, it will be created soon.", tableName);
            createTable(tableName, family);
            // run the method again
            insertOrUpdateBatch(tableName, rowKeyName, batchNo, family, datas);
        } catch (Exception e) {
            LOG.error("Insert or update data error, table: {} ,pk :{}", tableName, batchNo);
            LOG.error("Insert or update data error, error:", e);
//            throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    LOG.error("Closing table {} error ", tableName, e);
//                    throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        LOG.info("hbase batch success.spend time(mm):" + (endTime - startTime));
    }


    public static void deleteRowByRowkey(String tableName, String rowKey) {
        Connection conn = getConnection();
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Delete del = new Delete(Bytes.toBytes(rowKey));
            table.delete(del);
            LOG.debug("Table {} delete with rowkey {} succeeded", tableName, rowKey);

        } catch (TableNotFoundException | RetriesExhaustedWithDetailsException e) {
            // If table not exists, create it
            LOG.warn("Table {} not found, it will be created soon, with default column family name:{}", tableName,DEFAULT_COLUMNFAMILY);
            createTable(tableName, DEFAULT_COLUMNFAMILY);
            // run the method again
            deleteRowByRowkey(tableName, rowKey);
        } catch (Exception e) {
            LOG.error("Deleting data error, table: {} ,pk :{}", tableName, rowKey);
            LOG.error("error:", e.getMessage());
//            throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (Exception e) {
                    LOG.error("Closing table {} error ", tableName, e);
//                    throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR, e);
                }
            }
        }

    }


}

