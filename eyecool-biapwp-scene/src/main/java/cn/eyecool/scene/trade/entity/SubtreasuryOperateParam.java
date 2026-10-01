package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 子场景HTTP接口保存参数
 * 
 * @author zhanglei
 * @date 2021年01月26日
 */
public class SubtreasuryOperateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 场景编码 */
    private String channelCode;
    /** 子场景编码 */
    private String subTreasuryCode;
    /** 子场景名称 */
    private String subTreasuryName;
    /** 子场景备注 */
    private String remark;
    /** 操作类型("ADD"：添加，"DELETE"：删除（情况库下面的所有人），"UPDATE"：修改) */
    private String operateType;

    /** 场景id */
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getSubTreasuryCode() {
        return subTreasuryCode;
    }

    public void setSubTreasuryCode(String subTreasuryCode) {
        this.subTreasuryCode = subTreasuryCode;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getSubTreasuryName() {
        return subTreasuryName;
    }

    public void setSubTreasuryName(String subTreasuryName) {
        this.subTreasuryName = subTreasuryName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
