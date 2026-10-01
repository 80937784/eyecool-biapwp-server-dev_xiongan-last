package cn.eyecool.healthcode.constant;

import cn.eyecool.common.utils.MessageUtils;

/**
 * @Description HealthCodeConstants 健康码模块常量
 * @author zfx
 * @version 2021/1/29 18:10
 **/
public interface HealthCodeConstants {
    String HTTP_IDCARNO_ISNULL = MessageUtils.message("health.code.constants.idcard.empty");
    String HTTP_NAME_ISNULL = MessageUtils.message("health.code.constants.name.empty");
    String HTTP_DEVICECODE_ISNULL =  MessageUtils.message("health.code.constants.devicecode.empty");
    String HTTP_DEVICECODE_NOTEXISTS =  MessageUtils.message("health.code.constants.devicecode.not.exists");
    String HTTP_REGION_ISNULL =  MessageUtils.message("health.code.constants.region.empty");
    String HTTP_REQUESTSEQ_ISNULL =  MessageUtils.message("health.code.constants.request.serial.empty");
    String HTTP_REGION_ERROR = MessageUtils.message("health.code.constants.request.region.error");
    String HTTP_HEALTH_REQUEST_ERROR = MessageUtils.message("health.code.constants.request.failed");
    String HTTP_RESULT_PASS = MessageUtils.message("health.code.constants.pass");
    String HTTP_NOCHEACK_PASS = MessageUtils.message("health.code.constants.nocheck.pass");
    String CODE_GREEN = MessageUtils.message("health.code.constants.greencode");
    String CODE_YELLOW = MessageUtils.message("health.code.constants.yellowcode");
    String CODE_RED = MessageUtils.message("health.code.constants.redcode");
    /** 健康码访问地址 */
    String DICT_HEALTH_URL = "healthcode.request.url";
    String RESULT_CODE = "code";
    String REGION_SHANDONG = "shandong";
    String REGION_JINAN = "eyecool_jinan";
    String RESULT_MSG = "msg";
    String RESULT_DATA = "data";
    String DICT_CERT_TYPE = "sys_cert_type";
}
