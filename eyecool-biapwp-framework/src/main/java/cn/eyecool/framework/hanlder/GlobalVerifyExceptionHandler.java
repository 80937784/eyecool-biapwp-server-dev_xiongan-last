package cn.eyecool.framework.hanlder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.MessageUtils;

@ControllerAdvice
public class GlobalVerifyExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalVerifyExceptionHandler.class);

    @ExceptionHandler(value=MethodArgumentNotValidException.class)
    @ResponseBody
    public AjaxResult handlerException(MethodArgumentNotValidException ex) {
        // Map<String,Object> result = new HashMap<>();
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        StringBuffer sb = new StringBuffer();
        errors.forEach(error -> {
            String msg = error.getDefaultMessage();
            log.info("ObjectError getDefaultMessage(),the result is: "+msg);
            if (StringUtils.isNotBlank(msg)) {
                sb.append(MessageUtils.message(msg));
            }
        });
        return AjaxResult.error(sb.toString());
    }
}