package cn.eyecool.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import cn.eyecool.common.core.domain.model.LoginUser;

/**
 * 当前登录用户Holder
 * 
 * @author mawj
 * @date 2023/09/25
 */
public class LoginUserContextHolder {
    /**
     * 支持父子线程之间的数据传递
     */
    private final static ThreadLocal<LoginUser> THREAD_LOCAL_LOGINUSER = new TransmittableThreadLocal<>();

    /**
     * THREAD_LOCAL_LOGINUSER设置登录用户<br/>
     * <b>谨慎使用此方法,避免嵌套调用。</b>
     * 
     * @param loginUser 当前登录用户
     */
    public static void setLoginUser(LoginUser loginUser) {
        THREAD_LOCAL_LOGINUSER.set(loginUser);
    }

    /**
     * 获取THREAD_LOCAL_LOGINUSER中的登录用户
     * 
     * @return loginUser
     */
    public static LoginUser getLoginUser() {
        return THREAD_LOCAL_LOGINUSER.get();
    }

    /**
     * 清除LoginUser
     */
    public static void clear() {
        THREAD_LOCAL_LOGINUSER.remove();
    }
}
