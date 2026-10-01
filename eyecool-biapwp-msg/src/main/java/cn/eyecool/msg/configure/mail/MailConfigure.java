package cn.eyecool.msg.configure.mail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件发送配置
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
@Configuration
public class MailConfigure {

    /**
     * 邮件发送Bean实例（配置多例，灵活设置不同的邮件服务器信息）
     * 
     * @author mawenjun
     * @return
     * @date 2020年3月22日
     *
     */
    @Bean
    @Scope("prototype")
    public JavaMailSenderImpl mailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MailProperties mailProperties() {
        return new MailProperties();
    }
}