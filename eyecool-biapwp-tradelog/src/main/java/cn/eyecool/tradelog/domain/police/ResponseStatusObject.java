package cn.eyecool.tradelog.domain.police;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 通用响应状态对象 ResponseStatusObject */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ResponseStatusObject {

    /** 请求URL */
    public String RequestURL;

    /** 状态码：0-正常 1-系统忙 2-其他错误 3-参数错误 4-用户名错误 5-无权限 */
    public Integer StatusCode;

    /** 状态描述 */
    public String StatusString;

    /** 对应对象的ID */
    public String Id;

    /** 本地时间 YYYYMMDDHHMMSS */
    public String LocalTime;

    public ResponseStatusObject() {}

    public ResponseStatusObject(Integer code, String string) {
        this.StatusCode = code;
        this.StatusString = string;
    }

    public static ResponseStatusObject ok(String id) {
        ResponseStatusObject o = new ResponseStatusObject(0, "正常");
        o.Id = id;
        return o;
    }

    public static ResponseStatusObject err(int code, String msg) {
        return new ResponseStatusObject(code, msg);
    }
}
