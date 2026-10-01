package cn.eyecool.framework.config.mybatisplus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

import cn.eyecool.common.config.tenant.TenantProperties;

/**
 * MybatisPlus配置中心
 * 
 * @author mawj
 * @date 2020/10/12
 */
@Configuration
public class MybatisPlusConfig {

    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private TenantLineInnerInterceptor tenantLineInnerInterceptor;

    /**
     * 单页分页条数限制(默认无限制,参见 插件#handlerLimit 方法)
     */
    @SuppressWarnings("unused")
    private static final Long MAX_LIMIT = 1000L;

    /**
     * 新多租户插件配置,一缓和二缓遵循mybatis的规则,需要设置MybatisConfiguration#useDeprecatedExecutor = false 避免缓存万一出现问题
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        boolean enableTenant = tenantProperties.getEnabled();
        if (enableTenant) {
            interceptor.addInnerInterceptor(tenantLineInnerInterceptor);
        }
        // 如果用了分页插件注意先 add TenantLineInnerInterceptor 再 add
        // PaginationInnerInterceptor
        // 用了分页插件必须设置 MybatisConfiguration#useDeprecatedExecutor = false
        // PaginationInnerInterceptor paginationInnerInterceptor = new
        // PaginationInnerInterceptor();
        // paginationInnerInterceptor.setMaxLimit(MAX_LIMIT);
        // interceptor.addInnerInterceptor(paginationInnerInterceptor);
        // 防止全表更新与删除插件: BlockAttackInnerInterceptor
        BlockAttackInnerInterceptor blockAttackInnerInterceptor = new BlockAttackInnerInterceptor();
        interceptor.addInnerInterceptor(blockAttackInnerInterceptor);
        return interceptor;
    }

    @SuppressWarnings("deprecation")
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }
}