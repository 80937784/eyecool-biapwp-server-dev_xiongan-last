package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 子场景HTTP接口保存参数
 * 
 * @author admin
 * @date 2019年12月20日
 */
public class SubtreasuryBusiOperateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号 */
    private String receivedSeq;
    /** 场景编码 */
    private String channelCode;
    /** 子场景编码(场景编码_编号) */
    private String subTreasuryCode;
    /** 人员唯一标识(多个使用,分割, 在添加和删除操作时启用) */
    private String uniqueIds;
    /** 是否追加（N：否，Y：是），不追加则清空原来的子场景重新添加，追加的话则不清空, 此参数只有添加的时候启用 */
    private String isAppend;
    /** 操作类型("ADD"：添加，"DELETE"：删除，"CLEAR"：清空(删除库)) */
    private String operateType;

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

    public String getUniqueIds() {
        return uniqueIds;
    }

    public void setUniqueIds(String uniqueIds) {
        this.uniqueIds = uniqueIds;
    }

    public String getIsAppend() {
        return isAppend;
    }

    public void setIsAppend(String isAppend) {
        this.isAppend = isAppend;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

}
