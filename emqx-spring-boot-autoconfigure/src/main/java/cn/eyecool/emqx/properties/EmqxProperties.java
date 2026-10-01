package cn.eyecool.emqx.properties;

import lombok.Data;

/**
 * Emqx服务器连接配置
 * 
 * @author mawj
 * @date 2021/12/31
 */
@Data
public class EmqxProperties {

    /** emqx连接broker */
    private String broker = "tcp://127.0.0.1:1883";
    /** emqx连接客户端Id */
    private String clientId = "client-node-1";
    /** emqx连接属性 */
    private ConnectOptions connectOptions;

    @Data
    public static class ConnectOptions {
        /** 是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接 */
        private Boolean cleanSession = true;
        /** 连接的用户名 */
        private String username;
        /** 连接的密码 */
        private String password;
        /** 超时时间 单位为秒 */
        private Integer connectionTimeout = 20;
        /** 会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制 */
        private Integer keepAliveInterval = 60;
        /** 自动重连 */
        private Boolean automaticReconnect = true;
        /** 消息队列最大长度 */
        private Integer maxInflight = 10000;

    }

}
