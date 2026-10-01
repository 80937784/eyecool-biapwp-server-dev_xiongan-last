package cn.eyecool.device.constant;

/**
 * 设备相关Redis-key常量
 *
 * @author mawj
 * @date 2021/06/01
 */
public class RedisKeyConstants {

    /** 设备参数下发日志索引缓存Key(缓存最大设备参数下发日志排序索引) */
    public static final String DEVICE_PARAM_INDEX_CACHE_KEY = "device:param_distribte_index";
    /** 在线设备缓存Key前缀 */
    public static final String ONLINE_DEVICE_KEY_PREFIX = "device:online:";

    public static final String DEVICE_CACHE_PREFIX = "device:info:";

    public static final String NONINDUCTIVE_SOCKET_CACHE_KEY = "noninductive:socket";

    public static final String PERSON_RECOGNISE_CACHE_PREFIX = "noninductive:personrecognise";

    public static final String SUBTREASURY_DEVICE_CACHE_PREFIX = "noninductive:subtreasurydevice";

    public static final String CHANNEL_BUSINESS_CACHE_PREFIX = "noninductive:channelbusiness";
}
