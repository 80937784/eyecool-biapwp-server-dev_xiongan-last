package cn.eyecool.common.core.domain.http;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.alibaba.fastjson.JSON;

/**
 * 标准HTTP请求参数Bean.
 * 
 * @author admin
 * @date 2019年11月4日
 */
public class StandardHttpParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应用唯一标识 */
    @NotBlank(message = "http.param.appkey.empty")
    @Length(min = 8, max = 8, message = "http.param.appkey.max.length.limit")
    private String appKey;

    /** 请求签名(防止篡改) */
    @NotBlank(message = "http.param.sign.empty")
    private String sign;

    /** 请求时间戳(和nonce配合，确保请求的唯一性,防止重放攻击) */
    @NotBlank(message = "http.param.timestamp.empty")
    @Length(min = 13, max = 13, message = "http.param.timestamp.max.length.limit")
    private String timestamp;

    /** 请求唯一标识 */
    @NotBlank(message = "http.param.nonce.empty")
    @Length(max = 48, message = "http.param.nonce.max.length.limit")
    private String nonce;

    /** 接口交易码 */
    @NotBlank(message = "http.param.transcode.empty")
    @Length(max = 48, message = "http.param.transcode.max.length.limit")
    private String transCode;

    /** 接口请求参数的集合 */
    @NotBlank(message = "http.param.bizcontent.empty")
    private String bizContent;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getBizContent() {  
        return bizContent;
    }

    public void setBizContent(String bizContent) {
        this.bizContent = bizContent;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
