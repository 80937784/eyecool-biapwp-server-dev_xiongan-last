package cn.eyecool.emqx.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cn.eyecool.emqx.properties.EmqxProperties.ConnectOptions;
import cn.eyecool.emqx.properties.MqttProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Mqtt服务类
 * 
 * @author admin
 * @date 2020年7月14日
 */
@Slf4j
@Getter
@Setter
public class PlatformMqttService {

    private MqttProperties mqttProperties;
    private MqttClient mqttClient;
    private IMqttMsgEventHandler mqttMsgEventHandler;
    private MqttConnectOptions mqttConnectOptions;

    public static ExecutorService fixedThreadPool;

    public PlatformMqttService() {
        super();
    }

    public PlatformMqttService(IMqttMsgEventHandler mqttMsgEventHandler) {
        super();
        this.mqttMsgEventHandler = mqttMsgEventHandler;
    }

    public PlatformMqttService(MqttProperties mqttProperties, IMqttMsgEventHandler mqttMsgEventHandler) {
        super();
        this.mqttProperties = mqttProperties;
        this.mqttMsgEventHandler = mqttMsgEventHandler;
    }

    /**
     * 初始化线程池
     */
    private void initPool() {
        if (Boolean.TRUE.equals(mqttProperties.getMsgAsync())) {
            fixedThreadPool = Executors.newFixedThreadPool(mqttProperties.getMsgThreadPoolSize());
        }
    }

    /**
     * 初始化信息
     */
    @PostConstruct
    private void init() {
        this.initConnOptions();
        this.initClient();
        this.initPool();
        this.initDefaultCallback();
    }

    /**
     * 销毁对象
     */
    @PreDestroy
    private void destroy() {
        this.closeMattClient();
    }

    /**
     * 初始化MQTTClient对象
     */
    private MqttClient initClient() {
        String broker = mqttProperties.getEmqx().getBroker();
        String clientId = mqttProperties.getEmqx().getClientId();
        try {
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            log.error(e.getMessage());
            mqttClient = null;
        }
        return mqttClient;
    }

    /**
     * 初始化连接参数
     */
    private void initConnOptions() {
        // MQTT的连接设置
        this.mqttConnectOptions = new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        ConnectOptions connectOptions = mqttProperties.getEmqx().getConnectOptions();
        mqttConnectOptions.setCleanSession(connectOptions.getCleanSession());
        // 设置连接的用户名
        mqttConnectOptions.setUserName(connectOptions.getUsername());
        // 设置连接的密码
        String password = connectOptions.getPassword();
        if (StringUtils.isNotBlank(password)) {
            mqttConnectOptions.setPassword(password.toCharArray());
        }
        // 设置超时时间 单位为秒
        mqttConnectOptions.setConnectionTimeout(connectOptions.getConnectionTimeout());
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        mqttConnectOptions.setKeepAliveInterval(connectOptions.getKeepAliveInterval());
        // 自动重连
        mqttConnectOptions.setAutomaticReconnect(connectOptions.getAutomaticReconnect());
        // 自动重连
        mqttConnectOptions.setMaxInflight(connectOptions.getMaxInflight());
    }

    /**
     * 初始化MqttCallback
     */
    private void initDefaultCallback() {
        MqttCallback mqttCallback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 执行具体的连接成片事件回调处理
                log.info("connectComplete, is reconnect:[{}], serverURI:[{}]", reconnect, serverURI);
                mqttMsgEventHandler.handleConnectCompleteEvent(reconnect, serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                // 连接丢失事件回调处理
                log.warn("connectionLost", cause);
                mqttMsgEventHandler.handleConnectLostEvent(cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 根据主题类型进行不通的业务操作回调处理
                log.debug("\ntopic:" + topic + "\nQos:" + message.getQos() + "\nmessage content:"
                    + new String(message.getPayload()));
                if (!Boolean.TRUE.equals(mqttProperties.getMsgAsync())) {
                    mqttMsgEventHandler.handleMessageArrivedEvent(topic, message);
                    return;
                }
                CompletableFuture.runAsync(() -> {
                    mqttMsgEventHandler.handleMessageArrivedEvent(topic, message);
                }, fixedThreadPool);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 消息传递完成事件回调处理
                log.debug("deliveryComplete---------" + token.isComplete());
                mqttMsgEventHandler.handleDeliveryComplete(token);
            }
        };
        mqttClient.setCallback(mqttCallback);
    }

    /**
     * 连接MQTT服务器
     */
    public void connect() {
        if (mqttClient == null && initClient() == null) {
            return;
        }
        if (mqttClient.isConnected()) {
            return;
        }
        try {
            mqttClient.connect(mqttConnectOptions);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 重新连接
     */
    public void reconnect() {
        this.closeMattClient();
        this.connect();
    }

    /**
     * 消息订阅
     * 
     * @param topicFilter
     * @param qos
     * @return
     */
    public boolean subscribe(String topicFilter, int qos) {
        try {
            mqttClient.subscribe(topicFilter, qos);
            return true;
        } catch (MqttException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 消息订阅
     * 
     * @param topicFilters
     * @param qos
     * @return
     */
    public boolean subscribe(String[] topicFilters, int[] qos) {
        try {
            mqttClient.subscribe(topicFilters, qos);
            return true;
        } catch (MqttException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 取消消息订阅
     * 
     * @param topicFilter
     * @return
     */
    public boolean unSubscribe(String topicFilter) {
        try {
            mqttClient.unsubscribe(topicFilter);
            return true;
        } catch (MqttException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 取消消息订阅
     * 
     * @param topicFilters
     * @return
     */
    public boolean unSubscribe(String[] topicFilters) {
        try {
            mqttClient.unsubscribe(topicFilters);
            return true;
        } catch (MqttException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 发布消息
     * 
     * @param topic
     * @param content
     * @param qos
     * @param retained
     * @param disConnect
     */
    public boolean publishMessage(String topic, String content, int qos, boolean retained, boolean disConnect) {
        return publishMessage(topic, content.getBytes(), qos, retained, disConnect);
    }

    /**
     * 发布消息
     * 
     * @param topic
     * @param payload
     * @param qos
     * @param retained
     * @param disConnect
     * @return
     */
    public boolean publishMessage(String topic, byte[] payload, int qos, boolean retained, boolean disConnect) {
        // 创建消息
        MqttMessage message = new MqttMessage(payload);
        message.setRetained(retained);
        // 设置消息的服务质量
        message.setQos(qos);
        return publishMessage(topic, message, disConnect);

    }

    /**
     * 发布消息
     * 
     * @param topic
     * @param message
     * @param disConnect
     * @return
     */
    public boolean publishMessage(String topic, MqttMessage message, boolean disConnect) {
        try {
            this.connect();
            mqttClient.publish(topic, message);
            // 断开连接
            if (disConnect) {
                closeMattClient();
            }
            return true;
        } catch (MqttException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 关闭连接
     */
    public void closeMattClient() {
        if (null == mqttClient || !mqttClient.isConnected()) {
            return;
        }
        try {
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            log.error(e.getMessage());
        }
    }

}
