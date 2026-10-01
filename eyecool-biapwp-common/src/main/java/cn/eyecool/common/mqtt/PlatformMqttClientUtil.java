package cn.eyecool.common.mqtt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.eyecool.common.utils.StringUtils;

/**
 * Mqtt客户端工具类
 * 
 * @author admin
 * @date 2020年7月14日
 */
public class PlatformMqttClientUtil {

    private static final transient Logger LOG = LoggerFactory.getLogger(PlatformMqttClientUtil.class);

    private String broker;
    private String clientId;
    private MqttClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;
    private MqttCallback mqttCallback;

    public PlatformMqttClientUtil() {
        super();
    }

    public PlatformMqttClientUtil(MqttCallback mqttCallback) {
        super();
        this.mqttCallback = mqttCallback;
    }

    public PlatformMqttClientUtil(String broker, String clientId, MqttConnectOptions mqttConnectOptions,
        MqttCallback mqttCallback) {
        super();
        this.broker = broker;
        this.clientId = clientId;
        this.mqttConnectOptions = mqttConnectOptions;
        this.mqttCallback = mqttCallback;
    }

    public String getBroker() {
        return broker;
    }

    public String getClientId() {
        return clientId;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }

    /**
     * 初始化信息
     */
    @PostConstruct
    private void init() {
        this.initConnOptions();
        this.initClient();
        this.initDefaultCallback();
        // this.connect();
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
        if (StringUtils.isBlank(broker)) {
            this.broker = MqttConfigManager.getItemValue("broker");
        }
        if (StringUtils.isBlank(clientId)) {
            this.clientId = MqttConfigManager.getItemValue("clientId");
        }
        try {
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            LOG.error(e.getMessage());
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
        String cleanSession = MqttConfigManager.getItemValue("cleanSession");
        if (StringUtils.isNotBlank(cleanSession)) {
            mqttConnectOptions.setCleanSession(Boolean.parseBoolean(cleanSession));
        }
        // 设置连接的用户名
        String username = MqttConfigManager.getItemValue("username");
        if (StringUtils.isNotBlank(username)) {
            mqttConnectOptions.setUserName(username);
        }
        // 设置连接的密码
        String password = MqttConfigManager.getItemValue("password");
        if (StringUtils.isNotBlank(password)) {
            mqttConnectOptions.setPassword(password.toCharArray());
        }
        // 设置超时时间 单位为秒
        String connectionTimeout = MqttConfigManager.getItemValue("connectionTimeout");
        if (StringUtils.isNotBlank(connectionTimeout)) {
            mqttConnectOptions.setConnectionTimeout(Integer.parseInt(connectionTimeout));
        }
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        String keepAliveInterval = MqttConfigManager.getItemValue("keepAliveInterval");
        if (StringUtils.isNotBlank(keepAliveInterval)) {
            mqttConnectOptions.setKeepAliveInterval(Integer.parseInt(keepAliveInterval));
        }
        // 自动重连
        String automaticReconnect = MqttConfigManager.getItemValue("automaticReconnect");
        if (StringUtils.isNotBlank(automaticReconnect)) {
            mqttConnectOptions.setAutomaticReconnect(Boolean.parseBoolean(automaticReconnect));
        }
        // 自动重连
        String maxInflight = MqttConfigManager.getItemValue("maxInflight");
        if (StringUtils.isNotBlank(maxInflight)) {
            mqttConnectOptions.setMaxInflight(Integer.parseInt(maxInflight));
        }
    }

    /**
     * 初始化MqttCallback
     */
    private void initDefaultCallback() {
        if (this.mqttCallback == null) {
            this.mqttCallback = new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    LOG.warn("connectionLost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    LOG.debug("\ntopic:" + topic + "\nQos:" + message.getQos() + "\nmessage content:"
                        + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    LOG.debug("deliveryComplete---------" + token.isComplete());
                }
            };
        }
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
        }
    }

}
