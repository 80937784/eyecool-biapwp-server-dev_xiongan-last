package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 消息模板对象 msg_template
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 模板名称 */
    @Excel(name = "msg.template.name")
    private String templateName;

    /** 模板官方ID */
    @Excel(name = "msg.template.officalid")
    private String officalId;

    /** 模板内容 */
    @Excel(name = "msg.template.content")
    private String content;

    /** 通知方式 */
    @Excel(name = "msg.template.notice.method", dictType = "msg_notice_method")
    private String noticeMethod;

    /** 公众(企业)号ID */
    private String officalAccountId;

    @Excel(name = "msg.template.officalaccount.name")
    private String officalAccountName;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setOfficalId(String officalId) {
        this.officalId = officalId;
    }

    public String getOfficalId() {
        return officalId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setNoticeMethod(String noticeMethod) {
        this.noticeMethod = noticeMethod;
    }

    public String getNoticeMethod() {
        return noticeMethod;
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
