package cn.eyecool.tool.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * SDK文件上传对象 sys_sdk_file
 *
 * @author admin
 * @date 2021-03-31
 */
public class SysSdkFile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 上传文件名称 */
    @Excel(name = "sys.sdkfile.filename")
    private String fileName;

    /** 文件存储路径 */
    @Excel(name = "sys.sdkfile.filepath")
    private String filePath;

    /** 文件MD5 */
    @Excel(name = "sys.sdkfile.filemd5")
    private String md5;

    /** SDK类型 */
    @Excel(name = "sys.sdkfile.filetype", dictType = "sys_sdk_type")
    private String sdkType;

    /** 租户ID */
    private String tenantId;

    /** 版本排序 */
    @Excel(name = "sys.sdkfile.file.sort")
    private Long sortedNo;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }

    public String getSdkType() {
        return sdkType;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setSortedNo(Long sortedNo) {
        this.sortedNo = sortedNo;
    }

    public Long getSortedNo() {
        return sortedNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("id", getId())
            .append("fileName", getFileName()).append("filePath", getFilePath()).append("md5", getMd5())
            .append("sdkType", getSdkType()).append("createBy", getCreateBy()).append("updateBy", getUpdateBy())
            .append("createTime", getCreateTime()).append("updateTime", getUpdateTime())
            .append("tenantId", getTenantId()).append("sortedNo", getSortedNo()).toString();
    }
}