package cn.eyecool.system.constant;

/**
 * Redis-key常量
 * 
 * @author mawj
 * @date 2021/06/02
 */
public class RedisKeyConstants {

    /** 租户信息redis缓存key前缀 */
    public static final String TENANT_REDIS_KEY_PREFIX = "tenant:";

    /** app应用信息redis缓存key前缀 */
    public static final String APP_MGR_REDIS_KEY_PREFIX = "appmgr:";

    /** 租户接口权限redis缓存key前缀 */
    public static final String TENANT_INTERFACE_REDIS_KEY_PREFIX = "tenant:interfaces:";

    /** App接口权限redis缓存key前缀 */
    public static final String APP_INTERFACE_REDIS_KEY_PREFIX = "appmgr:interfaces:";

    /** 租户ID最大值缓存key */
    public static final String TENANT_MAX_ID_KEY = "tenant:tenantId:maxnum";

}
