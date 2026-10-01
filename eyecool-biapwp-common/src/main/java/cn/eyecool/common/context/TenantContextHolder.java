package cn.eyecool.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 租户holder
 * 
 * @author mawj
 * @date 2020/10/12
 */
public class TenantContextHolder {

    /**
     * 支持父子线程之间的数据传递
     */
    private final static ThreadLocal<String> THREAD_LOCAL_TENANT = new TransmittableThreadLocal<>();

    /**
     * THREAD_LOCAL_TENANT设置租户ID<br/>
     * <b>谨慎使用此方法,避免嵌套调用。</b>
     * 
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        THREAD_LOCAL_TENANT.set(tenantId);
    }

    /**
     * 获取THREAD_LOCAL_TENANT中的租户ID
     * 
     * @return String
     */
    public static String getTenantId() {
        return THREAD_LOCAL_TENANT.get();
    }

    /**
     * 清除tenantId
     */
    public static void clear() {
        THREAD_LOCAL_TENANT.remove();
    }
}