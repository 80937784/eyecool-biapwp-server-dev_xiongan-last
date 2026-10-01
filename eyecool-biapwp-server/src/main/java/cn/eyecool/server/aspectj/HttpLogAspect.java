package cn.eyecool.server.aspectj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.core.domain.http.StandardHttpParam;
import cn.eyecool.common.exception.BaseException;
import cn.eyecool.common.json.JSON;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.ip.IpUtils;
import cn.eyecool.server.annotation.HttpApiLog;
import cn.eyecool.system.service.ISysDictDataService;
import cn.eyecool.tradelog.constant.TradelogConstants;
import cn.eyecool.tradelog.domain.TradeReqRecord;
import cn.eyecool.tradelog.domain.TradeReqRecordDetail;
import cn.eyecool.tradelog.service.ITradeReqRecordService;

/**
 * HTTP接口访问日志切面
 *
 * @author sunhuayu Date on 2019/10/23
 */
@Aspect
@Component
public class HttpLogAspect {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLogAspect.class);

    @Autowired
    private ISysDictDataService dictDataService;
    @Autowired
    private ITradeReqRecordService tradeReqRecordService;

    @Pointcut("@annotation(cn.eyecool.server.annotation.HttpApiLog)")
    public void httpLogPointCut() {}

    @Around(value = "httpLogPointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        TradeReqRecord reqRecord = new TradeReqRecord();
        TradeReqRecordDetail recordDetail = new TradeReqRecordDetail();
        reqRecord.setReceivedTime(new Date());
        try {
            Object proceed = joinPoint.proceed();
            AjaxResult ajaxResult = (AjaxResult)proceed;
            recordDetail.setResponseMsg(JSON.marshal(proceed));
            if (null != ajaxResult) {
                reqRecord.setStatusCode(ajaxResult.get(AjaxResult.CODE_TAG).toString());
            } else {
                reqRecord.setStatusCode(HttpAjaxResult.HTTP_SUCC_CODE);
            }
            return proceed;
        } catch (Throwable throwable) {
            LOG.error("HTTP interface exception,", throwable);
            recordDetail.setResponseMsg(getExceptionInfo(throwable));
            reqRecord.setStatusCode(HttpAjaxResult.HTTP_ERR_CODE);
            throw new BaseException(getExceptionInfo(throwable));
        } finally {
            String transUrl = ServletUtils.getRequest().getRequestURI();
            String ipAddr = IpUtils.getIpAddr(ServletUtils.getRequest());
            reqRecord.setTenantId(TenantContextHolder.getTenantId());
            String tenantId = TenantContextHolder.getTenantId();
            CompletableFuture.runAsync(() -> {
                TenantContextHolder.setTenantId(tenantId);
                saveLog(joinPoint, reqRecord, recordDetail, transUrl, ipAddr);
            });
            TenantContextHolder.clear();
        }
    }

    /**
     * 保存日志
     * 
     * @param joinPoint
     * @param reqRecord
     * @param transUrl
     * @param clientIp
     */
    private void saveLog(ProceedingJoinPoint joinPoint, TradeReqRecord reqRecord, TradeReqRecordDetail recordDetail,
        String transUrl, String clientIp) {
        // 输出
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        HttpApiLog httpApiLog = signature.getMethod().getAnnotation(HttpApiLog.class);
        Object[] args = joinPoint.getArgs();
        StandardHttpParam httpParam = null;
        String transCode = null;
        for (Object arg : args) {
            if (arg instanceof StandardHttpParam) {
                httpParam = (StandardHttpParam)arg;
                transCode = httpParam.getTransCode();// 交易码
                reqRecord.setTransCode(transCode);
                String label = dictDataService.selectDictLabel(DictConstants.HTTP_INTREFACE_DICT_TYPE, transCode);// 交易标题
                reqRecord.setTransTitle(label);
                break;
            }
        }
        Date sendTime = new Date();
        reqRecord.setSendTime(new Date());
        reqRecord.setTimeUsed(sendTime.getTime() - reqRecord.getReceivedTime().getTime());
        String recordId = IdWorker.getNextStringId();
        reqRecord.setId(recordId);
        reqRecord.setClientIp(clientIp);
        reqRecord.setTransUrl(transUrl);
        reqRecord.setClassMethod(signature.getDeclaringTypeName() + "." + signature.getName() + "()");
        try {
            // 如果是场景交易，需要从bizContent中获取场景编码
            if (null != httpParam) {
                String bizContent = httpParam.getBizContent();
                JSONObject bizContentObj = com.alibaba.fastjson.JSON.parseObject(bizContent);
                String channelCode = (String)bizContentObj.get("channelCode");
                if (StringUtils.isNotBlank(channelCode)) {
                    reqRecord.setChannelCode(channelCode);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        String[] excludeTransForSaveReq = httpApiLog.excludeTransForSaveReq();
        boolean isSaveReqData = httpApiLog.isSaveRequestData()
            && (excludeTransForSaveReq == null || !Arrays.asList(excludeTransForSaveReq).contains(transCode));
        if (!isSaveReqData) {
            httpParam.setBizContent(MessageUtils.message("http.log.aspect.tip"));
        }
        recordDetail.setRecordId(recordId);
        try {
            String params = JSON.marshal(httpParam);
            recordDetail.setRequestMsg(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] excludeTransForSaveResp = httpApiLog.excludeTransForSaveResp();
        boolean isSaveResData = httpApiLog.isSaveResponseData()
            && (excludeTransForSaveResp == null || !Arrays.asList(excludeTransForSaveResp).contains(transCode));
        if (!isSaveResData) {
            recordDetail.setResponseMsg("{\"friendly reminder\":\"not saved!!!!!\"}");
        }
        String detailFilePath = saveRecordDetailToFile(recordDetail, TradelogConstants.TRADE_REQ_RECORD_FILE_BASEDIR
            + DateUtils.parseDateToStr("yyyyMMdd", reqRecord.getReceivedTime()) + "/");
        reqRecord.setDetailFilePath(detailFilePath);
        tradeReqRecordService.insertTradeReqRecord(reqRecord);
    }

    @AfterThrowing(value = "httpLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {}

    /**
     * 对于接口请求报文详情进行文件记录和落盘处理
     * 
     * @param recordDetail
     * @param baseDir
     * @return
     */
    private String saveRecordDetailToFile(TradeReqRecordDetail recordDetail, String baseDir) {
        BufferedWriter bw = null;
        try {
            File dirFile = new File(baseDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            String filePath = baseDir + recordDetail.getRecordId() + ".txt";
            File detailFile = new File(filePath);
            bw = new BufferedWriter(new FileWriter(detailFile));
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("requestMsg", recordDetail.getRequestMsg());
            jsonObj.put("responseMsg", recordDetail.getResponseMsg());
            bw.write(jsonObj.toJSONString());

            return filePath;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } finally {
            if (null != bw) {
                try {
                    bw.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
    }

    /**
     * getExceptionInfo
     *
     * @param e
     * @return result限制长度65530，MySQL text字段长65535，防止存不进去
     */
    public static String getExceptionInfo(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String result = sw.toString();
            if (StringUtils.isNotBlank(result) && result.length() > 65530) {
                result = result.substring(0, 65530);
            }
            return result;
        } finally {
            if (null != sw) {
                try {
                    sw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (null != pw) {
                pw.close();
            }
        }
    }

}
