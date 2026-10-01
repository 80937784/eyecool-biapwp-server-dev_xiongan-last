package cn.eyecool.common.utils.sign;

/**
 * 数字综管加密类
 *
 * @author zhupy
 * @date 2023/07/24
 */
public class EyecoolPmSign {

    /**
     * 服务端生成签名, 使用公共参数即可
     *
     * @return
     */
    public static String generateSign(String appKey,String transCode, String timestamp, String nonce, String appSecrect) {
        // 源字符串拼接,按照请求参数名的字母升序排列非空请求参数(appkey->nonce->timestamp->transCode)
        String originalSignStr =
            "appkey=" + appKey + "&nonce=" + nonce + "&timestamp=" + timestamp + "&transCode=" + transCode;
        // 拼接appSecrect
        originalSignStr = originalSignStr + "&appSecrect=" + appSecrect;
        // 进行MD5加密并转为大写
        String md5 = Md5Utils.hash(originalSignStr);
        return md5.toUpperCase();
    }

}
