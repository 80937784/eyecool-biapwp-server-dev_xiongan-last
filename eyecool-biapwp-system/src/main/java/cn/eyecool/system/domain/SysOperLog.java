package cn.eyecool.system.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.ColumnType;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 操作日志记录表 oper_log
 * 
 * @author admin
 */
public class SysOperLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志主键 */
    @Excel(name = "sys.oper.log.operid", cellType = ColumnType.NUMERIC)
    private Long operId;

    /** 操作模块 */
    @Excel(name = "sys.oper.log.title")
    private String title;

    /** 业务类型（0其它 1新增 2修改 3删除） */
    @Excel(name = "sys.oper.log.busitype", readConverterExp = "0=sys.oper.log.busitype0,1=sys.oper.log.busitype1,2=sys.oper.log.busitype2,3=sys.oper.log.busitype3,4=sys.oper.log.busitype4,5=sys.oper.log.busitype5,6=sys.oper.log.busitype6,7=sys.oper.log.busitype7,8=sys.oper.log.busitype8,9=sys.oper.log.busitype9")
    private Integer businessType;

    /** 业务类型数组 */
    private Integer[] businessTypes;

    /** 请求方法 */
    @Excel(name = "sys.oper.log.method")
    private String method;

    /** 请求方式 */
    @Excel(name = "sys.oper.log.method.request")
    private String requestMethod;

    /** 操作类别（0其它 1后台用户 2手机端用户） */
    @Excel(name = "sys.oper.log.oper.type", readConverterExp = "0=sys.oper.log.oper.type0,1=sys.oper.log.oper.type1,2=sys.oper.log.oper.type2")
    private Integer operatorType;

    /** 操作人员 */
    @Excel(name = "sys.oper.log.opername")
    private String operName;

    /** 部门名称 */
    @Excel(name = "sys.oper.log.deptname")
    private String deptName;

    /** 请求url */
    @Excel(name = "sys.oper.log.operurl")
    private String operUrl;

    /** 操作地址 */
    @Excel(name = "sys.oper.log.operip")
    private String operIp;

    /** 操作地点 */
    @Excel(name = "sys.oper.log.operlocation")
    private String operLocation;

    /** 请求参数 */
    @Excel(name = "sys.oper.log.operparam")
    private String operParam;

    /** 返回参数 */
    @Excel(name = "sys.oper.log.jsonresult")
    private String jsonResult;

    /** 操作状态（0正常 1异常） */
    @Excel(name = "sys.oper.log.status", readConverterExp = "0=sys.oper.log.status0,1=sys.oper.log.status1")
    private Integer status;

    /** 错误消息 */
    @Excel(name = "sys.oper.log.errormsg")
    private String errorMsg;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "sys.oper.log.opertime", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    /** 租户ID */
    private String tenantId;

    public Long getOperId() {
        return operId;
    }

    public void setOperId(Long operId) {
        this.operId = operId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public Integer[] getBusinessTypes() {
        return businessTypes;
    }

    public void setBusinessTypes(Integer[] businessTypes) {
        this.businessTypes = businessTypes;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public void setOperUrl(String operUrl) {
        this.operUrl = operUrl;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public String getOperLocation() {
        return operLocation;
    }

    public void setOperLocation(String operLocation) {
        this.operLocation = operLocation;
    }

    public String getOperParam() {
        return operParam;
    }

    public void setOperParam(String operParam) {
        this.operParam = operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getOperTime() {
        return operTime;
    }

    public void setOperTime(Date operTime) {
        this.operTime = operTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}
