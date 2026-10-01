package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 微信用户对象 msg_offical_account_user
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgOfficalAccountUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 公众号ID */
    private String officalAccountId;

    /** 公众号AppId */
    @Excel(name = "msg.offical.account.user.appid")
    private String appId;

    /** 公众号名称 */
    @Excel(name = "msg.offical.account.user.accountname")
    private String officalAccountName;

    /** 微信标识 */
    @Excel(name = "msg.offical.account.user.openid")
    private String openId;

    /** 微信名称 */
    @Excel(name = "msg.offical.account.user.wxname")
    private String wxName;

    /** 微信头像 */
    private String headImgUrl;

    /** 绑定手机 */
    @Excel(name = "msg.offical.account.user.phone")
    private String phone;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setOfficalAccountId(String officalAccountId) {
        this.officalAccountId = officalAccountId;
    }

    public String getOfficalAccountId() {
        return officalAccountId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public String getOfficalAccountName() {
        return officalAccountName;
    }

    public void setOfficalAccountName(String officalAccountName) {
        this.officalAccountName = officalAccountName;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setWxName(String wxName) {
        this.wxName = wxName;
    }

    public String getWxName() {
        return wxName;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
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
