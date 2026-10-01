package cn.eyecool.framework.security.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.framework.security.token.BioDataAuthenticationToken;

public class BioDataLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private String usernameParameter = "username";
    private String bioDataParameter = "bioData";
    private String sdkTypeParameter = "sdkType";
    private boolean postOnly = true;

    public BioDataLoginAuthenticationFilter() {
        /**
         * 设置该过滤器对POST请求/login进行拦截
         */
        super(new AntPathRequestMatcher("/bioLogin", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            /**
             * 从http请求中获取用户输入的用户名和密码信息
             * 这里接收的是form形式的参数，如果要接收json形式的参数，修改这里即可
             */
            String username = this.obtainUsername(request);
            String bioData = this.obtainBioData(request);
            String sdkType = this.obtainSdkType(request);
            if(StringUtils.isEmpty(username) && StringUtils.isEmpty(bioData)) {
                throw new UsernameNotFoundException(MessageUtils.message("login.auth.get.user.authentication.failed"));
            }
            /**
             * 使用用户输入的用户名和密码信息创建一个未认证的用户认证Token
             */
            BioDataAuthenticationToken authRequest = new BioDataAuthenticationToken(username, bioData,sdkType);
            /**
             * 设置一些详情信息
             */
            this.setDetails(request, authRequest);
            /**
             * 通过AuthenticationManager调用相应的AuthenticationProvider进行用户认证
             */
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }

    protected String obtainBioData(HttpServletRequest request) {
        return request.getParameter(this.bioDataParameter);
    }

    protected String obtainSdkType(HttpServletRequest request) {
        return request.getParameter(this.sdkTypeParameter);
    }

    protected void setDetails(HttpServletRequest request, BioDataAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setBioDataParameter(String bioDataParameter) {
        Assert.hasText(bioDataParameter, "Password parameter must not be empty or null");
        this.bioDataParameter = bioDataParameter;
    }

    public void setSdkTypeParameter(String sdkTypeParameter) {
        Assert.hasText(sdkTypeParameter, "Password parameter must not be empty or null");
        this.sdkTypeParameter = sdkTypeParameter;
    }


    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getUsernameParameter() {
        return this.usernameParameter;
    }

    public final String getBioDataParameter() {
        return this.bioDataParameter;
    }

    public final String getSdkTypeParameter() {
        return this.sdkTypeParameter;
    }
}
