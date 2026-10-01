package cn.eyecool.framework.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.framework.web.service.TokenService;

/**
 * 租户过滤器(为当前线程设置租户ID)
 * 
 * @author mawj
 * @date 2020/11/04
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    @Autowired
    private TokenService tokenService;
    @Autowired
    private TenantProperties tenantProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        try {
            if (tenantProperties.getEnabled()) {
                LoginUser loginUser = tokenService.getLoginUser(request);
                String tenantIdParam = request.getParameter("tenantId");
                String tenantId = null;
                if (StringUtils.isNotNull(loginUser)) {
                    String username = loginUser.getUsername();
                    String currUserTenantId = loginUser.getUser().getTenantId();
                    if (log.isDebugEnabled()) {
                        log.debug("current user is [{}] and tenantId is [{}], query tenant param is [{}]", username,
                            currUserTenantId, tenantIdParam);
                    }
                    if (UserConstants.SUPER_TENANT.equals(currUserTenantId) && StringUtils.isNotBlank(tenantIdParam)) {
                        tenantId = tenantIdParam;
                    } else {
                        tenantId = currUserTenantId;
                    }
                }

                // 保存租户id
                if (StringUtils.isNotEmpty(tenantId)) {
                    TenantContextHolder.setTenantId(tenantId);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

}
