package cn.eyecool.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * URL(编码、解码、参数解析)
 * 
 * @author mawj
 * @date 2021/09/27
 */
public class UrlUtil {
    private final static String ENCODE = "GBK";

    /**
     * URL 解码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:09:51
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * URL 转码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:10:28
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 正则替换
     * 
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String replaceUrlParameterReg(String url, String name, String value) {
        if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(value)) {
            url = url.replaceAll("(" + name + "=[^&]*)", name + "=" + value);
        }
        return url;
    }

    /**
     * 正则替换
     * 
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String getUrlParameterReg(String url, String name) {

        Map<String, String> mapRequest = new HashMap<>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(url);
        if (strUrlParam == null) {
            return "";
        }
        // 每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            // 解析出键值
            if (arrSplitEqual.length > 1) {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (!StringUtils.EMPTY.equals(arrSplitEqual[0])) {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], StringUtils.EMPTY);
                }
            }
        }
        return mapRequest.get(name);
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     * 
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }
}