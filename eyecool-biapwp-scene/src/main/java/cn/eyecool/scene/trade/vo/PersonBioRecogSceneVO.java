package cn.eyecool.scene.trade.vo;

import java.io.Serializable;

/**
 * 人脸1-N识别场景信息VO
 * 
 * @author admin
 * @date 2019年11月20日
 */
public class PersonBioRecogSceneVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 场景编码 */
    private String channelCode;
    /** 业务号1 */
    private String busiCodeFirst;
    /** 业务号2 */
    private String busiCodeSecond;
    /** 业务号3 */
    private String busiCodeThird;
    /** 手机号 */
    private String phone;

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
