package cn.eyecool.framework.security.handle;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.ServletUtils;

/**
 * 认证失败处理类 返回未授权
 * 
 * @author admin
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
        throws IOException {
        int code = HttpStatus.UNAUTHORIZED;
        String msg = MessageUtils.message("auth.entry.auth.failed", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
