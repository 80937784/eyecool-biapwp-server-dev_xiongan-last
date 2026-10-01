package cn.eyecool.emqx.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT消息事件回调处理器
 * 
 * @author admin
 * @date 2020年7月16日
 */
public interface IMqttMsgEventHandler {

    /**
     * MQTT服务器连接成功事件回调
     * 
     * @param reconnect 是否是重新连接
     * @param serverURI 服务URI地址
     */
    public void handleConnectCompleteEvent(boolean reconnect, String serverURI);

    /**
     * 处理MQTT消息接收事件回调
     * 
     * @param topic 接受的消息主题
     * @param message 接收的消息信息
     */
    public void handleMessageArrivedEvent(String topic, MqttMessage message);

    /**
     * MQTT消息传递完毕事件回调
     * 
     * @param token
     */
    public void handleDeliveryComplete(IMqttDeliveryToken token);

    /**
     * 连接丢失事件回调处理
     * 
     * @param cause
     */
    public void handleConnectLostEvent(Throwable cause);

}
