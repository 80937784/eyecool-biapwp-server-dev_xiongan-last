/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : ThirdHttpServer
 ******************************************************************************/
package cn.eyecool.server.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.ip.IpUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.server.handler.XAThirdHandler;
import cn.eyecool.server.http.param.CardPassPerson;
import cn.eyecool.server.http.param.CardPassResHeader;
import cn.eyecool.server.http.param.DoorArea;

/**
 * 对接三方接口
 *
 * @author zfx
 * @since 2022/11/28 9:04
 **/
@RestController
@RequestMapping("/api/standard")
public class ThirdHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(ThirdHttpServer.class);
    // 连接超时时间
    private static int connTimeOut = 100 * 1000;
    // 读取超时时间
    private int readTimeOut = 100 * 1000;
    private static String appKey = "zLaPqltN";
    private static String appSecrect = "d098eed3e2990ca8eaec5fedda8f22b6f7a3566b";
    private static String httpUrl = "http://127.0.0.1:8701/api/standard";

    @Autowired
    private XAThirdHandler xaThirdHandler;

    /**
     * syncDoorRecord 根据一卡通接口文档，实现接口 返回代码 说明 0000 接口调用成功 1001 接口访问成功，但发生系统数据库异常 1002 数据报文不符合规范格式 1003 数据报文头约束参数不能为空 1004
     * 数据报文体约束参数不能为空
     * 
     * @param jsonData
     * @param request
     * @return java.lang.String
     * @author zfx
     * @since 2022/11/28 9:20
     */
    @ResponseBody
    @RequestMapping(value = "/syncDoorRecord", method = {RequestMethod.POST},
        produces = "application/json;charset=UTF-8")
    public Map<String, Object> syncDoorRecord(@RequestBody String jsonData, HttpServletRequest request) {
        String clientIp = IpUtils.getIpAddr(ServletUtils.getRequest());
        if (logger.isInfoEnabled()) {
            logger.info("HTTP syncDoorRecord 请求参数信息=> clientIp:[{}], jsonData:[{}]", clientIp, jsonData);
        }
        CardPassResHeader cardPassResHeader = xaThirdHandler.syncPersonData(jsonData);
        // CardPassResponse response = new CardPassResponse(cardPassResHeader);
        Map<String, Object> result = convertToResult(cardPassResHeader, StringUtils.EMPTY);
        logger.info("syncDoorRecord 业务处理结果[{}]", result);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/syncAuthority", method = {RequestMethod.POST},
        produces = "application/json;charset=UTF-8")
    public Map<String, Object> syncAuthority(@RequestBody String text) {
        String clientIp = IpUtils.getIpAddr(ServletUtils.getRequest());
        if (logger.isInfoEnabled()) {
            logger.info("HTTP syncAuthority 请求参数信息=> clientIp:[{}], text:[{}]", clientIp, text);
        }
        CardPassResHeader cardPassResHeader = xaThirdHandler.syncPersonData(text);
        // CardPassResponse response = new CardPassResponse(cardPassResHeader);
        Map<String, Object> result = convertToResult(cardPassResHeader, StringUtils.EMPTY);
        logger.info("syncAuthority 业务处理结果[{}]", result);
        return result;
    }

    /**
     * convertToResult 将结果转换为 一卡通需要的格式数据
     * 
     * @param resHeader
     * @param body
     * @return java.util.Map<java.lang.String,java.lang.String>
     * @author zfx
     * @since 2023/4/7 15:58
     */
    private Map<String, Object> convertToResult(CardPassResHeader resHeader, String body) {
        Map<String, Object> result = Maps.newHashMap();
        Map<String, String> header = Maps.newHashMap();
        header.put("commandID", resHeader.getCommandID());
        header.put("transactionID", resHeader.getTransactionID());
        header.put("timestamp", resHeader.getTimestamp());
        header.put("rspCode", resHeader.getRspCode());
        header.put("rspMessage", resHeader.getRspMessage());
        header.put("mode", resHeader.getMode());
        result.put("body", body);
        result.put("header", header);
        return result;
    }

    /**
     * sendToCardPass 模拟测试推送一卡通刷卡记录数据
     * 
     * @param jsonData
     * @param request
     * @return cn.eyecool.common.core.domain.AjaxResult
     * @author zfx
     * @since 2022/11/29 15:09
     */
    @ResponseBody
    @RequestMapping(value = "/sendToCardPass", method = {RequestMethod.POST, RequestMethod.GET},
        produces = "application/json;charset=UTF-8")
    public AjaxResult sendToCardPass(String cardNo, String areaCode) {
        AjaxResult ajaxResult = xaThirdHandler.xaThirdHandler(cardNo, areaCode);
        logger.info("syncDoorRecord 业务处理结果[{}]", ajaxResult);
        return ajaxResult;
    }

    /**
     * syncDoorRecord模拟测试推送一卡通 权限状态数据
     * 
     * @param cardNo
     * @return cn.eyecool.common.core.domain.AjaxResult
     * @author zfx
     * @since 2023/4/17 9:36
     */
    @ResponseBody
    @RequestMapping(value = "/syncAuthorityStatus", method = {RequestMethod.POST, RequestMethod.GET},
        produces = "application/json;charset=UTF-8")
    public AjaxResult syncAuthorityStatus(String cardNo) {
        if (StringUtils.isEmpty(cardNo)) {
            cardNo = "00000001";
        }
        CardPassPerson cardPassPerson = new CardPassPerson();
        cardPassPerson.setCardNo(cardNo);
        List<DoorArea> list = Lists.newArrayList();
        // DoorArea area = new DoorArea();
        // area.setAreaCode("1");
        // list.add(area);
        DoorArea area2 = new DoorArea();
        area2.setAreaCode("2");
        list.add(area2);
        cardPassPerson.setAreaList(list);
        xaThirdHandler.syncAuthorityStatus(cardPassPerson);
        logger.info("syncAuthorityStatus 完成");
        return AjaxResult.success();
    }

    /**
     * testHttp 模拟测试http接口
     * 
     * @param bizContent
     * @return java.lang.String
     * @author zfx
     * @throws @since 2022/12/6 14:13
     */
    @RequestMapping(value = "testHttp", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String testHttp(@RequestBody String bizContent) throws Exception {
        JSONObject jsonObject = sendRequest(bizContent);
        return jsonObject.toJSONString();
    }

    private JSONObject sendRequest(String bizContent) throws Exception {
        JSONObject parse = JSONObject.parseObject(bizContent);
        String transCode = parse.getString("transCode");
        String url = parse.getString("httpUrl");
        String key = parse.getString("appKey");
        String secrect = parse.getString("appSecrect");
        String timestamp = parse.getString("timestamp");
        String nonce = parse.getString("nonce");
        if (StringUtils.isNotEmpty(url)) {
            httpUrl = url;
        }
        if (StringUtils.isNotEmpty(key)) {
            appKey = key;
        }
        if (StringUtils.isNotEmpty(secrect)) {
            appSecrect = secrect;
        }
        if (StringUtils.isEmpty(timestamp)) {
            timestamp = System.currentTimeMillis() + "";
        }
        if (StringUtils.isEmpty(nonce)) {
            nonce = timestamp;
        }
        String sign = generateSign(timestamp, nonce, transCode);
        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair("appKey", appKey));
        params.add(new BasicNameValuePair("sign", sign));
        params.add(new BasicNameValuePair("timestamp", timestamp));
        params.add(new BasicNameValuePair("nonce", nonce));
        params.add(new BasicNameValuePair("transCode", transCode));
        params.add(new BasicNameValuePair("bizContent", bizContent));
        HttpEntity httpEntity = new UrlEncodedFormEntity(params, "UTF-8");
        JSONObject httpPost = httpPost(StringUtils.isEmpty(url) ? httpUrl : url, httpEntity);
        return httpPost;
    }

    private String generateSign(String timestamp, String nonce, String transCode) {
        // String nonce = timestamp;
        // 源字符串拼接,按照请求参数名的字母升序排列非空请求参数(appkey->nonce->timestamp->transCode)
        String originalSignStr =
            "appkey=" + appKey + "&nonce=" + nonce + "&timestamp=" + timestamp + "&transCode=" + transCode;
        // 拼接appSecrect
        originalSignStr = originalSignStr + "&appSecrect=" + appSecrect;

        logger.info("reqjson======{}", originalSignStr);
        // 进行MD5加密并转为大写
        String md5 = Md5Utils.hash(originalSignStr);
        logger.info("md5===={}", md5.toUpperCase());

        return md5.toUpperCase();
    }

    public JSONObject httpPost(String url, HttpEntity entity) {
        HttpURLConnection conn = null;
        String errString = null;
        JSONObject jObject = null;
        try {
            URL connUrl = new URL(url);
            conn = (HttpURLConnection)connUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(connTimeOut);
            conn.setReadTimeout(readTimeOut);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
            entity.writeTo(conn.getOutputStream());

            try {
                if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                    jObject = JSONObject.parseObject(readString(conn.getInputStream()));
                } else {
                    errString = readString(conn.getErrorStream());
                    jObject = JSONObject.parseObject(errString);
                }
            } catch (JSONException e) {
                if (errString == null) {
                    jObject = JSONObject.parseObject(e.toString());
                }
            }

            return jObject;
        } catch (MalformedURLException e) {
            logger.error("http post error[{}]", e.toString(), e);
            jObject = JSON.parseObject(e.toString());
        } catch (IOException e) {
            logger.error("http post error[{}]", e.toString(), e);
            jObject = JSON.parseObject(e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jObject;
    }

    private String readString(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        byte[] toBuf = null;
        try {
            len = in.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = in.read(buf);
            }
            toBuf = out.toByteArray();

            return new String(toBuf, "UTF-8");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return null;
    }
}
