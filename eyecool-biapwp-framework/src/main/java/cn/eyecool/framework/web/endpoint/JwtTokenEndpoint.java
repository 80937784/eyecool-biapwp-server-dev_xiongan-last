package cn.eyecool.framework.web.endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 校验JWT_TOKEN端点
 * 
 * @author mawj
 * @date 2020/10/29
 */
@RestController
public class JwtTokenEndpoint {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenEndpoint.class);

    /**
     * 校验TOKEN
     * 
     * @param request
     * @return
     */
    @PostMapping(value = "/jwt/check_token")
    public AjaxResult checkToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        LoginUser loginUser = null;
        try {
            loginUser = SecurityUtils.getLoginUser();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        response.put("active", StringUtils.isNotNull(loginUser));
        return AjaxResult.success(response);
    }

    /**
     * 获取登录用户信息
     * 
     * @param request
     * @return
     */
    @PostMapping(value = "/jwt/user")
    public AjaxResult getLoginUserDetail(HttpServletRequest request) {
        LoginUser loginUser = null;
        try {
            loginUser = SecurityUtils.getLoginUser();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return AjaxResult.success(loginUser);
    }

}
