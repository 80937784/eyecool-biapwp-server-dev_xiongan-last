package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 邮箱配置对象 msg_mail_property
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgMailProperty extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** SMTP服务地址 */
    @Excel(name = "msg.mail.host")
    private String host;

    /** SMTP服务端口 */
    @Excel(name = "msg.mail.port")
    private Integer port;

    /** 登录用户名 */
    @Excel(name = "msg.mail.username")
    private String username;

    /** 登录授权码 */
    @Excel(name = "msg.mail.password")
    private String password;

    /** 发件邮箱 */
    @Excel(name = "msg.mail.address")
    private String emailAddr;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
