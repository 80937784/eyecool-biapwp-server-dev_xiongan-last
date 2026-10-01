package cn.eyecool.common.utils;

/**
 * ID生成工具类
 * 
 * @author admin
 * @date 2019年10月16日
 */
public class IdWorker {

    public final static SnowflakeIdWorker instance = new SnowflakeIdWorker(1, 1);

    private static SnowflakeIdWorker getInstance() {
        return instance;
    }

    public static long getNextLongId() {
        return getInstance().nextId();
    }

    public static String getNextStringId() {
        return String.valueOf(getNextLongId());
    }
}
