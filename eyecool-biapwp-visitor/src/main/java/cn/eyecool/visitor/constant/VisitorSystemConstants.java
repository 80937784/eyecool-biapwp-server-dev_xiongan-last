package cn.eyecool.visitor.constant;


/**
 * 访客系统常量类
 * @Author Administrator
 * @create 2021/10/21 15:47
 */
public class VisitorSystemConstants {


    /** 访客系统中验证码在redis中中存放前缀 **/
    public static final String VISITOR_SYSTEM_LOGIN_VERIFY_CODE_CACHE_PREFIX = "visitor-system:verify-code:";

    /** 访客系统中token在redis中中存放前缀 **/
    public static final String VISITOR_SYSTEM_TOKEN_CACHE_PREFIX = "visitor-system:token:";

    /**验证码数据来源 **/
    public static final String SOURCE="123456789";

    /**验证码有效时间(秒) **/
    public static final Long VERIFY_CODE_EFFECTIVE_TIME=60*5L;

    /**验证码允许重复请求时间(秒) **/
    public static final Long VERIFY_CODE_REACQUIRE_TIME=60L;

    /**token失效时间(秒) **/
    public static final Long TOKEN_EXPIRE_TIME=60*60L;



}
