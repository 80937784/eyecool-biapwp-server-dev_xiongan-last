/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : SDHealthCodeServiceImpl
 ******************************************************************************/
package cn.eyecool.healthcode.service.impl;

import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.healthcode.constant.HealthCodeConstants;
import cn.eyecool.healthcode.param.HealthCodeRequest;
import cn.eyecool.healthcode.service.ISDHealthCodeService;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * 山东健康码查询实现类
 *
 * @author zfx
 * @since 2021/3/5 17:28
 **/
@SuppressWarnings("deprecation")
@Service
public class SDHealthCodeServiceImpl implements ISDHealthCodeService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCodeServiceImpl.class);
    /**
     * 省平台固定 aeskey
     */
    public static final String AES_KEY = "tVaRHye4WddpH9WRs5vBBWsy62xuIKst";

    @SuppressWarnings("resource")
    @Override
    public AjaxResult querySddzjkm(HealthCodeRequest healthCodeRequest, String url) {
        String idCarNo = healthCodeRequest.getIdCarNo();
        String name = healthCodeRequest.getName();
        // X-Signature：调用方生成的签名值，生成方式是X-Client-Id+X-Timestamp+X-Nonce组合字符
        String XClientId = healthCodeRequest.getxClientId();
        // 浪潮页面上，通过短信验证码才能看的那个
        String appSecret = healthCodeRequest.getxCientSecert();
        // 授权编码
        String appid = "xiwynnkx31vi2xzh6tul";
        // 授权秘钥
        String appSecret1 = "c2ad06d8615b49ab8f22dee1404f4585";
        // 时间戳
        String XTimestamp = System.currentTimeMillis() + "";
        // 随机数
        String random = RandomStringUtils.random(15, true, true);
        // header签名
        String XSignature = XClientId + XTimestamp + random;
        String sign = getSignature(XSignature, appSecret);
        // param签名
        String XSignature1 = appid + "&" + XTimestamp + "&" + random;
        String sign1 = getSignature1(XSignature1, appSecret1);

        // 查询参数
        JSONObject paramJsonData = new JSONObject();
        idCarNo = encByAES(idCarNo);
        String pname = encByAES(name);
        paramJsonData.put("departureTime", "");
        paramJsonData.put("cardType", "01");
        paramJsonData.put("cardNo", idCarNo);
        paramJsonData.put("fromCityCode", "0");
        paramJsonData.put("outside", 1);
        paramJsonData.put("name", pname);
        paramJsonData.put("fromCity", "");
        String personBodyParam = paramJsonData.toString();
        // URL编码转义特殊字符
        personBodyParam = personBodyParam.replace(":", "%3A");
        personBodyParam = personBodyParam.replace("{", "%7B");
        personBodyParam = personBodyParam.replace("}", "%7D");
        personBodyParam = personBodyParam.replace("\"", "%22");
        String reqUrl = url + "?paramJsonData=" + personBodyParam + "&nonce_str=" + random + "&sign=" + sign1
            + "&appid=xiwynnkx31vi2xzh6tul" + "&time=" + XTimestamp;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost loginhttpPost = new HttpPost(reqUrl);
        loginhttpPost.addHeader("Content-Type", "x-www-form-urlencoded");
        loginhttpPost.setHeader("X-Client-Id", XClientId);
        loginhttpPost.setHeader("X-Timestamp", XTimestamp);
        loginhttpPost.setHeader("X-Nonce", random);
        loginhttpPost.setHeader("X-Signature", sign);
        try {
            JSONObject resultJson;
            String resultStr = "";
            String reqResState = "";
            // 拿到结果
            try {
                HttpResponse response = httpClient.execute(loginhttpPost);
                resultStr = EntityUtils.toString(response.getEntity());
                StatusLine statusLine = response.getStatusLine();
                reqResState = statusLine.getReasonPhrase();
                resultJson = JSONObject.parseObject(resultStr);
                logger.info("Call health code request result [{}]resultJson[{}]", reqResState, resultJson);
            } catch (Exception e) {
                // 用于大批量调用，如果接口拒绝访问，停留5秒再访问
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    logger.error(e1.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
                logger.error("Call health code request result[{}]message[{}]", reqResState, e.getMessage(), e);
                return HttpAjaxResult.businessError(getResultMsg("4", reqResState + resultStr));
            }
            // 判断签名验证结果
            if (resultJson.containsKey("code") && resultJson.getString("code").equals("200")) {
                JSONObject data = resultJson.getJSONObject("data");
                JSONObject data1 = data.getJSONObject("data");
                JSONObject data2 = data1.getJSONObject("data");
                Boolean success = data2.getBoolean("success");
                if (success) {
                    JSONObject data3 = data2.getJSONObject("data");
                    if (data3.containsKey("isHaveHealthCode") && data3.getBoolean("isHaveHealthCode")) {
                        // 健康码等级： 0，绿色； 1，黄色； 2，红色
                        String state = data3.getString("state");
                        String stateInfo = data3.getString("stateInfo");
                        HashMap<String, String> resData = Maps.newHashMap();
                        resData.put("state", state);
                        String stuf = "";
                        if (!StringUtils.isBlank(stateInfo)) {
                            stuf = "-" + stateInfo;
                        }
                        if (Constants.STATUS_ZERO.equals(state)) {
                            resData.put("msg", HealthCodeConstants.CODE_GREEN + stuf);
                            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_GREEN, resData);
                        } else if (Constants.STATUS_ONE.equals(state)) {
                            resData.put("msg", HealthCodeConstants.CODE_YELLOW + stuf);
                            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_YELLOW, resData);
                        } else {
                            resData.put("msg", HealthCodeConstants.CODE_RED + stuf);
                            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_RED, resData);
                        }
                    } else if (data3.containsKey("isHaveHealthCode") && !data3.getBoolean("isHaveHealthCode")) {
                        // 未上报健康码信息
                        return HttpAjaxResult.businessError(
                            getResultMsg(data3.getString("state"), data3.getString("stateInfo") +MessageUtils.message("health.code.service.sd.no.health.info")));
                    } else {
                        // 其他情况
                        return HttpAjaxResult.businessError(getResultMsg("4", MessageUtils.message("health.code.service.sd.more.request")));
                    }

                } else {
                    // 其他情况
                    return HttpAjaxResult.businessError(getResultMsg("4", data2.getString("msg")));
                }
            } else if (resultJson.containsKey("code") && resultJson.containsKey("msg")) {
                // 其他情况
                return HttpAjaxResult.businessError(getResultMsg("4", resultJson.getString("msg")));
            } else {
                // 其他情况
                return HttpAjaxResult.businessError(getResultMsg("4", MessageUtils.message("health.code.service.sd.more.request")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return HttpAjaxResult.businessError(getResultMsg("4", e.getMessage()));

        }
    }

    /**
     * getErrorEmg 整理失败数据返回内容
     *
     * @param state
     * @param resultStr
     * @return java.lang.String
     * @author zfx
     * @since 2021/3/5 19:46
     */
    private String getResultMsg(String state, String resultStr) {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("state", state);
        map.put("msg", resultStr);
        return JSONObject.toJSONString(map);
    }

    public static String getSignature(String signatureReqStr, String secretKey) {
        Mac sha256_HMAC;
        String result = "";
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            result = Base64.encode(sha256_HMAC.doFinal(signatureReqStr.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSignature1(String signatureReqStr, String secretKey) {
        Mac sha1_HMAC;
        String result = "";
        try {
            sha1_HMAC = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            sha1_HMAC.init(secret_key);
            result = Base64.encode(sha1_HMAC.doFinal(signatureReqStr.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String encByAES(String data) {
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, AES_KEY.getBytes());
        byte[] strByte = aes.encrypt(data);
        String encode = Base64.encode(strByte);
        encode = encode.replace("+", "%2b");
        return encode;
    }
}
