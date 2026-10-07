package cn.eyecool.tradelog.domain.police;

import lombok.Data;

/**
 * @author zfx
 * @ClassName RegisterObj
 * @description 注册、保活、注销 请求体对象
 * @since 2026/8/20 10:40
 **/
@Data
public class ViidSystemReq {
    @Data
    public static class RegisterObject {
        private String DeviceID;
    }
    @Data
    public static class KeepaliveObject {
        private String DeviceID;
    }
    @Data
    public static class UnRegisterObject {
        private String DeviceID;
    }
    //注册
    private RegisterObject RegisterObject;
    //保活
    private KeepaliveObject KeepaliveObject;
    //注销
    private UnRegisterObject UnRegisterObject;
}