package cn.eyecool.framework.config.mybatisplus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 多租户配置中心
 * 
 * @author mawj
 * @date 2020/11/05
 */
@Configuration
@AutoConfigureBefore(MybatisPlusConfig.class)
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfiguration {

    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 新多租户插件配置
     * 
     * @return
     */
    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        return new TenantLineInnerInterceptor(new TenantLineHandler() {
            /**
             * 获取租户ID
             * 
             * @return
             */
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId != null) {
                    return new StringValue(TenantContextHolder.getTenantId());
                }
                return new NullValue();
            }

            /**
             * 获取多租户的字段名
             * 
             * @return String
             */
            @Override
            public String getTenantIdColumn() {
                return tenantProperties.getColumn();
            }

            /**
             * 过滤不需要根据租户隔离的表 这是 default 方法,默认返回 false 表示所有表都需要拼多租户条件
             * 
             * @param tableName 表名
             */
            @Override
            public boolean ignoreTable(String tableName) {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId == null) {// 没有租户限制
                    return true;
                }
                if (UserConstants.SUPER_TENANT.equals(tenantId) && "sys_tenant".equals(tableName)) {
                    return true;
                }
                return tenantProperties.getIgnoreTables().stream()
                    .anyMatch((t) -> t.equalsIgnoreCase(tableName) || match(t, tableName.toLowerCase()));
            }
        });
    }

    /**
     * 匹配资料
     *
     * @param patternPath 模糊匹配表达式
     * @param requestPath 待匹配的url
     * @return
     */
    private static boolean match(String patternPath, String requestPath) {
        if (StringUtils.isEmpty(patternPath) || StringUtils.isEmpty(requestPath)) {
            return false;
        }
        PathMatcher matcher = new AntPathMatcher();
        return matcher.match(patternPath, requestPath);
    }
}