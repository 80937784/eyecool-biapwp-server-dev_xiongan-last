package cn.eyecool.server.mqtt;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.device.constant.MqttTopicConstants;

/**
 * MQTT消息事件回调处理器
 * 
 * @author admin
 * @date 2020年7月16日
 */
@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttMsgEventHandler {

    private static final transient Logger LOG = LoggerFactory.getLogger(MqttMsgEventHandler.class);

    @Autowired
    private PlatformMqttClientUtil clientUtil;
    @Autowired
    private DeviceMqttBusiHandler clientDeviceMqttBusiHandler;
    @Value("${mqtt.msg-async}")
    private boolean msgAsync;
    @Value("${mqtt.msg-thread-pool-size}")
    private int msgThreadPoolSize;
    private static ExecutorService fixedThreadPool;

    @PostConstruct
    private void initPool() {
        if (msgAsync) {
            fixedThreadPool = Executors.newFixedThreadPool(msgThreadPoolSize);
        }
    }

    /**
     * MQTT服务器连接成功事件回调
     * 
     * @param reconnect 是否是重新连接
     * @param serverURI 服务URI地址
     */
    public void handleConnectCompleteEvent(boolean reconnect, String serverURI) {
        LOG.info("reconnect is [{}],serverURI is [{}] ", reconnect, serverURI);
        CompletableFuture.runAsync(() -> {
            String[] topicFilters = {MqttTopicConstants.SUB_DEVICE_REGIST_TOPIC_PATTERN,
                MqttTopicConstants.SUB_DEVICE_UPGRADE_RESULT_TOPIC_PATTERN,
                MqttTopicConstants.SUB_DEVICE_CONFIG_RESULT_TOPIC_PATTERN};
            int[] qos = {2, 2, 2};
            boolean result = false;
            for (int i = 0; i < 3; i++) {
                result = clientUtil.subscribe(topicFilters, qos);
                if (result) {
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            if (!result) {
                LOG.error("Failed to initialize subscription topic~");
            }
        });
    }

    /**
     * 处理MQTT消息接收事件回调
     * 
     * @param topic 接受的消息主题
     * @param message 接收的消息信息
     */
    public void handleMessageArrivedEvent(String topic, MqttMessage message) {
        if (!msgAsync) {
            executeHandleMsg(topic, message);
            return;
        }
        CompletableFuture.runAsync(() -> {
            executeHandleMsg(topic, message);
        }, fixedThreadPool);
    }

    /**
     * 执行接收消息处理逻辑
     * 
     * @param topic
     * @param message
     */
    private void executeHandleMsg(String topic, MqttMessage message) {
        String deviceNo = topic.substring(topic.lastIndexOf("/") + 1);
        // 设备注册
        if (MqttTopicConstants.SUB_DEVICE_REGIST_TOPIC_PATTERN.replace("+", deviceNo).equals(topic)) {
            clientDeviceMqttBusiHandler.registerDevice(deviceNo, message);
            return;
        }
        // 设备升级结果回写
        if (MqttTopicConstants.SUB_DEVICE_UPGRADE_RESULT_TOPIC_PATTERN.replace("+", deviceNo).equals(topic)) {
            clientDeviceMqttBusiHandler.saveDeviceUpgradeResult(deviceNo, message);
            return;
        }
        // 参数下发结果回写
        if (MqttTopicConstants.SUB_DEVICE_CONFIG_RESULT_TOPIC_PATTERN.replace("+", deviceNo).equals(topic)) {
            clientDeviceMqttBusiHandler.saveDeviceParamConfigResult(deviceNo, message);
            return;
        }
        LOG.error("Unknown topic subscription information,topic:{},message:{}", topic, message.toString());
    }

    /**
     * MQTT消息传递完毕事件回调
     * 
     * @param token
     */
    public void handleDeliveryComplete(IMqttDeliveryToken token) {
        LOG.debug("deliveryComplete---------" + token.isComplete());
        LOG.debug("topic:{}", Arrays.asList(token.getTopics()).stream().reduce((e1, e2) -> e1 + "," + e2).orElse(null));
    }

    /**
     * 连接丢失事件回调处理
     * 
     * @param cause
     */
    public void handleConnectLostEvent(Throwable cause) {
        LOG.warn("Connection lost:" + cause.getMessage());
        clientUtil.reconnect();
    }

}
