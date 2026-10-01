package cn.eyecool.tradelog.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 接口交易请求记录详细对象 trade_req_record_detail
 * 
 * @author admin
 * @date 2021-05-13
 */
public class TradeReqRecordDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 交易记录主键 */
    private String recordId;

    /** 请求报文 */
    private String requestMsg;

    /** 响应报文 */
    private String responseMsg;

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRequestMsg(String requestMsg) {
        this.requestMsg = requestMsg;
    }

    public String getRequestMsg() {
        return requestMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
