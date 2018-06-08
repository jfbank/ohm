package com.wtgroup.ohm.utils;

import com.wtgroup.ohm.annotation.support.HEntityAnnParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-25-23:36
 */
public class FieldNameUtil {
    private static final Logger log = LoggerFactory.getLogger(HEntityAnnParser.class);

    public static Field guessFieldFroGetterSetter(String methodName, Class clz) {
        int guessTime = 1;
        NoSuchFieldException ex = null;
        Field declaredField = null;
        //暂定尝试2次
        while (guessTime < 3) {
            //log.info("第{}次猜测", guessTime);
            String guessedName = convertGetterSetterToFieldName(methodName, guessTime);
            //验证名字是否正确
            if (guessedName != null) {
                try {
                    declaredField = clz.getDeclaredField(guessedName);
                } catch (NoSuchFieldException e) {
                    log.info("第{}次猜测失败, 正在重试...", guessedName);
                    ex = e;
                    guessTime++;
                    continue;
                }
                //若正确, 退出循环, 并放回结果
                return declaredField;
            }
        }

        //报异常了, 说明获取的方法名很可能不正确, 导致 NoSuchFieldException
        log.error("根据getter/setter猜测属性名失败, 请确保使用了标准的getter/setter");
        ex.printStackTrace();
        return null;
    }


    /**
     * getter/setter方法名 -> 对应的字段名
     * TODO 规则可能不准
     *
     * @param methodName
     * @return
     */
    private static String convertGetterSetterToFieldName(String methodName, int guessTime) {
        if (!methodName.matches("^[gs]et.*")) {
            log.warn("仅支持根据getter/setter猜测字段名. return null");
            return null;
        }

        //切掉开头3个字母 get/set
        String f = methodName.substring(3);
        //首字母转为小写即可
        //前提是变量名不是 $ _ 开头
        char[] cs = f.toCharArray();
        if (cs.length == 0) {
            return null;        //切除get/set后没有了字符
        } else if (cs.length == 1) {
            //第一次尝试: 一个字符时直接转小写
            if (guessTime == 1 && isUpperCase(cs[0])) {
                cs[0] += 32;
            }
            //以后尝试时, 不管大小写
        } else {
            //第一次尝试: 第二个字母是大写字母时, 首字母若是大写字母也不转小写
            //i.e.第二个字母不是大写, 首字母大写时才转小写
            if (guessTime == 1 && (cs[1] > 90 || cs[1] < 65) && (isUpperCase(cs[0]))) {
                cs[0] += 32;        //首字母转小写

            }
            //第二次尝试: 首字母一律转小写
            if (guessTime == 2) {
                if (isUpperCase(cs[0])) {
                    cs[0] += 32;
                }
            }
        }


        f = String.valueOf(cs);
        return f;
    }


    private static boolean isUpperCase(char ch) {
        if (ch <= 90 && ch >= 65) {
            return true;
        }
        return false;
    }

    private static boolean isLowerCase(char ch) {
        if (ch <= 122 && ch >= 97) {
            return true;
        }
        return false;
    }

}
