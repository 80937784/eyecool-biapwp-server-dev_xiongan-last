package cn.eyecool.framework.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.constant.SsoConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.domain.SysClientDetails;
import cn.eyecool.system.service.ISysClientDetailsService;

/**
 * 终端过滤器 验证终端合法性
 * 
 * @author mawj
 * @date 2020/11/06
 */
@Component
public class ClientAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientAuthenticationFilter.class);

    private static final List<String> ignoreURIList =
        Arrays.asList("/sso/login", "/profile/**", "/common/download**", "/common/download/resource**", "/druid/**",
            "/swagger-ui.html", "/swagger-resources/**", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js",
            "/webjars/**", "/*/api-docs", "/api/**", "/websocket/**", "/show/**", "/actuator/**", "/prometheus/**");

    @Autowired
    private ISysClientDetailsService clientDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        PathMatcher matcher = new AntPathMatcher();
        // 加入单元测试开关
        boolean anyMatch = EyecoolConfig.isMockEnabled() || ignoreURIList.stream()
            .anyMatch(it -> matcher.match(request.getContextPath() + it, request.getRequestURI()));
        if (anyMatch) {
            chain.doFilter(request, response);
            return;
        }
        // 获取请求头
        String credentialHeader = request.getHeader(SsoConstants.SSO_CLIENT_CREDENTIAL_HEADER);
        if (StringUtils.isBlank(credentialHeader)) {
            log.warn("client credential header is missing!");
            int code = HttpStatus.BAD_REQUEST;
            String msg =
                MessageUtils.message("client.auth.filter.get.user.authentication.failed", request.getRequestURI());
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
            return;
        }
        // 解码并进行格式验证
        String credential =
            new String(Base64.decodeBase64(credentialHeader.substring(SsoConstants.CLIENT_CREDENTIAL_PREFIX.length())));
        String[] credentialArray = Convert.toStrArray(":", credential);
        if (credentialArray == null || credentialArray.length != 2) {
            log.warn("client credential header [{}] is invalid formatter!", credentialHeader);
            int code = HttpStatus.BAD_REQUEST;
            String msg =
                MessageUtils.message("client.auth.filter.user.authentication.format.failed", request.getRequestURI());
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
            return;
        }
        String clientId = credentialArray[0];
        String clientSecrect = credentialArray[1];
        // 验证客户端是否被授权
        SysClientDetails clientDetails = clientDetailsService.selectSysClientDetailsById(clientId);
        if (StringUtils.isNull(clientDetails)) {
            log.warn("the sso client is unauthorized, client_id = [{}] !", clientId);
            int code = HttpStatus.ERROR;
            String msg = MessageUtils.message("client.auth.filter.client.unauth", request.getRequestURI());
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
            return;
        }
        // 密码匹配
        boolean matchesPassword = SecurityUtils.matchesPassword(clientSecrect, clientDetails.getClientSecret());
        if (!matchesPassword) {
            log.warn("the sso client's secrect is invalid, client_id = [{}]", clientId);
            int code = HttpStatus.ERROR;
            String msg = MessageUtils.message("client.auth.filter.secret.key.illegal", request.getRequestURI());
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
            return;
        }
        chain.doFilter(request, response);
    }
}
