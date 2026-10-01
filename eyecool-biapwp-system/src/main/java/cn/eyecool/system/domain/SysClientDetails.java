package cn.eyecool.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 终端配置对象 sys_client_details
 * 
 * @author admin
 * @date 2020-11-03
 */
public class SysClientDetails extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 终端编号 */
    private String clientId;

    /** 资源ID标识 */
    @Excel(name = "sys.client.resourceids.name")
    private String resourceIds;

    /** 终端安全码 */
    @Excel(name = "sys.client.clientsecret.name")
    private String clientSecret;

    /** 终端授权范围 */
    @Excel(name = "sys.client.scope.name")
    private String scope;

    /** 服务器回调地址 */
    @Excel(name = "sys.client.serverredirecturl.name")
    private String webServerRedirectUri;

    /** 访问资源所需权限 */
    @Excel(name = "sys.client.authorities.name")
    private String authorities;

    /** 附加信息 */
    @Excel(name = "sys.client.additionalinformation.name")
    private String additionalInformation;

    /** 终端明文安全码 */
    @Excel(name = "sys.client.originsecret.name")
    private String originSecret;

    public void setClientId(String clientId) 
    {
        this.clientId = clientId;
    }

    public String getClientId() 
    {
        return clientId;
    }
    public void setResourceIds(String resourceIds) 
    {
        this.resourceIds = resourceIds;
    }

    public String getResourceIds() 
    {
        return resourceIds;
    }
    public void setClientSecret(String clientSecret) 
    {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() 
    {
        return clientSecret;
    }
    public void setScope(String scope) 
    {
        this.scope = scope;
    }

    public String getScope() 
    {
        return scope;
    }
    public void setWebServerRedirectUri(String webServerRedirectUri) 
    {
        this.webServerRedirectUri = webServerRedirectUri;
    }

    public String getWebServerRedirectUri() 
    {
        return webServerRedirectUri;
    }
    public void setAuthorities(String authorities) 
    {
        this.authorities = authorities;
    }

    public String getAuthorities() 
    {
        return authorities;
    }
    public void setAdditionalInformation(String additionalInformation) 
    {
        this.additionalInformation = additionalInformation;
    }

    public String getAdditionalInformation() 
    {
        return additionalInformation;
    }
    public void setOriginSecret(String originSecret) 
    {
        this.originSecret = originSecret;
    }

    public String getOriginSecret() 
    {
        return originSecret;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("clientId", getClientId())
            .append("resourceIds", getResourceIds())
            .append("clientSecret", getClientSecret())
            .append("scope", getScope())
            .append("webServerRedirectUri", getWebServerRedirectUri())
            .append("authorities", getAuthorities())
            .append("additionalInformation", getAdditionalInformation())
            .append("originSecret", getOriginSecret())
            .toString();
    }
}
