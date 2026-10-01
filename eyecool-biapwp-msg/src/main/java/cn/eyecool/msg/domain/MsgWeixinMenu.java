package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 微信公众号菜单对象 msg_weixin_menu
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgWeixinMenu extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 公众号主键 */
    private String officalAccountId;

    /** 公众号名称 */
    @Excel(name = "msg.weixin.menu.officalaccount.name")
    private String officalAccountName;

    /** 公众号AppId */
    @Excel(name = "msg.weixin.menu.appid")
    private String appId;

    /** 菜单JSON */
    @Excel(name = "msg.weixin.menu.json")
    private String menuJson;

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

    public String getOfficalAccountName() {
        return officalAccountName;
    }

    public void setOfficalAccountName(String officalAccountName) {
        this.officalAccountName = officalAccountName;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setMenuJson(String menuJson) {
        this.menuJson = menuJson;
    }

    public String getMenuJson() {
        return menuJson;
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
