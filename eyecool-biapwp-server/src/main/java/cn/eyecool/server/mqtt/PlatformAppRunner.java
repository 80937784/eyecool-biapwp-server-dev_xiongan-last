package cn.eyecool.server.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * app启动成功监控
 * 
 * @author mawj
 * @date 2021/03/30
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class PlatformAppRunner implements ApplicationRunner {

    @Autowired
    private PlatformMqttClientUtil mqttClientUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Spring boot started successfully, calling mqtt connection");
        mqttClientUtil.connect();
    }

}
