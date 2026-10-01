package cn.eyecool.server.http.param;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

/**
 * Mqtt认证参数
 * 
 * @author mawj
 * @date 2021/03/30
 */
public class MqttAuthParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户端ID */
    @NotBlank(message = "mqtt.param.clientid.msg")
    private String clientid;

    /** 用户名（设备SN） */
    @NotBlank(message = "mqtt.param.username.msg")
    private String username;

    /** 明文密码 */
    @NotBlank(message = "mqtt.param.password.msg")
    private String password;

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
