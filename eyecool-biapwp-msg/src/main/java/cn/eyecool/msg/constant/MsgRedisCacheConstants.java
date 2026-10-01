package cn.eyecool.msg.constant;

/**
 * 消息通知部分redis缓存常量
 * 
 * @author mawj
 * @date 2021/04/15
 */
public class MsgRedisCacheConstants {

    /** 钉钉Token缓存 */
    public static final String MSG_DINGTALK_TOKEN_CACHE_KEY = "msg:dingtalk:token";
    /** 微信Token缓存 */
    public static final String MSG_WECHAT_TOKEN_CACHE_KEY = "msg:wechat:token";
    /** 微信实例缓存 */
    public static final String MSG_WECHAT_INSTANCE_CACHE_KEY = "msg:wechat:instance";
    /** 钉钉实例缓存 */
    public static final String MSG_DINGTALK_INSTANCE_CACHE_KEY = "msg:dingtalk:instance";
    /** 微信自助绑定手机号验证码缓存KEY前缀 */
    public static final String WECHAT_PHONE_BIND_CAPTCHAT_CACHE_KEY_PREFIX = "msg:wechat:bind_captcha:";
}
