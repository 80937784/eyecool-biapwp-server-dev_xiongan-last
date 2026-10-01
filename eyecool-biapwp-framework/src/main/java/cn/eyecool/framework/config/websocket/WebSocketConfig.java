package cn.eyecool.framework.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * websocket配置
 * 
 * @author admin
 * @date 2019年11月6日
 */
@Component
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter createServerEndpointExporter() {
        return new ServerEndpointExporter();
    }
}