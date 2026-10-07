package cn.eyecool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import cn.eyecool.match.service.commons.Requester;
import cn.eyecool.match.service.commons.ServerInfo;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 *
 * @author admin
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},
    scanBasePackages = {"cn.eyecool", "com.eyecool", "org.weixin4j"})
@ImportResource(locations = {"classpath*:minisearch.xml"})
public class EyecoolApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(EyecoolApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  The application starts successfully   ლ(´ڡ`ლ)ﾞ");
    }

    @Bean
    public Requester requesterBean() {
        return Requester.newBuilder().setCaller(ServerInfo.newBuilder()
            // 必选，本应用的提供商
            .setVendor("eyecool.cn")
            // 必选，本应用启动时间
            .setStartedAt(System.currentTimeMillis())
            // 必选，本应用ID，需唯一
            .setServerId("application-001")
            // 必选，本应用IP地址
            .setHost("127.0.0.1")
            // 必选，本应用服务端口号
            .setPort(8080)
            // 可选，算法类型
            .setAlgType("face")
            // 可选，算法版本
            .setAlgVersion("1.0.1")
            // 可选，特征版本
            .setFeatVersion("v1.1.1").build()).build();
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(5000);
        return factory;
    }

    /*增加部分*/
    @Bean
    MeterRegistryCustomizer<MeterRegistry>
        configurer(@Value("${spring.application.name:actuator-prometheus}") String applicationName) {
        return (registry) -> registry.config().commonTags("application", applicationName);
    }

}
