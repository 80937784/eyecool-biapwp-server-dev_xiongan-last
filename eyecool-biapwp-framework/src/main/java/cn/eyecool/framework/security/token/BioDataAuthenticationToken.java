package cn.eyecool.framework.security.token;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class BioDataAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private final Object principal;

    /**
     * bioData
     */
    private Object bioData;

    private String sdkType;

    /**
     * 创建未认证的用户名密码认证对象
     */
    public BioDataAuthenticationToken(Object principal, Object bioData, String sdkType) {
        super((Collection<? extends GrantedAuthority>)null);
        this.principal = principal;
        this.bioData = bioData;
        this.sdkType = sdkType;
        this.setAuthenticated(false);
    }

    /**
     * 创建已认证的用户密码认证对象
     */
    public BioDataAuthenticationToken(Object principal, Object bioData, String sdkType,
        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.bioData = bioData;
        this.sdkType = sdkType;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.bioData;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public String getSdkType() {
        return this.sdkType;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        } else {
            super.setAuthenticated(false);
        }
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.bioData = null;
    }

}