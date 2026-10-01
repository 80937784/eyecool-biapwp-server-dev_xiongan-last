package cn.eyecool.common.core.domain.http;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * HTTP接口状态码常量定义
 * 
 * @author admin
 * @date 2019年11月4日
 */
public class HttpAjaxResult extends AjaxResult {

    private static final long serialVersionUID = 1L;

    public static final String HTTP_SUCC_CODE = Constants.SUCCESS;
    public static final String HTTP_ERR_CODE = Constants.FAIL;

    public HttpAjaxResult() {
        super();
    }

    public HttpAjaxResult(String code, String msg) {
        super.put(AjaxResult.CODE_TAG, code);
        super.put(AjaxResult.MSG_TAG, msg);
    }

    public HttpAjaxResult(String code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     * 
     * @return
     */
    public static AjaxResult httpSuccess() {
        return new HttpAjaxResult(HTTP_SUCC_CODE, MessageUtils.message("http.ajax.result.operator.success"));
    }

    /**
     * 返回成功消息
     * 
     * @param msg
     * @param data
     * @return
     */
    public static AjaxResult httpSuccess(String msg, Object data) {
        return new HttpAjaxResult(HTTP_SUCC_CODE, msg, data);
    }

    /**
     * 返回成功消息
     * 
     * @param msg
     * @return
     */
    public static AjaxResult httpSuccess(String msg) {
        return new HttpAjaxResult(HTTP_SUCC_CODE, msg);
    }

    /**
     * 返回成功消息
     * 
     * @param data
     * @return
     */
    public static AjaxResult httpSuccess(Object data) {
        return new HttpAjaxResult(HTTP_SUCC_CODE, MessageUtils.message("http.ajax.result.operator.success"), data);
    }

    /**
     * 返回错误消息
     * 
     * @return
     */
    public static AjaxResult httpError() {
        return new HttpAjaxResult(HTTP_ERR_CODE, MessageUtils.message("http.ajax.result.operator.success"));
    }

    /**
     * 返回错误消息
     * 
     * @param msg
     * @return
     */
    public static AjaxResult httpError(String msg) {
        return new HttpAjaxResult(HTTP_ERR_CODE, msg);
    }

    /**
     * 返回错误信息
     * 
     * @param msg
     * @param data
     * @return
     */
    public static AjaxResult httpError(String msg, Object data) {
        return new HttpAjaxResult(HTTP_ERR_CODE, msg, data);
    }

    /**
     * 返回错误消息
     * 
     * @param data
     * @return
     */
    public static AjaxResult httpError(Object data) {
        return new HttpAjaxResult(HTTP_ERR_CODE, MessageUtils.message("http.ajax.result.operator.failed"), data);
    }

    /**
     * 返回错误消息
     * 
     * @param code
     * @param msg
     * @return
     */
    public static AjaxResult httpError(String code, String msg) {
        return new HttpAjaxResult(code, msg);
    }

    /**
     * 返回错误消息
     * 
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public static AjaxResult httpError(String code, String msg, Object data) {
        return new HttpAjaxResult(code, msg, data);
    }

    /**
     * 返回公共参数校验错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult globalValidError(String msg) {
        return httpError("1001", msg);
    }

    /**
     * 时间戳校验错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult timestampValidError() {
        String msg = MessageUtils.message("http.ajax.result.time.invalid", System.currentTimeMillis());
        return globalValidError(msg);
    }

    /**
     * 重复请求错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult nonceRepeatError() {
        String msg = MessageUtils.message("http.ajax.result.nonce.invalid");
        return httpError("1002", msg);
    }

    /**
     * 应用系统不存在错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult appKeyNotExistsError() {
        String msg = MessageUtils.message("http.ajax.result.appkey.not.exists");
        return httpError("1003", msg);
    }

    /**
     * 请求接口不存在错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult transCodeNotExistsError(String transCode) {
        String msg = MessageUtils.message("http.ajax.result.transcode.interface.not.exists", transCode);
        return httpError("1004", msg);
    }

    /**
     * 签名验证不通过
     * 
     * @param msg
     * @return
     */
    public static AjaxResult signIllegalError() {
        String msg = MessageUtils.message("http.ajax.result.sign.check.failed");
        return httpError("1005", msg);
    }

    /**
     * 没有接口访问权限
     * 
     * @param msg
     * @return
     */
    public static AjaxResult interfaceAuthFailedError(String transCode) {
        String msg = MessageUtils.message("http.ajax.result.interface.no.auth", transCode);
        return httpError("1006", msg);
    }

    /**
     * 接口访问权限过期
     * 
     * @param msg
     * @return
     */
    public static AjaxResult interfaceAuthExpireError(String transCode) {
        String msg = MessageUtils.message("http.ajax.result.interface.expired", transCode);
        return httpError("1006", msg);
    }

    /**
     * 业务数据校验错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult businessDataValidError(String msg) {
        return httpError("1100", msg);
    }

    /**
     * 系统繁忙
     * 
     * @param msg
     * @return
     */
    public static AjaxResult systemBusyError() {
        return httpError("-1", MessageUtils.message("http.ajax.result.system.busy"));
    }

    /**
     * 系统繁忙
     * 
     * @param msg
     * @return
     */
    public static AjaxResult systemBusyError(String msg) {
        return httpError("-1", msg);
    }

    /**
     * 业务错误
     * 
     * @param msg
     * @return
     */
    public static AjaxResult businessError(String msg) {
        return httpError("1101", msg);
    }

}
