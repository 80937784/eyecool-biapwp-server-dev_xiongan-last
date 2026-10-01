package cn.eyecool.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

import cn.eyecool.framework.security.provider.BioDataAuthenticationProvider;
import cn.eyecool.framework.web.service.BioDataUserDetailsServiceImpl;

/**
 * 自定义的用户名密码认证配置类
 */
@Component
public class BioDataAuthenticationConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
  @Autowired
  private BioDataUserDetailsServiceImpl userDetailsService;
  @Autowired
  private BioDataAuthenticationProvider bioDataAuthenticationProvider;
  @Override
  public void configure(HttpSecurity http) throws Exception {
    bioDataAuthenticationProvider.setUserDetailsService(userDetailsService);

    http.authenticationProvider(bioDataAuthenticationProvider);
  }

}
