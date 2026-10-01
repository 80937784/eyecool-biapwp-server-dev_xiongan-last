package cn.eyecool.common.utils.uuid;

import java.util.Random;

/**
 * ID生成器工具类
 * 
 * @author admin
 */
public class IdUtils {
    /**
     * 获取随机UUID
     * 
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 随机UUID
     */
    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * getSevenRandom 获取7位随机数数 左补零
     * 
     * @param
     * @return java.lang.String
     * @author zfx
     * @since 2022/11/29 14:59
     */
    public static String getSevenRandom() {
        Random r = new Random();
        int x = r.nextInt(9999999) + 1;
        return String.format("%07d", x);
    }

}
