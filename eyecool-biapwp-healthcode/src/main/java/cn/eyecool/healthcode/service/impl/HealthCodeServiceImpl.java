package cn.eyecool.healthcode.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weixin4j.model.message.template.TemplateData;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonCert;
import cn.eyecool.basedata.mapper.BasePersonCertMapper;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.http.HttpClientUtil;
import cn.eyecool.healthcode.constant.HealthCodeConstants;
import cn.eyecool.healthcode.param.HealthCodeRequest;
import cn.eyecool.healthcode.service.IHealthCodeService;
import cn.eyecool.healthcode.service.ISDHealthCodeService;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDictTypeService;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 健康码查询实现类
 *
 * @author zfx
 * @since 2021/1/29 16:46
 **/
@Service
public class HealthCodeServiceImpl implements IHealthCodeService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCodeServiceImpl.class);

    @Autowired
    private ISysDictTypeService sysDictTypeService;
    @Autowired
    private BasePersonCertMapper basePersonCertMapper;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IPersonHealthCodeLogService personHealthCodeLogService;
    @Autowired
    private ISDHealthCodeService sdHealthCodeService;
    @Autowired
    private IMsgSendService msgSendService;

    @Value("${check.device:false}")
    private boolean checkDevice;
    /**
     * 电子健康卡/码分配的授权，该部分需要替换 XClientId
     */
    @Value("${appId.jinan.eyecool:e599eab7dbbe4a1bb80f8c53fa6940aa}")
    private String appIdJinan;
    /**
     * 分配的appSecret 电子健康卡/码分配的密钥，该部分需要替换 ClientSecret
     */
    @Value("${appSecret.jinan.eyecool:0f7d3bc73bb342d393d5b8cb5acd7f91}")
    private String appSecretJinan;

    @SuppressWarnings("unchecked")
    @Override
    public AjaxResult healthCodeSearch(String jsonContent) {
        HealthCodeRequest healthCodeRequest = JSONObject.parseObject(jsonContent, HealthCodeRequest.class);
        long start = System.currentTimeMillis();
        String region = healthCodeRequest.getRegion();
        String reqUrl = getHealthUrl(region);
        logger.info("The URL address of the health code query request is [{}]", reqUrl);
        if (StringUtils.isEmpty(reqUrl)) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_REGION_ERROR);
        }
        PersonHealthCodeLog personHealthCodeLog = initHealthCodeLog(healthCodeRequest);
        AjaxResult result = null;
        try {
            /** 存储证件信息 */
            operatePersonCert(healthCodeRequest);
            /** 济南公司申请的济南健康码 */
            if (HealthCodeConstants.REGION_SHANDONG.equals(region) || HealthCodeConstants.REGION_JINAN.equals(region)) {
                result = handleJiNanHealthcode(healthCodeRequest, personHealthCodeLog, reqUrl);
            } else {
                result = handleOtherHealthcode(personHealthCodeLog, reqUrl, jsonContent);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            personHealthCodeLog.setMessage(e.getMessage());
            result = HttpAjaxResult.businessError(HealthCodeConstants.HTTP_HEALTH_REQUEST_ERROR);
        } finally {
            personHealthCodeLog.setTimeUsed(System.currentTimeMillis() - start);
            personHealthCodeLogService.insertPersonHealthCodeLog(personHealthCodeLog);
        }
        Map<String, String> resultData = (Map<String, String>)result.get(AjaxResult.DATA_TAG);
        if (null == resultData) {
            resultData = new HashMap<String, String>();
        }
        resultData.put("healthcodeLogId", personHealthCodeLog.getId());
        result.put(AjaxResult.DATA_TAG, resultData);
        return result;
    }

    /**
     * 济南公司申请的健康码
     * 
     * @param healthCodeRequest
     * @param personHealthCodeLog
     * @param reqUrl
     * @return
     */
    private AjaxResult handleJiNanHealthcode(HealthCodeRequest healthCodeRequest,
        PersonHealthCodeLog personHealthCodeLog, String reqUrl) {
        healthCodeRequest.setxClientId(appIdJinan);
        healthCodeRequest.setxCientSecert(appSecretJinan);
        AjaxResult result = sdHealthCodeService.querySddzjkm(healthCodeRequest, reqUrl);
        String msg = (String)result.get(HealthCodeConstants.RESULT_MSG);
        personHealthCodeLog.setMessage(msg);
        if (Constants.STATUS_ZERO.equals(result.get(HealthCodeConstants.RESULT_CODE))) {
            personHealthCodeLog.setResult(DictConstants.BioResult.PASS);
            return result;
        }
        String wechatMsg = StringUtils.EMPTY;
        if (msg.contains(HealthCodeConstants.CODE_YELLOW)) {
            wechatMsg = HealthCodeConstants.CODE_YELLOW;
        } else if (msg.contains(HealthCodeConstants.CODE_RED)) {
            wechatMsg = HealthCodeConstants.CODE_RED;
        }
        if (StringUtils.isNotEmpty(wechatMsg)) {
            final String resWechatMsg = wechatMsg;
            String tenantId = TenantContextHolder.getTenantId();
            CompletableFuture.runAsync(() -> {
                TenantContextHolder.setTenantId(tenantId);
                // 发送微信
                sendWeixinMessage(healthCodeRequest, resWechatMsg);
            });
        }
        personHealthCodeLog.setResult(DictConstants.BioResult.NOTPASS);
        return result;
    }

    /**
     * 申请的其他健康码
     * 
     * @param personHealthCodeLog
     * @param reqUrl
     * @param jsonContent
     * @return
     */
    private AjaxResult handleOtherHealthcode(PersonHealthCodeLog personHealthCodeLog, String reqUrl,
        String jsonContent) {
        String response = HttpClientUtil.doPostJson(reqUrl, jsonContent);
        if (logger.isInfoEnabled()) {
            logger.info("Health code request[{}]result[{}]", reqUrl, response);
        }
        JSONObject res = JSONObject.parseObject(response);
        String code;
        String msg;
        JSONObject data = res.getJSONObject(HealthCodeConstants.RESULT_DATA);
        if (data == null) {
            msg = res.getString(HealthCodeConstants.RESULT_MSG);
            code = res.getString(HealthCodeConstants.RESULT_CODE);
        } else {
            msg = data.getString(HealthCodeConstants.RESULT_MSG);
            code = data.getString(HealthCodeConstants.RESULT_CODE);
        }
        personHealthCodeLog.setMessage(msg);
        HashMap<String, String> resData = Maps.newHashMap();
        personHealthCodeLog.setResult(DictConstants.BioResult.NOTPASS);
        if (code.equals(Constants.STATUS_ONE)) {
            resData.put("state", Constants.STATUS_ZERO);
            resData.put("msg", msg);
            personHealthCodeLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_GREEN, resData);
        } else if (code.equals(Constants.STATUS_FOUR)) {
            resData.put("state", Constants.STATUS_ONE);
            resData.put("msg", msg);
            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_YELLOW, resData);
        } else if (code.equals(Constants.STATUS_FIVE)) {
            // 4 黄码 5 红码
            resData.put("state", Constants.STATUS_TWO);
            resData.put("msg", msg);
            return HttpAjaxResult.httpSuccess(HealthCodeConstants.CODE_RED, resData);
        } else {
            if (!checkDevice) {
                resData.put("state", Constants.STATUS_ZERO);
                resData.put("msg", HealthCodeConstants.HTTP_NOCHEACK_PASS);
                return HttpAjaxResult
                    .httpSuccess(HealthCodeConstants.CODE_GREEN + "-" + HealthCodeConstants.HTTP_RESULT_PASS, resData);
            }
            return HttpAjaxResult.businessError(msg);
        }
    }

    private String getHealthUrl(String region) {
        List<SysDictData> sysDictDatas = sysDictTypeService.selectDictDataByType(HealthCodeConstants.DICT_HEALTH_URL);
        if (CollectionUtils.isEmpty(sysDictDatas)) {
            return StringUtils.EMPTY;
        }
        for (int i = 0; i < sysDictDatas.size(); i++) {
            String dictLabel = sysDictDatas.get(i).getDictLabel();
            String dictValue = sysDictDatas.get(i).getDictValue();
            if (dictLabel.equals(region)) {
                logger.info("The URL area code of the health code query request is: [{}], and the address is [{}]", region, dictValue);
                return dictValue;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * initHealthCodeLog 初始化健康码请求日志
     *
     * @param healthCodeRequest 请求对象
     * @return cn.eyecool.biapwp.healthcode.domain.PersonHealthCodeLog
     * @author zfx
     * @since 2021/1/30 11:07
     */
    private PersonHealthCodeLog initHealthCodeLog(HealthCodeRequest healthCodeRequest) {
        PersonHealthCodeLog personHealthCodeLog = new PersonHealthCodeLog();
        String id = String.valueOf(IdWorker.getNextLongId());
        personHealthCodeLog.setId(id);
        personHealthCodeLog.setUniqueId(healthCodeRequest.getIdCarNo());
        personHealthCodeLog.setReceivedSeq(healthCodeRequest.getRequestSeq());
        personHealthCodeLog
            .setReceivedTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, healthCodeRequest.getRequestTime()));
        personHealthCodeLog.setDeviceCode(healthCodeRequest.getDeviceCode());
        personHealthCodeLog.setTemperature(healthCodeRequest.getTemperature());
        personHealthCodeLog.setCarNo(healthCodeRequest.getCarNo());
        personHealthCodeLog.setRegion(healthCodeRequest.getRegion());
        personHealthCodeLog.setResult(DictConstants.BioResult.NOTPASS);
        return personHealthCodeLog;
    }

    /**
     * operatePersonCert 处理证件信息 有则不处理 无则添加
     *
     * @param healthCodeRequest 请求对象请求区域不合法，请按照字典
     * @return void
     * @author zfx
     * @since 2021/1/30 11:22
     */
    private void operatePersonCert(HealthCodeRequest healthCodeRequest) throws CustomException, IOException {
        String idCarNo = healthCodeRequest.getIdCarNo();
        /** 处理证件信息 包含证件照 */
        BasePersonCert basePersonCert = new BasePersonCert();
        basePersonCert.setUniqueId(idCarNo);
        List<BasePersonCert> certList = basePersonCertMapper.selectBasePersonCertList(basePersonCert);
        if (CollectionUtils.isNotEmpty(certList)) {
            return;
        }
        String photo = healthCodeRequest.getPhoto();
        if (StringUtils.isNotEmpty(photo)) {
            String baseDir = sysConfigService.selectConfigByKey(SysConfigConstants.BASEDATA_CERT_DIR_KEY);
            if (StringUtils.isBlank(baseDir)) {
                MessageUtils.message("health.code.service.folder.need", SysConfigConstants.BASEDATA_CERT_DIR_KEY);
            }
            String filePathName =
                PlatformFileUploadUtils.uploadWithFileName(Boolean.TRUE, idCarNo + ".jpg", photo, baseDir);
            basePersonCert.setCertImg(filePathName);
        }
        basePersonCert.setCertType(DictConstants.CertType.ID_CARD);
        basePersonCert.setCertNum(idCarNo);
        basePersonCert.setCertName(healthCodeRequest.getName());
        basePersonCert.setGender(getGenderCode(healthCodeRequest.getGender()));
        basePersonCert.setNation(getDictNationVal(healthCodeRequest.getNation()));
        basePersonCert.setAddress(healthCodeRequest.getRegAddress());
        basePersonCert.setBthDate(healthCodeRequest.getBirthday());
        basePersonCert.setEncrypted(DictConstants.Encrypted.ENABLE);
        basePersonCert.setId(IdWorker.getNextStringId());
        basePersonCert.setCreateTime(new Date());
        basePersonCert.setUpdateTime(new Date());
        basePersonCert.setCertAuthority(healthCodeRequest.getCertAuthority());
        basePersonCert.setCertValidity(healthCodeRequest.getCertValidity());
        basePersonCertMapper.insertBasePersonCert(basePersonCert);
    }

    private String getDictNationVal(String nation) {
        String res = "";
        List<SysDictData> sysDictDatas = sysDictTypeService.selectDictDataByType(HealthCodeConstants.DICT_CERT_TYPE);
        if (StringUtils.isEmpty(nation) || CollectionUtils.isEmpty(sysDictDatas)) {
            return res;
        }
        for (int i = 0; i < sysDictDatas.size(); i++) {
            if (sysDictDatas.get(i).getDictValue().equals(nation)) {
                res = sysDictDatas.get(i).getDictLabel();
                break;
            }
        }
        return res;
    }

    private String getGenderCode(String name) {
        if (StringUtils.isEmpty(name)) {
            return "2";
        }
        switch (name) {
            case "男":
                return "0";
            case "女":
                return "1";
            default:
                return "2";
        }
    }

    /**
     * 推送健康码红码人员信息到微信公众号
     *
     * @param request
     */
    private void sendWeixinMessage(HealthCodeRequest request, String healthCode) {
        String configKey = SysConfigConstants.ABNORMAL_HEALTHCODE_PUSH_WEIXIN_PARAMS;
        String weixinParamJson = sysConfigService.selectConfigByKey(configKey);
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isBlank(weixinParamJson)) {
            logger.warn("tenant [{}] is not configured with abnormal health code push WeChat public account parameters[" + configKey + "]", tenantId);
            return;
        }
        logger.info("Tenant [{}] abnormal health code push WeChat public account parameters: [{}]", tenantId, weixinParamJson);
        JSONObject parseObject = null;
        String appId = null;
        String templateId = null;
        String phoneStr = null;
        try {
            parseObject = JSONObject.parseObject(weixinParamJson);
            appId = (String)parseObject.get("appId");
            templateId = (String)parseObject.get("templateId");
            phoneStr = (String)parseObject.get("phone");
            if (StringUtils.isBlank(appId) || StringUtils.isBlank(templateId) || StringUtils.isBlank(phoneStr)) {
                logger.warn("Tenant[{}] platform system parameters [" + configKey + "] wrong format, appId:[{}], templateId[{}], phone:[{}]", tenantId,
                    appId, templateId, phoneStr);
                return;
            }
        } catch (Exception e) {
            logger.error("Tenant [{}] platform system parameter [" + configKey + "] malformed", e);
            return;
        }
        MsgWeixinSendInfo sendInfo = new MsgWeixinSendInfo();
        sendInfo.setReceivedSeq(IdWorker.getNextStringId());
        sendInfo.setAppId(appId);
        sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
        sendInfo.setMsgSubject(MessageUtils.message("health.code.service.exception.tip"));
        sendInfo.setTemplateId(templateId);
        sendInfo.setPhoneStr(phoneStr);
        List<TemplateData> list = Lists.newArrayList();
        // 姓名
        list.add(new TemplateData("keyword1", request.getName()));
        // 健康码（黄码/红码）
        list.add(new TemplateData("keyword2", healthCode));
        // 刷卡地点
        list.add(new TemplateData("keyword3", request.getSite()));
        // 时间
        list.add(new TemplateData("keyword4", request.getRequestTime()));
        // 备注
        list.add(new TemplateData("remark", MessageUtils.message("health.code.service.msg.remark")));
        sendInfo.setTemplateData(list);
        msgSendService.sendWeixinMessage(sendInfo);
    }

}
