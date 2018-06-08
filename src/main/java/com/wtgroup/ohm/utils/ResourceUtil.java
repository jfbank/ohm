package com.wtgroup.ohm.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-16:53
 */
public class ResourceUtil {
private static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);
    private static Properties properties = new Properties();

    static {        //静态属性 -> 静态代码块 -> 代码块 -> 构造方法!
        //log.debug("ohm.properties的绝对路径: {}",ResourceUtil.class.getClassLoader().getResource("ohm.properties").getPath());
        //InputStream ohmConf;
        //加载默认配置文件: ohm.properties
        //ohmConf = ResourceUtil.class.getClassLoader().getResourceAsStream("ohm.properties");

        Resource resource = new ClassPathResource("ohm.properties");
        try {
            log.debug("资源文件名: {}",resource.getFile());
        } catch (IOException e) {
            log.debug("当前环境不支持生成File实例, 资源文件可能在jar包中");
        }
        try {
            properties.load(resource.getInputStream());
            log.debug("加载资源文件后的properties键值对数目: {}",properties.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 默认获取String类型的value
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        Object val = properties.get(key);
        return val==null?null:String.valueOf(val);
    }

    /**
     * Note: 只有根据key拿到"true", 解析结果才会是true. 其他情况都是false
     *
     * @param key
     * @return
     */
    public static Boolean getBoolean(String key) {
        String val = get(key);
        return val==null?null:Boolean.valueOf(val);
    }

    public static Integer getInteger(String key) {
        String val = get(key);
        return val==null?null:Integer.valueOf(val);
    }

    public static Long getLong(String key) {
        String val = get(key);
        return val==null?null:Long.valueOf(val);
    }

    public static Float getFloat(String key) {
        String val = get(key);
        return val==null?null:Float.valueOf(val);
    }

    public static Double getDouble(String key) {
        String val = get(key);
        return val==null?null:Double.valueOf(val);
    }

}
