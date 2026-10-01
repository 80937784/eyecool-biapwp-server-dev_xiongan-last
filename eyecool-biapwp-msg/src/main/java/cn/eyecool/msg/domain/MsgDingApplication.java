package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 钉钉微应用对象 msg_ding_application
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgDingApplication extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 团队(企业)主键 */
    private String teamId;

    /** 团队(企业)名称 */
    @Excel(name = "msg.ding.app.teamname")
    private String teamName;

    /** 团队(企业)CorpId */
    @Excel(name = "msg.ding.app.corpid")
    private String corpId;

    /** 微应用名称 */
    @Excel(name = "msg.ding.app.appname")
    private String appName;

    /** 微应用agentId */
    @Excel(name = "msg.ding.app.agentid")
    private String agentId;

    /** 微应用AppKey */
    @Excel(name = "msg.ding.app.appkey")
    private String appKey;

    /** 微应用AppSecrect */
    @Excel(name = "msg.ding.app.appsecret")
    private String appSecrect;

    /** 微应用简介 */
    @Excel(name = "msg.ding.app.shortdesc")
    private String shortDes;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppSecrect(String appSecrect) {
        this.appSecrect = appSecrect;
    }

    public String getAppSecrect() {
        return appSecrect;
    }

    public void setShortDes(String shortDes) {
        this.shortDes = shortDes;
    }

    public String getShortDes() {
        return shortDes;
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
