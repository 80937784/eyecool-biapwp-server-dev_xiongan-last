package cn.eyecool.msg.configure.weixin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weixin4j.Configuration;
import org.weixin4j.Weixin;
import org.weixin4j.WeixinException;
import org.weixin4j.component.AbstractComponent;
import org.weixin4j.http.HttpsClient;
import org.weixin4j.http.Response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.msg.configure.weixin.model.ArticleAnalysisData;
import cn.eyecool.msg.configure.weixin.model.InterfaceAnalysisData;
import cn.eyecool.msg.configure.weixin.model.UpstreamMsgAnalysisData;
import cn.eyecool.msg.configure.weixin.model.UserAnalysisData;

public class AnalysisComponent extends AbstractComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisComponent.class);

    public AnalysisComponent(Weixin weixin) {
        super(weixin);
        // TODO Auto-generated constructor stub
    }

    /**
     * 用户增减数据
     *
     * @return List<UserSummaryData>
     * @throws WeixinException
     */
    public List<UserAnalysisData> userAnalysisDataSummary(String beginDate, String endDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);

            end = simpleDateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 7) {
                calendar.add(Calendar.DAY_OF_YEAR, 6);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post(
                "https://api.weixin.qq.com/datacube/getusersummary?access_token=" + weixin.getToken().getAccess_token(),
                params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get返回json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UserAnalysisData> summaryDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> summaryDatas.add(JSON.toJavaObject((JSONObject)n, UserAnalysisData.class)));
                return summaryDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 累计用户数据
     *
     * @return List<UserCumulateData>
     * @throws WeixinException
     */
    public List<UserAnalysisData> userAnalysisDataCumulate(String beginDate, String endDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);
            end = simpleDateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 7) {
                calendar.add(Calendar.DAY_OF_YEAR, 6);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getusercumulate?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UserAnalysisData> cumulateDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> cumulateDatas.add(JSON.toJavaObject((JSONObject)n, UserAnalysisData.class)));
                return cumulateDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 图文群发每日数据
     *
     * @param formatDate
     * @return List<ArticleAnalysisData>
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisSummary(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getarticlesummary?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 图文群发总数据
     *
     * @param formatDate
     * @return
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisTotal(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getarticletotal?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get返,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 图文统计数据
     *
     * @param formatDate
     * @return
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisUserRead(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getuserread?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 图文统计分时数据
     *
     * @param formatDate
     * @return
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisUserReadHour(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getuserreadhour?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 图文分享转发数据
     *
     * @param formatDate
     * @return
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisUserShare(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getusershare?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 图文分享转发分时数据
     *
     * @param formatDate
     * @return
     * @throws WeixinException
     */
    public List<ArticleAnalysisData> articleAnalysisUserShareHour(String formatDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", formatDate);
        params.put("end_date", formatDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getusersharehour?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<ArticleAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, ArticleAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送概况数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysis(String beginDate, String endDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);

            end = simpleDateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 7) {
                calendar.add(Calendar.DAY_OF_YEAR, 6);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post(
                "https://api.weixin.qq.com/datacube/getupstreammsg?access_token=" + weixin.getToken().getAccess_token(),
                params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息分送分时数据
     *
     * @param beginDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgHour(String beginDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        params.put("end_date", beginDate);
        Response res = http.post(
            "https://api.weixin.qq.com/datacube/getupstreammsghour?access_token=" + weixin.getToken().getAccess_token(),
            params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送周数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgWeek(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);

            end = simpleDateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 30) {
                calendar.add(Calendar.DAY_OF_YEAR, 29);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getupstreammsgweek?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return 回json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送月数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgMonth(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);
            end = simpleDateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 30) {
                calendar.add(Calendar.DAY_OF_YEAR, 29);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getupstreammsgmonth?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送分布数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgDist(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);
            end = simpleDateFormat.parse(endDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 15) {
                calendar.add(Calendar.DAY_OF_YEAR, 14);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getupstreammsgdist?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送分布周数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgDistWeek(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);

            end = simpleDateFormat.parse(endDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 30) {
                calendar.add(Calendar.DAY_OF_YEAR, 29);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getupstreammsgdistweek?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 消息发送分布月数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<UpstreamMsgAnalysisData> upstreamMsgAnalysisMsgDistMonth(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginDate);

            end = simpleDateFormat.parse(endDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            int endDay = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTime(begin);
            int beginDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (endDay - beginDay >= 30) {
                calendar.add(Calendar.DAY_OF_YEAR, 29);
                endDate = simpleDateFormat.format(calendar.getTime());
            }
            params.put("end_date", endDate);
            Response res = http.post("https://api.weixin.qq.com/datacube/getupstreammsgdistmonth?access_token="
                + weixin.getToken().getAccess_token(), params);
            JSONObject jsonObj = res.asJSONObject();
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    LOGGER.info("/menu/get,return json：{}", jsonObj);
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null) {
                    // 返回异常信息
                    throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
                }
                // 返回自定义菜单对象
                List<UpstreamMsgAnalysisData> analysisDatas = new ArrayList<>();
                res.asJSONObject().getJSONArray("list")
                    .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, UpstreamMsgAnalysisData.class)));
                return analysisDatas;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    /**
     * 接口分析数据
     *
     * @param beginDate
     * @param endDate
     * @return
     * @throws WeixinException
     */
    public List<InterfaceAnalysisData> interfaceAnalysisSummary(String beginDate, String endDate)
        throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        params.put("end_date", endDate);
        Response res = http.post("https://api.weixin.qq.com/datacube/getinterfacesummary?access_token="
            + weixin.getToken().getAccess_token(), params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<InterfaceAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, InterfaceAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }

    public List<InterfaceAnalysisData> interfaceAnalysisSummaryHour(String beginDate) throws WeixinException {
        HttpsClient http = new HttpsClient();
        JSONObject params = new JSONObject();
        params.put("begin_date", beginDate);
        params.put("end_date", beginDate);
        Response res = http.post("https://api.weixin.qq.com/datacube/getinterfacesummaryhour?access_token="
            + weixin.getToken().getAccess_token(), params);
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                LOGGER.info("/menu/get,return json：{}", jsonObj);
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                // 返回异常信息
                throw new WeixinException(getCause(jsonObj.getIntValue("errcode")));
            }
            // 返回自定义菜单对象
            List<InterfaceAnalysisData> analysisDatas = new ArrayList<>();
            res.asJSONObject().getJSONArray("list")
                .forEach(n -> analysisDatas.add(JSON.toJavaObject((JSONObject)n, InterfaceAnalysisData.class)));
            return analysisDatas;
        }
        // 返回自定义菜单对
        return Collections.emptyList();
    }
}
