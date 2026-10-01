package cn.eyecool.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.ColumnType;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 岗位表 sys_post
 * 
 * @author admin
 */
public class SysPost extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 岗位序号 */
    @Excel(name = "sys.post.postid", cellType = ColumnType.NUMERIC)
    private Long postId;

    /** 岗位编码 */
    @Excel(name = "sys.post.postcode")
    private String postCode;

    /** 岗位名称 */
    @Excel(name = "sys.post.postname")
    private String postName;

    /** 岗位排序 */
    @Excel(name = "sys.post.postsort")
    private String postSort;

    /** 状态（0正常 1停用） */
    @Excel(name = "sys.post.status", readConverterExp = "0=sys.post.status0,1=sys.post.status1")
    private String status;

    /** 用户是否存在此岗位标识 默认不存在 */
    private boolean flag = false;

    /** 租户ID */
    private String tenantId;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @NotBlank(message = "sys.post.postcode.msg")
    @Size(min = 0, max = 64, message = "sys.post.postcode.msg.length")
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @NotBlank(message = "sys.post.postname.msg")
    @Size(min = 0, max = 50, message = "sys.post.postname.msg.length")
    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    @NotBlank(message = "sys.post.postsort.msg")
    public String getPostSort() {
        return postSort;
    }

    public void setPostSort(String postSort) {
        this.postSort = postSort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
