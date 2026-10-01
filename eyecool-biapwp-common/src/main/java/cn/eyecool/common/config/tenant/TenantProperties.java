package cn.eyecool.common.config.tenant;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 租户属性
 * 
 * @author mawj
 * @date 2020/10/12
 */
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

    /**
     * 是否开启租户模式
     */
    private Boolean enabled = false;

    /**
     * 需要排除多租户的表
     */
    private List<String> ignoreTables = new ArrayList<>();

    /**
     * 多租户字段名称
     */
    private String column = "tenant_id";

    /**
     * 排除不进行租户隔离的sql 样例全路径：cn.eyecool.system.mapper.UserMapper.findList
     */
    private List<String> ignoreSqls = new ArrayList<>();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIgnoreTables() {
        return ignoreTables;
    }

    public void setIgnoreTables(List<String> ignoreTables) {
        this.ignoreTables = ignoreTables;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public List<String> getIgnoreSqls() {
        return ignoreSqls;
    }

    public void setIgnoreSqls(List<String> ignoreSqls) {
        this.ignoreSqls = ignoreSqls;
    }

}