package cn.eyecool.msg.util;

import java.util.Map;
import java.util.Properties;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件工具类(设置发送属性)
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月22日
 *
 */
public class MailUtil {

    /**
     * 应用邮件服务器属性
     * 
     * @author mawenjun
     * @param sender
     * @param properties
     * @date 2020年3月22日
     *
     */
    public static void applyProperties(JavaMailSenderImpl sender, MailProperties properties) {
        sender.setHost(properties.getHost());
        if (properties.getPort() != null) {
            sender.setPort(properties.getPort());
        }
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setProtocol(properties.getProtocol());
        if (properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(properties.getDefaultEncoding().name());
        }
        if (!properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(properties.getProperties()));
        }
    }

    /**
     * 获取额外拓展属性
     * 
     * @author mawenjun
     * @param source
     * @return
     * @date 2020年3月22日
     *
     */
    public static Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
