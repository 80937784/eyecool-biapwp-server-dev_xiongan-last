package cn.eyecool.emqx.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * MQTT使用属性配置
 * 
 * @author mawj
 * @date 2021/12/31
 */
@Data
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /** 是否启用 */
    private Boolean enabled = false;
    /** proto协议版本 */
    private String protoVersion = "proto1.0";
    /** 异步处理订阅消息 */
    private Boolean msgAsync = false;
    /** 异步处理订阅消息线程池大小 */
    private Integer msgThreadPoolSize = 4;
    /** Emqx服务连接属性 */
    private EmqxProperties emqx;

}
