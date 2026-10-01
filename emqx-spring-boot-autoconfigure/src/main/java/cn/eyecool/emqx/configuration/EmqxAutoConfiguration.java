package cn.eyecool.emqx.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.eyecool.emqx.properties.MqttProperties;
import cn.eyecool.emqx.service.DefaultMqttMsgEventHandler;
import cn.eyecool.emqx.service.IMqttMsgEventHandler;
import cn.eyecool.emqx.service.PlatformMqttService;

/**
 * Emqx自动配置
 * 
 * @author mawj
 * @date 2021/12/31
 */
@Configuration
@EnableConfigurationProperties({MqttProperties.class})
@ConditionalOnClass(PlatformMqttService.class)
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = false)
public class EmqxAutoConfiguration {

    @Autowired
    private MqttProperties mqttProperties;

    /**
     * MQTT服务
     */
    @Bean
    @ConditionalOnMissingBean(PlatformMqttService.class)
    public PlatformMqttService platformMqttService() {
        return new PlatformMqttService(mqttProperties, defaultMqttMsgEventHandler());
    }

    /**
     * 消息回调处理
     * 
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(IMqttMsgEventHandler.class)
    public IMqttMsgEventHandler defaultMqttMsgEventHandler() {
        return new DefaultMqttMsgEventHandler();
    }

}
