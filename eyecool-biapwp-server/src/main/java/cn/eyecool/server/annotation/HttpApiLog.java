package cn.eyecool.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP请求日志注解
 * 
 * @author mawj
 * @date 2021/05/13
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpApiLog {
    /**
     * 接口描述
     */
    public String desc() default "HTTP统一调用接口";

    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     */
    public boolean isSaveResponseData() default true;

    /**
     * 不需要保存请求数据的交易码(isSaveRequestData==true时起作用)
     */
    public String[] excludeTransForSaveReq() default {};

    /**
     * 不需要保存响应数据的交易码(isSaveResponseData==true时起作用)
     */
    public String[] excludeTransForSaveResp() default {};

}
