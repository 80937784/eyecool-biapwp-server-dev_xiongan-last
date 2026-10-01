package cn.eyecool.framework.security.handle;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.framework.manager.AsyncManager;
import cn.eyecool.framework.manager.factory.AsyncFactory;
import cn.eyecool.framework.web.service.TokenService;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author admin
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;

    /**
     * 退出处理
     * 
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        String msg = MessageUtils.message("user.logout.success");
        if (StringUtils.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            String tenantId = loginUser.getUser().getTenantId();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(tenantId, userName, Constants.LOGOUT,msg));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.SUCCESS, msg)));
    }
}
