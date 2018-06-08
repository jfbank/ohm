package com.wtgroup.ohm.annotation.support;

import com.wtgroup.ohm.annotation.HEntity;
import com.wtgroup.ohm.annotation.RowKey;
import com.wtgroup.ohm.bean.Column;
import com.wtgroup.ohm.utils.FieldNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 内含hbase的实体类, 和解析注解后的表,列族,列等信息
 *
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-23-1:43
 */
public class HEntityAnnParser<T> {
    private final Logger log = LoggerFactory.getLogger(HEntityAnnParser.class);
    private Class<T> hEntityClass;
    private com.wtgroup.ohm.bean.RowKey rowKey;
    private String table;
    private String defaultFamily;
    private Set<Column> columns = new LinkedHashSet<>();


    public HEntityAnnParser(Class<T> hEntityClass) {
        //判断是否还有 @HEntity注解
        if (!hasHEntityAnt(hEntityClass)) {
            throw new RuntimeException(hEntityClass.getName() + "@" + Integer.toHexString(hEntityClass.hashCode()) + "缺少@HEntity类注解");
        }

        this.hEntityClass = hEntityClass;

        //解析注解并丰富属性值
        parse();

    }


    private boolean hasHEntityAnt(Class<T> hEntityClzz) {
        return hEntityClzz.isAnnotationPresent(HEntity.class);

    }


    //解析注解, 获取 columns 等信息
    private void parse() {
        //解析类注解, 得到表名, 默认列族名
        parseTableAndDefaultFamilyName();

        //解析row key
        //解析普通列(属性or方法)
        parseRowKeyAndColumns();
    }

    private void parseRowKeyAndColumns() {

        //解析所有 字段 的注解
        Field[] declaredFields = hEntityClass.getDeclaredFields();
        for (Field f : declaredFields) {
            f.setAccessible(true);
            if (f.isAnnotationPresent(RowKey.class)) {
                //解析 row key
                this.rowKey = parseRowKey(f);
            }

            if (f.isAnnotationPresent(com.wtgroup.ohm.annotation.Column.class)) {
                //解析普通字段
                com.wtgroup.ohm.bean.Column column = parseColumn(f);
                if (column != null) {
                    this.columns.add(column);
                }
            }

        }


        //解析所有 方法 的注解
        Method[] declaredMethods = hEntityClass.getDeclaredMethods();
        for (Method m : declaredMethods) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(RowKey.class)) {
            //解析 row key
            this.rowKey = parseRowKey(m);
            }

