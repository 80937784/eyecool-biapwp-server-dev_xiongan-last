package cn.eyecool.msg.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 消息日志附件对象 msg_log_annex
 * 
 * @author admin
 * @date 2021-04-15
 */
public class MsgLogAnnex extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 消息日志ID */
    @Excel(name = "msg.log.annex.logid")
    private String logId;

    /** 源文件名称 */
    @Excel(name = "msg.log.annex.filename")
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件MD5 */
    @Excel(name = "msg.log.annex.file.md5")
    private String fileMd5;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getLogId() {
        return logId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileMd5() {
        return fileMd5;
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
