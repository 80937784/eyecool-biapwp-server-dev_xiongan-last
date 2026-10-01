package cn.eyecool.server.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.common.utils.spring.SpringUtils;

/**
 * Mqtt通信配置
 * 
 * @author admin
 * @date 2020年7月15日
 */
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttClientConfig {

    /**
     * MQTT客户端注册
     */
    @Bean
    public PlatformMqttClientUtil platformMqttClientUtil() {
        return new PlatformMqttClientUtil(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 执行具体的连接成片事件回调处理
                MqttMsgEventHandler handler = SpringUtils.getBean(MqttMsgEventHandler.class);
                handler.handleConnectCompleteEvent(reconnect, serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                // 连接丢失事件回调处理
                MqttMsgEventHandler handler = SpringUtils.getBean(MqttMsgEventHandler.class);
                handler.handleConnectLostEvent(cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 根据主题类型进行不通的业务操作回调处理
                MqttMsgEventHandler handler = SpringUtils.getBean(MqttMsgEventHandler.class);
                handler.handleMessageArrivedEvent(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 消息传递完成事件回调处理
                MqttMsgEventHandler handler = SpringUtils.getBean(MqttMsgEventHandler.class);
                handler.handleDeliveryComplete(token);
            }
        });
    }

}