            if (m.isAnnotationPresent(com.wtgroup.ohm.annotation.Column.class)) {
                //解析普通字段
                Column column = parseColumn(m);
                if (column != null) {
                    columns.add(column);            //方法的优先级高于字段, 字段有重复的会被替换
                }
            }

        }

    }


    private com.wtgroup.ohm.bean.Column parseColumn(AccessibleObject fieldOrMethod) {
        fieldOrMethod.setAccessible(true);

        String fieldName = null;
        Field field = null;
        if (fieldOrMethod instanceof Field) {
            field = (Field) fieldOrMethod;
            fieldName = field.getName();
        }
        if (fieldOrMethod instanceof Method) {
            //判断是否是getter/setter方法, 仅支持getter/setter方法注解Column
            Method m = (Method) fieldOrMethod;
            if (!m.getName().matches("^[sg]et.*")) {
                log.warn("仅支持getter/setter方法注解Column");
                return null;
            }

            //根据getter/setter推测字段
            field = FieldNameUtil.guessFieldFroGetterSetter(m.getName(), hEntityClass);
            fieldName = field.getName();

        }


        //解析普通字段
        com.wtgroup.ohm.annotation.Column columnAnt;
        if ((columnAnt = fieldOrMethod.getAnnotation(com.wtgroup.ohm.annotation.Column.class)) != null) {
            String colName = columnAnt.name(), family = columnAnt.family();
            if ("".equals(colName) && fieldName != null) {
                //未指定列名时, 以字段名作为列名
                colName = fieldName;

            }
            if ("".equals(family)) {
                //未指定列族名时, 以默认列族名为列族名
                if (defaultFamily == null) {
                    throw new RuntimeException("字段" + fieldName + "未指所属列族, 缺省使用默认列族, 但类注解未指定默认列族");
                }

                family = defaultFamily;
            }

            if (field == null) {
                log.warn("无法建立列名{}和javabean字段间的映射关系: 获取Field失败", colName);
            }else if(field.getType().getName().matches(".*(Date)$")){
                //暂不支持Date类型, 麻烦
                log.warn("暂不支持使用Date类型字段, 请根据hbase表中日期格式选择Long/String接收数据. 本次将忽略此字段的数据封装");
                return null;
            } else {
                //创建 Column
                log.debug("解析出: column name: {}, column family: {}, Filed: {}",new Object[]{colName,family,field});
                return new com.wtgroup.ohm.bean.Column(colName, family, table, field);
            }


        }
        return null;
    }


    private com.wtgroup.ohm.bean.RowKey parseRowKey(AccessibleObject fieldOrMethod) {
        fieldOrMethod.setAccessible(true);
        Field field = null;

        if (fieldOrMethod instanceof Field) {
            field = (Field) fieldOrMethod;
            //根据getter获取属性值
//            try {
//                PropertyDescriptor des = new PropertyDescriptor(f.getName(), hEntityClass);
//                return (String) des.getReadMethod().invoke(hEntity);
//            } catch (IntrospectionException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }

        }

        //若字段没有指定@RowKey, 则寻找方法上的

        //若是getter方法直接调用次方法得到row key
        if (fieldOrMethod instanceof Method) {
            Method m = (Method) fieldOrMethod;
//            if (!m.getName().startsWith("get")) {
//                log.warn("请在getter上使用@RowKey, 或者指定某个字段为row key, 即在该字段上使用@RowKey注解");
//                return null;
//            }
            if (!m.getName().matches("^[sg]et.*")) {
                log.warn("仅支持getter/setter方法注解@RowKey");
                return null;
            }

            //根据getter/setter推测字段
            field = FieldNameUtil.guessFieldFroGetterSetter(m.getName(), hEntityClass);

        }


        if (field != null) {
            RowKey rowKeyAnt = fieldOrMethod.getAnnotation(RowKey.class);
            if (rowKeyAnt == null) {
                return null;
            }
            String rowKeyName = rowKeyAnt.name();
            if ("".equals(rowKeyName)) {
                //字段名作为RowKey名
                rowKeyName = field.getName();
            }
            log.debug("解析出: row key name: {}, Field: {}",rowKeyName,field);
            return new com.wtgroup.ohm.bean.RowKey(rowKeyName, table, field);
        }


        return null;
    }

    private HEntityAnnParser parseTableAndDefaultFamilyName() {
        String[] tf = new String[2];
        //构造方法已经拦截了没有注解的情况
        HEntity hEntiyAnt = (HEntity) hEntityClass.getAnnotation(HEntity.class);
        this.table = hEntiyAnt.table();
        this.defaultFamily = hEntiyAnt.defaultFamily();

        return this;
    }


    public String getTable() {
        return table;
    }

    public HEntityAnnParser setTable(String table) {
        this.table = table;
        return this;
    }

    public String getDefaultFamily() {
        return defaultFamily;
    }

    public HEntityAnnParser setDefaultFamily(String defaultFamily) {
        this.defaultFamily = defaultFamily;
        return this;
    }

    public Set<com.wtgroup.ohm.bean.Column> getColumns() {
        return columns;
    }

    public HEntityAnnParser setColumns(Set<com.wtgroup.ohm.bean.Column> columns) {
        this.columns = columns;
        return this;
    }


    public com.wtgroup.ohm.bean.RowKey getRowKey() {
        return rowKey;
    }

    public void setRowKey(com.wtgroup.ohm.bean.RowKey rowKey) {
        this.rowKey = rowKey;
    }

    public Class<T> gethEntityClass() {
        return hEntityClass;
    }

    public void sethEntityClass(Class<T> hEntityClass) {
        this.hEntityClass = hEntityClass;
    }
}

