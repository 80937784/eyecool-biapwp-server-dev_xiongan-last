package cn.eyecool.framework.security.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.framework.security.token.BioDataAuthenticationToken;
import cn.eyecool.framework.web.service.BioDataValidateService;
@Component
public class BioDataAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;
    @Autowired
    private BioDataValidateService bioDataValidateService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        BioDataAuthenticationToken authenticationToken = (BioDataAuthenticationToken) authentication;
        String username = (String) authenticationToken.getPrincipal();
        String bioData = (String) authenticationToken.getCredentials();
        String sdkType = authenticationToken.getSdkType();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        SysUser user = ((LoginUser) userDetails).getUser();
        bioDataValidateService.validate(sdkType, user, bioData);

        BioDataAuthenticationToken customUsernamePasswordAuthenticationToken = new BioDataAuthenticationToken(
                userDetails, authenticationToken.getCredentials(), "", userDetails.getAuthorities());
        customUsernamePasswordAuthenticationToken.setDetails(authenticationToken.getDetails());
        return customUsernamePasswordAuthenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 判断 authentication 是不是 SmsCodeAuthenticationToken 的子类或子接口
        return BioDataAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

}