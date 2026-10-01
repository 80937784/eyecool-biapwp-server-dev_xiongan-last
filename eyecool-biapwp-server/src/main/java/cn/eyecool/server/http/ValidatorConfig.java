package cn.eyecool.server.http;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 参数验证器配置
 * 
 * @author admin
 * @date 2019年11月4日
 */
@Configuration
public class ValidatorConfig {

    /**
     * 配置验证器
     *
     * @return validator
     */
    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure()
            // 快速失败模式
            .failFast(true)
            // .addProperty( "hibernate.validator.fail_fast", "true" )
            .buildValidatorFactory();
        return validatorFactory.getValidator();
    }
}