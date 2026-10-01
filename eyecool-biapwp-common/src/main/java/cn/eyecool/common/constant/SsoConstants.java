package cn.eyecool.common.constant;

/**
 * 单点登录相关常量
 * 
 * @author mawj
 * @date 2020/09/23
 */
public class SsoConstants {

    /** web前端地址重定向类型(内部路由跳转) */
    public static final String WEB_REDIRECT_TYPE_INNER = "inner";

    /** web前端地址重定向类型(外部地址跳转) */
    public static final String WEB_REDIRECT_TYPE_OUT = "out";

    /** web前端默认重定向地址 */
    public static final String WEB_DEFAULT_REDIRECT_URL = "/index";

    /** web前端登录页面重定向地址参数KEY */
    public static final String WEB_REDIRECT_PARAM_KEY = "redirect";

    /** web前端登录页面重定向地址类型参数KEY */
    public static final String WEB_REDIRECT_TYPE_PARAM_KEY = "redirect_type";

    /** web前端重定向应用终端ID参数KEY */
    public static final String WEB_REDIRECT_CLIENT_ID_PARAM_KEY = "client_id";

    /** 登录页跳转来源KEY */
    public static final String SSO_PAGE_ORIGIN_KEY = "sso_from";

    /** 登录页跳转来源 */
    public static final String SSO_PAGE_ORIGIN_LOGOUT_VALUE = "logout";

    /** 单点登录客户端认证信息(id:secrect) */
    public static final String SSO_CLIENT_CREDENTIAL_HEADER = "ClientCredential";

    /** 终端管理 cache key */
    public static final String SYS_CLIENT_KEY = "sys_client:";

    /** 终端认证前缀 */
    public static final String CLIENT_CREDENTIAL_PREFIX = "Basic ";

}
