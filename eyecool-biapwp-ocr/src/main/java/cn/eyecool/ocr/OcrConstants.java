package cn.eyecool.ocr;

/**
 * Description Package cn.eyecool.biapwp.ocr
 *
 * @author sunhuayu Date on 2020/12/18
 */
public interface OcrConstants {
    /**
     * ocr图片存放日志
     */
    String OCR_LOG_IMAGE_DIR_KEY = "busi.orc.image.dir";
    String OCR_LOG_IMAGE_PREFIX = "data:jpg;base64,";

    interface Result {
        // 正常
        String SUCCESS = "1";
        // 停用
        String FAIL = "0";
    }

    interface OCR_TYPE {
        String OCR_LOG_IDCARD_FRONT = "OCR_LOG_IDCARD_FRONT";// ocr身份证正面识别
        String OCR_LOG_IDCARD_TEMP = "OCR_LOG_IDCARD_TEMP";// ocr临时身份证识别
        String OCR_LOG_IDCARD_BACK = "OCR_LOG_IDCARD_BACK";// ocr身份证反面识别
        String OCR_LOG_BANK_CARD = "OCR_LOG_BANK_CARD";// ocr银行卡识别
        String OCR_LOG_BUSINESS_LIC = "OCR_LOG_BUSINESS_LIC";// ocr营业执照识别
        String OCR_LOG_DRIVER_LIC = "OCR_LOG_DRIVER_LIC";// ocr驾驶证识别
        String OCR_LOG_DRIVING_LIC = "OCR_LOG_DRIVING_LIC";// ocr行驶证正面识别
        String OCR_LOG_HK_MAC_PASS = "OCR_LOG_HK_MAC_PASS";// ocr港澳通行证识别
        String OCR_LOG_PASSPORT = "OCR_LOG_PASSPORT";// ocr护照识别
    }
}
