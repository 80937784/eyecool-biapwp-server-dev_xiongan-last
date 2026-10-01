package cn.eyecool.device.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mqtt认证AES加密密钥组件
 * 
 * @author mawj
 * @date 2021/03/31
 */
@Component
public class MqttAuthAesKeyComponent {

    @Value("${mqtt.auth.aes.skey:4a9Nw3!^#Qf1e407}")
    private String deviceMqttAuthSkey;

    public String getDeviceMqttAuthSkey() {
        return deviceMqttAuthSkey;
    }

}
