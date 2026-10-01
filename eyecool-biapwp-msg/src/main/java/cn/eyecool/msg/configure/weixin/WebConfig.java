package cn.eyecool.msg.configure.weixin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WEB配置
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月25日
 *
 */
@Configuration
public class WebConfig {

    /**
     * 微信消息回调请求过滤器
     * 
     * @author mawenjun
     * @return
     * @date 2020年3月25日
     *
     */
    @Bean
    public FilterRegistrationBean<PlatformWeixinUrlFilter> weixinUrlFilter() {
        FilterRegistrationBean<PlatformWeixinUrlFilter> registrationBean = new FilterRegistrationBean<>();
        PlatformWeixinUrlFilter weixinUrlFilter = new PlatformWeixinUrlFilter();
        registrationBean.setFilter(weixinUrlFilter);
        List<String> url = new ArrayList<>();
        url.add("/api/mp/callback"); // 拦截请求
        registrationBean.setUrlPatterns(url);
        return registrationBean;
    }

}