package cn.eyecool.tradelog.domain.police;

import lombok.Data;

/**
 * @author zfx
 * @ClassName ResponseObj
 * @description 调用返回对象
 * @since 2026/8/20 10:44
 **/
@Data
public class ViidResponse {
    private ResponseStatus ResponseStatus;
    @Data
    public static class ResponseStatus {
        //R必选，本次请求URI
        private String RequestURL;
        //0成功，其他错误码
        private String StatusCode;
        //状态描述
        private String StatusString;
        //O，创建资源才返回ID；注册/保活/注销不返回
        private String ID;
        //O，校时 yyyyMMddHHmmss
        private String LocalTime;
    }
    public ResponseStatusListObject ResponseStatusListObject;

    public ViidResponse() {}

    public ViidResponse(ResponseStatusListObject o) {
        this.ResponseStatusListObject = o;
    }
}
