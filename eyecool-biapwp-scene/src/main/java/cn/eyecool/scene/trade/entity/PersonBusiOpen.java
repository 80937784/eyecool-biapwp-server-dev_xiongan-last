package cn.eyecool.scene.trade.entity;

import java.io.Serializable;

/**
 * 开通人脸、指纹、虹膜、指静脉参数
 * 
 * @author admin
 * @date 2019年11月14日
 */
public class PersonBusiOpen implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 唯一标识 */
    private String uniqueId;
    /** 业务流水号 */
    private String receivedSeq;
    /** 场景编码 */
    private String channelCode;
    /** 场景密钥 */
    private String channelKey;
    /** 业务号1 */
    private String busiCodeFirst;
    /** 业务号2 */
    private String busiCodeSecond;
    /** 业务号3 */
    private String busiCodeThird;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
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

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getBusiCodeFirst() {
        return busiCodeFirst;
    }

    public void setBusiCodeFirst(String busiCodeFirst) {
        this.busiCodeFirst = busiCodeFirst;
    }

    public String getBusiCodeSecond() {
        return busiCodeSecond;
    }

    public void setBusiCodeSecond(String busiCodeSecond) {
        this.busiCodeSecond = busiCodeSecond;
    }

    public String getBusiCodeThird() {
        return busiCodeThird;
    }

    public void setBusiCodeThird(String busiCodeThird) {
        this.busiCodeThird = busiCodeThird;
    }

}
