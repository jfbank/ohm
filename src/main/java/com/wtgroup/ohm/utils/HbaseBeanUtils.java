package com.wtgroup.ohm.utils;

import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2018/5/27 21:55
 */
public class HbaseBeanUtils {
    private static final String UTF8_ENCODING = "UTF-8";
    private static final Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);

    /**1. 仅支持基本数据类型及其包装类;
     * 2. 可以没有setter
     * 3. 暂不支持Date类型, 用Long/String
     * @param object
     * @param field
     * @param value
     * @throws IllegalAccessException
     */
    public static void setProperty(Object object, Field field, byte[] value) throws IllegalAccessException {
        if (value == null) {
            return;
        }
        //boolean特殊, hbase的规则: -1==true, 0==false, 但是在转码的时候出现乱码


//        String valStr = Bytes.toString(value);
        //大部分格式都可以用字符转过去
        String valStr = new String(value, UTF8_CHARSET);

        field.setAccessible(true);
        Class<?> type = field.getType();
        String typeName = type.getName();

        //
        if (typeName.matches(".*String$")) {
            field.set(object, valStr);
        } else if (typeName.matches(".*((Integer)|(int))$")) {
            field.set(object, Integer.valueOf(valStr));
        } else if (typeName.matches(".*((Long)|(long))$")) {
            field.set(object, Long.valueOf(valStr));
        } else if (typeName.matches(".*((Short)|(short))$")) {
            field.set(object,Short.valueOf(valStr));
        } else if (typeName.matches(".*((Float)|(float))$")) {
            field.set(object,Float.valueOf(valStr));
        } else if (typeName.matches(".*((Double)|(double))$")) {
            field.set(object,Double.valueOf(valStr));
        } else if (typeName.matches(".*((Boolean)|(boolean))$")) {
            if (value[0] == -1) {
                field.set(object, true);
            } else {
                field.set(object,false);
            }
        } else if (typeName.matches(".*((Byte)|(byte))$")) {
            field.set(object,Byte.valueOf(valStr));
        } else if (typeName.matches(".*((Character)|(char))$")) {
            field.set(object, valStr.toCharArray()[0]);
        } else if (typeName.matches(".*(BigDecimal)$")) {
            field.set(object, Bytes.toBigDecimal(value));
        }



    }


}
