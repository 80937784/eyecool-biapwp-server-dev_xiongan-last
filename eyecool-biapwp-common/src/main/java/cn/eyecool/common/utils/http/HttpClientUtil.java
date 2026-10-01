package cn.eyecool.common.utils.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * httpclient 工具类
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2018年11月10日
 */
public class HttpClientUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    // 连接超时时间
    private static int connTimeOut = 60 * 1000;

    /**
     * get请求
     * 
     * @author mawenjun
     * @param url
     * @param param
     * @return
     * @date 2018年11月10日
     *
     */
    public static String doGet(String url, Map<String, String> param, Map<String, String> header) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 返回结果字符串
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null && !param.isEmpty()) {
                for (Entry<String, String> entry : param.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();
            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            // 添加请求头信息
            httpGet.addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpGet.addHeader("accept", "*/*");
            httpGet.addHeader("connection", "Keep-Alive");
            // 添加自定义的请求头
            if (null != header && !header.isEmpty()) {
                header.keySet().forEach(key -> {
                    httpGet.addHeader(key.toString(), header.get(key));
                });
            }
            // 执行请求
            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            log.error("HTTP request exception", e);
        } finally {
            finallyClose(httpClient, response);
        }
        return resultString;
    }

    /**
     * 默认Get请求
     * 
     * @author mawenjun
     * @param url
     * @return
     * @date 2018年11月10日
     *
     */
    public static String doGet(String url) {
        return doGet(url, null, null);
    }

    /**
     * Post请求
     * 
     * @author mawenjun
     * @param url
     * @param param
     * @param header
     * @return
     * @date 2018年11月10日
     *
     */
    public static String doPost(String url, Map<String, String> param, Map<String, String> header) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 添加header信息
            if (header != null && !header.isEmpty()) {
                Set<Entry<String, String>> entrySet = header.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // 创建参数列表
            if (MapUtils.isNotEmpty(param)) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Entry<String, String> entry : param.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("HTTP request exception", e);
        } finally {
            finallyClose(httpClient, response);
        }

        return resultString;
    }

    /**
     * 默认Post请求
     * 
     * @author mawenjun
     * @param url
     * @return
     * @date 2018年11月10日
     *
     */
    public static String doPost(String url) {
        return doPost(url, null, null);
    }

    /**
     * PostFormdData请求
     * 
     * @author mawenjun
     * @param url
     * @param param
     * @param header
     * @return
     * @date 2018年11月10日
     *
     */
    public static String doPostFormData(String url, Map<String, String> param, Map<String, MultipartFile> fileMap,
        Map<String, String> header) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        // 每个post参数之间的分隔。随意设定，只要不会和其他的字符串重复即可。
        String boundary = "--------------4585696313564699";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 添加header信息
            if (header != null && !header.isEmpty()) {
                Set<Entry<String, String>> entrySet = header.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置请求头
            httpPost.setHeader("Content-Type", "multipart/form-data; charset=utf-8; boundary=" + boundary);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 字符编码
            builder.setCharset(Charset.forName("UTF-8"));
            // 模拟浏览器
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // boundary
            builder.setBoundary(boundary);
            // 创建参数列表
            if (MapUtils.isNotEmpty(param)) {
                for (Entry<String, String> entry : param.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue(),
                        ContentType.create("text/plain", Consts.UTF_8));
                }
            }
            if (MapUtils.isNotEmpty(fileMap)) {
                for (Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    builder.addBinaryBody(entry.getKey(), entry.getValue().getInputStream(),
                        ContentType.MULTIPART_FORM_DATA, entry.getValue().getOriginalFilename());
                }
            }
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("HTTP request exception", e);
        } finally {
            finallyClose(httpClient, response);
        }

        return resultString;
    }

    /**
     * PostJson请求
     * 
     * @author mawenjun
     * @param url
     * @param json
     * @return
     * @date 2018年11月10日
     * @date 2020年02月14日 增加超时时间设置 modify by zfx
     */
    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            /** 设置超时时间 */
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connTimeOut)
                .setSocketTimeout(connTimeOut).setConnectTimeout(connTimeOut).build();
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            httpPost.setConfig(requestConfig);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("HTTP request exception, timeOut[{}]", connTimeOut, e);
        } finally {
            finallyClose(httpClient, response);
        }
        return resultString;
    }

    /**
     * 关闭和释放
     * 
     * @author xiadecheng
     * @param httpClient
     * @param response
     * @date 2019年6月4日
     *
     */
    private static void finallyClose(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
