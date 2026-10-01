package cn.eyecool.emqx.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 默认Mqtt消息回调处理
 * 
 * @author mawj
 * @date 2021/12/31
 */
public class DefaultMqttMsgEventHandler implements IMqttMsgEventHandler {

    @Override
    public void handleConnectCompleteEvent(boolean reconnect, String serverURI) {

    }

    @Override
    public void handleMessageArrivedEvent(String topic, MqttMessage message) {

    }

    @Override
    public void handleDeliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void handleConnectLostEvent(Throwable cause) {

    }

}
