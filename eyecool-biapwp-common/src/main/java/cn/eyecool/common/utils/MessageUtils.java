package cn.eyecool.common.utils;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.utils.spring.SpringUtils;

/**
 * 获取i18n资源文件
 * 
 * @author admin
 */

public class MessageUtils {

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args) {
        
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        try {
            return messageSource.getMessage(code, args, getLocale());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return code;
    }

    private static Locale getLocale() {
        String language = EyecoolConfig.getLanguage();
        if ("zh".equals(language)) {
            return Locale.CHINA;
        }
        if ("en".equals(language)) {
            return Locale.US;
        }
        return LocaleContextHolder.getLocale();
    }

}
