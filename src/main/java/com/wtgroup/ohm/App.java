package com.wtgroup.ohm;

import com.wtgroup.ohm.example.dao.CustomerEventDao;
import com.wtgroup.ohm.example.entity.CustomerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**`java -jar 包名`即可执行这里的测试方法.
 * Note: 例子的代码仅供参考, 实际运行需要hbase环境支持.
 * @author Nisus-Liu
 * @version 1.0.0
 * @email liuhejun108@163.com
 * @date 2018-05-26-23:43
 */
//@SpringBootApplication
public class App {
private static final Logger log = LoggerFactory.getLogger(App.class);
//    public static void main(String[] args) {
//        SpringApplication.run(App.class, args);

        //运行测试方法
//        example();
//    }

    public static void example() {
        String certno = "211221198903200310";       //TODO
        String prefix;
        String md5Certno = md5(certno);
        log.debug("身份证号: {}, MD5: {}", certno, md5Certno);

        String md5Prefix = md5Certno.substring(md5Certno.length() - 5);
        log.debug("身份证号MD5后5位: {}", md5Prefix);


        prefix = md5Prefix+"-" + certno + "-";

        log.debug("row key前缀: {}", prefix);

        //测试根据前缀查询的方法
        CustomerEventDao dao = new CustomerEventDao();
        List<CustomerEvent> res = dao.getByRowKeyPrefix(prefix, prefix, nextString(prefix));
        System.out.println(res.size());
    }


    /* -- 工具方法 -- */

    /**
     * 使用md5的算法进行加密
     */
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
        // 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }


    /**根据ASCII码获取下一个字符串
     * @param current
     * @return
     * @date 2018-5-22 18:38:27
     */
    public static String nextString(String current)
    {
        Integer ascii = 0;
        StringBuffer sbu = new StringBuffer();
        char[] chars = current.toCharArray();
        char last = chars[chars.length - 1];
        chars[chars.length-1] = (char) ((int)last+1);
        for (char aChar : chars) {
            sbu.append(aChar);
        }

        return sbu.toString();
    }


}
