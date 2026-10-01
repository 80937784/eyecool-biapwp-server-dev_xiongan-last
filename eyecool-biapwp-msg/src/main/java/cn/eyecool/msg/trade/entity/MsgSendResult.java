package cn.eyecool.msg.trade.entity;

import java.io.Serializable;
import java.util.Map;

import com.beust.jcommander.internal.Maps;

/**
 * 消息推送结果
 * 
 * @author admin
 * @date 2020年3月19日
 */
public class MsgSendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码（0：成功, 1:失败） */
    private String statusCode;
    /** 接口响应数据JSON */
    private String jsonResponse;
    /** 错误信息 */
    private String errmsg;

    private Map<String, Object> data = Maps.newHashMap();

    public MsgSendResult() {
        super();
    }

    public MsgSendResult(String statusCode, String jsonResponse, String errmsg) {
        super();
        this.statusCode = statusCode;
        this.jsonResponse = jsonResponse;
        this.errmsg = errmsg;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public Map<String, Object> getData() {
        return data;
    }

}
