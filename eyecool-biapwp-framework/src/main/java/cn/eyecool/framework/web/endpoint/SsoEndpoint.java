package cn.eyecool.framework.web.endpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.constant.SsoConstants;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.domain.SysClientDetails;
import cn.eyecool.system.service.ISysClientDetailsService;

/**
 * 单点登录端点
 * 
 * @author mawj
 * @date 2020/10/30
 */
@RestController
public class SsoEndpoint {

    private final Logger logger = LoggerFactory.getLogger(SsoEndpoint.class);

    @Value("${sso.loginPage:localhost/login}")
    private String ssoLoginPage;
    @Autowired
    private ISysClientDetailsService clientDetailsService;

    /**
     * 单点登录请求入口
     * 
     * @param param
     * @param request
     * @param response
     */
    @GetMapping(value = "/sso/login")
    public void login(@RequestParam Map<String, String> param, HttpServletRequest request,
        HttpServletResponse response) {
        String redirect = SsoConstants.WEB_DEFAULT_REDIRECT_URL;
        String redirectType = SsoConstants.WEB_REDIRECT_TYPE_INNER;
        String redirectParam = param.get(SsoConstants.WEB_REDIRECT_PARAM_KEY);
        String clientId = param.get(SsoConstants.WEB_REDIRECT_CLIENT_ID_PARAM_KEY);
        if (StringUtils.isNotEmpty(redirectParam)) {
            redirect = redirectParam;
            redirectType = SsoConstants.WEB_REDIRECT_TYPE_OUT;
            // 根据clientId判断当前redirectURI是否合法
            checkClient(clientId, redirect);
        }
        String url = ssoLoginPage + "?" + SsoConstants.WEB_REDIRECT_PARAM_KEY + "=" + redirect + "&"
            + SsoConstants.WEB_REDIRECT_TYPE_PARAM_KEY + "=" + redirectType;
        String ssoPageOrigin = param.get(SsoConstants.SSO_PAGE_ORIGIN_KEY);
        if (StringUtils.isNotEmpty(ssoPageOrigin)) {
            url += "&" + SsoConstants.SSO_PAGE_ORIGIN_KEY + "=" + ssoPageOrigin.toString();
        }
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 校验单点登录客户端
     * 
     * @param clientId
     * @param redirectUri
     */
    private void checkClient(String clientId, String redirectUri) {
        if (StringUtils.isBlank(clientId)) {
            logger.warn("the sso client_id can not be null !");
            throw new CustomException("the sso client_id can not be null  !");
        }

        SysClientDetails clientDetails = clientDetailsService.selectSysClientDetailsById(clientId);
        if (StringUtils.isNull(clientDetails)) {
            logger.warn("the sso client for client_id = [{}] is not exists !", clientId);
            throw new CustomException("the sso client for client_id =" + clientId + " is not exists !");
        }

        String webServerRedirectUri = clientDetails.getWebServerRedirectUri();
        if (StringUtils.isBlank(webServerRedirectUri)) {
            logger.warn("the sso client's redirect_uri for client_id = [{}] can not be matched", clientId);
            throw new CustomException("sso client's redirect_uri for client_id =" + clientId + " can not be matched !");
        }

        String[] redirectUriArray = Convert.toStrArray(webServerRedirectUri);
        boolean anyMatch = Arrays.stream(redirectUriArray).anyMatch(it -> redirectUri.startsWith(it));
        if (!anyMatch) {
            logger.warn("the sso client's redirect_uri for client_id = [{}] can not be matched", clientId);
            throw new CustomException(
                "the sso client's redirect_uri for client_id =" + clientId + " can not be matched !");
        }
    }
}
