package cn.eyecool.device.constant;

/**
 * MQTT协议通讯需要订阅和发布的主题常量
 * 
 * @author admin
 * @date 2020年7月15日
 */
public interface MqttTopicConstants {

    // 设备信息注册主题通配符表达式
    public static final String SUB_DEVICE_REGIST_TOPIC_PATTERN = "/edge/regist/+";
    // 设备升级结果主题通配符表达式
    public static final String SUB_DEVICE_UPGRADE_RESULT_TOPIC_PATTERN = "/edge/update/result/+";
    // 设备参数下发结果通配符表达式
    public static final String SUB_DEVICE_CONFIG_RESULT_TOPIC_PATTERN = "/edge/business/config/result/+";

    // 设备信息注册结果主题前缀
    public static final String PUB_DEVICE_REGIST_RESULT_TOPIC_PREFIX = "/edge/regist/results/";
    // 设备升级信号主题前缀
    public static final String PUB_DEVICE_UPGRADE_SIGNAL_TOPIC_PREFIX = "/edge/update/";
    // 设备参数下发主题前缀
    public static final String PUB_DEVICE_CONFIG_DISTRIBUTE_TOPIC_PREFIX = "/edge/business/config/";
    // 设备动作主题
    public static final String PUB_DEVICE_ACTION_TOPIC = "/biapwp/edge/action";

}
