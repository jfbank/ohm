package com.wtgroup.ohm.bean;

import java.lang.reflect.Field;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-10:10
 */
public class RowKey {
    private String name;        //对应hbase的列名
    private String table;
    private Field field;        //对应javabean的字段名

    public RowKey() {
    }

    public RowKey(String name, String table, Field field) {
        this.name = name;
        this.table = table;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
