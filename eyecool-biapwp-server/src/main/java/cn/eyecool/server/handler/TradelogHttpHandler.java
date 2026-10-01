package cn.eyecool.server.handler;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.noninductive.disruptor.event.RecognizeHitEvent.RecognizeHitResultMessage;
import cn.eyecool.noninductive.disruptor.event.RecognizeStrangerEvent.RecognizeStrangerMessage;
import cn.eyecool.noninductive.disruptor.queue.RecognizeHitDisruptorQueue;
import cn.eyecool.noninductive.disruptor.queue.RecognizeStrangerDisruptorQueue;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDictTypeService;
import cn.eyecool.tradelog.domain.PersonFaceMatchLog;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.PersonFaceirisSearchLog;
import cn.eyecool.tradelog.param.MulitLogQueryRequest;
import cn.eyecool.tradelog.param.PersonFaceIrisMultiBakLog;
import cn.eyecool.tradelog.param.PersonFaceMatchBakLog;
import cn.eyecool.tradelog.param.PersonFaceSearchBakLog;
import cn.eyecool.tradelog.service.IPersonFaceMatchLogService;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;
import cn.eyecool.tradelog.service.IPersonFaceirisSearchLogService;
import cn.eyecool.tradelog.vo.PersonFaceMatchLogVO;
import cn.eyecool.tradelog.vo.PersonFaceSearchLogVO;
import cn.eyecool.tradelog.vo.PersonMulitSearchVO;

/**
 * 日志HTTP请求处理器
 * 
 * @author mawj
 * @date 2021/05/10
 */
@Component
public class TradelogHttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TradelogHttpHandler.class);

    @Autowired
    private IPersonFaceSearchLogService faceSearchLogService;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;
    @Autowired
    private IPersonFaceirisSearchLogService faceirisSearchLogService;
    @Autowired
    private IPersonFaceMatchLogService personFaceMatchLogService;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private IBasePersonInfoService basePersonInfoService;

    /**
     * 查询设备信息
     * 
     * @param deviceNo
     * @return
     */
    private DeviceInfo getClientByDeviceNo(String deviceNo) {
        // 清空在此之前设备之的TenantContextHolder上下文信息，不携带租户隔离查询设备信息
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
        // 重新设置租户信息
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(tenantId);
        }
        if (CollectionUtils.isEmpty(list)) {
            LOG.error("Device [{}] does not exist", deviceNo);
            return null;
        }
        return list.get(0);
    }

    /**
     * 校验设备所属租户和接口授权租户是否一致
     *
     * @param deviceInfo
     * @return
     */
    private boolean validateDeviceTenant(DeviceInfo deviceInfo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        String deviceNo = deviceInfo.getDeviceNo();
        LOG.info("Device [{}] belongs to tenant [{}]", deviceNo, deviceInfo.getTenantId());
        if (StringUtils.isBlank(deviceInfo.getTenantId())) {
            LOG.error("Device [{}] does not maintain tenant information", deviceNo);
            return false;
        }
        // 验证appKey所属租户和设备所属租户是否一致
        String tenantId = TenantContextHolder.getTenantId();
        if (!deviceInfo.getTenantId().equals(tenantId)) {
            LOG.error("The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]", deviceNo, deviceInfo.getTenantId(), tenantId);
            return false;
        }
        return true;
    }

    /**
     * 保存人脸识别回传日志
     *
     * @param bizContent
     * @return
     */
    public AjaxResult savePersonFaceSearchBakLog(String bizContent) {
        // json转换
        PersonFaceSearchBakLog faceSearchBakLog = null;
        try {
            faceSearchBakLog = JSONObject.parseObject(bizContent, PersonFaceSearchBakLog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String deviceCode = faceSearchBakLog.getDeviceCode();
        // 如果有设备编码，那么是设备日志回传，根据设备编码查询并记录场景编码和子场景信息，防止设备传输有误
        if (StringUtils.isNotBlank(deviceCode)) {
            DeviceInfo deviceInfo = getClientByDeviceNo(deviceCode);
            if (null != deviceInfo) {
                // 验证设备租户和授权appKey所属租户是否一致
                if (!validateDeviceTenant(deviceInfo)) {
                    String msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                    return HttpAjaxResult.businessError(msg);
                }
                faceSearchBakLog.setChannelCode(deviceInfo.getChannelCode());
                faceSearchBakLog.setDeviceModel(deviceInfo.getDeviceModelCode());
                if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                    faceSearchBakLog.setDeviceName(deviceInfo.getDeviceName());
                }
                if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                    faceSearchBakLog.setDeviceIp(deviceInfo.getDeviceIp());
                }
                if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                    faceSearchBakLog.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
                }
                if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                    faceSearchBakLog.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
                }
                faceSearchBakLog.setDeviceAddr(deviceInfo.getDeviceAddr());
                // 设备方向设置
                faceSearchBakLog.setDeviceDirection(deviceInfo.getDeviceDirection());
                faceSearchBakLog.setSubtreasuryCode(deviceInfo.getPrimarySubCode());
                if (StringUtils.isNotBlank(deviceInfo.getPrimarySubCode())) {
                    ChannelSubtreasuryInfo subCondition = new ChannelSubtreasuryInfo();
                    subCondition.setSubTreasuryCode(deviceInfo.getPrimarySubCode());
                    List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                        channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(subCondition);
                    ChannelSubtreasuryInfo subtreasuryInfo =
                        CollectionUtils.isEmpty(subtreasuryInfoList) ? null : subtreasuryInfoList.get(0);
                    faceSearchBakLog
                        .setSubtreasuryName(null == subtreasuryInfo ? null : subtreasuryInfo.getSubTreasuryName());
                }
                // 无感设备弹窗
                if (DictConstants.DeviceType.NONINDUCTIVE_DEVICE.equals(deviceInfo.getDeviceType())) {
                    PersonFaceSearchBakLog finalFaceSearchBakLog = faceSearchBakLog;
                    String tenantId = TenantContextHolder.getTenantId();
                    CompletableFuture.runAsync(() -> {
                        TenantContextHolder.setTenantId(tenantId);
                        handlePopWindow(finalFaceSearchBakLog, deviceInfo);
                    });
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Device [{}] is of type [{}], no popup function is required", deviceCode, deviceInfo.getDeviceType());
                }
            }
        }
        String acceptBakLog = sysConfigService.selectConfigByKey(SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(acceptBakLog)) {
            LOG.warn("The scene return log is directly discarded. To receive it, please modify the system parameter {}=Y", SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
            return HttpAjaxResult.httpSuccess();
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = faceSearchBakLog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = faceSearchBakLog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 卡号
        String cardNo = faceSearchBakLog.getCardNo();
        if (StringUtils.isNotBlank(cardNo) && cardNo.length() > 48) {
            msg = MessageUtils.message("base.person.handler.cardno.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 现场照校验（非刷卡的情况下现场照不能为空）
        String sceneImage = faceSearchBakLog.getSceneImage();
        if (StringUtils.isBlank(cardNo) && StringUtils.isBlank(sceneImage)) {
            msg = MessageUtils.message("bio.trade.handler.sceneimage.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求时间不能为空
        String receivedTimeStr = faceSearchBakLog.getReceivedTime();
        if (StringUtils.isBlank(receivedTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.received.time.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, receivedTimeStr);
        } catch (Exception e) {
            LOG.error("Request time [receivedTime] malformed: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.received.time.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求耗时
        String timeUsedStr = faceSearchBakLog.getTimeUsed();
        if (StringUtils.isNotBlank(timeUsedStr)) {
            try {
                Long.valueOf(timeUsedStr);
            } catch (Exception e) {
                LOG.error("Request timed [timeUsed] malformed: {}", e.getMessage());
                msg = MessageUtils.message("trade.log.handler.timeused.format.error");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 检活分数不为空时候，必须是数值
        String checkliveScore = faceSearchBakLog.getCheckliveScore();
        if (StringUtils.isNotBlank(checkliveScore) && !DoubleValidator.getInstance().isValid(checkliveScore)) {
            msg = MessageUtils.message("trade.log.handler.checklive.result.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活结果不为空时，必须是0或者1
        String checkliveResult = faceSearchBakLog.getCheckliveResult();
        if (StringUtils.isNotBlank(checkliveResult) && !DictConstants.Status.ENABLE.equals(checkliveResult)
            && !DictConstants.Status.DISABLE.equals(checkliveResult)) {
            msg = MessageUtils.message("trade.log.handler.checklive.result.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 结果不为空时，必须是0或者1
        String result = faceSearchBakLog.getResult();
        if (StringUtils.isNotBlank(result) && !DictConstants.Status.ENABLE.equals(result)
            && !DictConstants.Status.DISABLE.equals(result)) {
            msg = MessageUtils.message("trade.log.handler.result.notempty.must.been");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 得分为空时，必须是数值
        String sceneStockScore = faceSearchBakLog.getSceneStockScore();
        if (StringUtils.isNotBlank(sceneStockScore) && !DoubleValidator.getInstance().isValid(sceneStockScore)) {
            msg = MessageUtils.message("trade.log.handler.scene.stock.score.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备经度不为空时，必须是数字
        String deviceLongitude = faceSearchBakLog.getDeviceLongitude();
        if (StringUtils.isNotBlank(deviceLongitude)) {
            try {
                Double val = Double.valueOf(deviceLongitude);
                if (val > 180 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 设备维度不为空时，必须是数字
        String deviceDimension = faceSearchBakLog.getDeviceDimension();
        if (StringUtils.isNotBlank(deviceDimension)) {
            try {
                Double val = Double.valueOf(deviceDimension);
                if (val > 90 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 温度不为空时，必须是数字
        String temperature = faceSearchBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            LOG.error("temperature temperature=[{}] type error, must be numeric", temperature);
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureFloor = faceSearchBakLog.getTemperatureFloor();
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            LOG.error("Temperature threshold lower limit temperatureFloor=[{}] type error, must be numeric", temperatureFloor);
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureTop = faceSearchBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            LOG.error("The upper limit of the temperature threshold temperatureTop=[{}] is of the wrong type and must be a numeric value", temperatureTop);
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否生物识别
        String bioRecognized = faceSearchBakLog.getBioRecognized();
        if (StringUtils.isNotBlank(bioRecognized) && !DictConstants.YesOrNoState.YES.equals(bioRecognized)
            && !DictConstants.YesOrNoState.NO.equals(bioRecognized)) {
            LOG.error("Whether biometric identification [bioRecognized=[{}] is illegal, it must be [Y|N]", bioRecognized);
            msg = MessageUtils.message("trade.log.handler.bio.recognized.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            faceSearchLogService.savePersonFaceSearchBakLog(faceSearchBakLog);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Failed to return face log", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to return face log", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 动态弹窗
     * 
     * @param finalFaceSearchBakLog 识别日志
     * @param deviceInfo 设备信息
     */
    private void handlePopWindow(PersonFaceSearchBakLog finalFaceSearchBakLog, DeviceInfo deviceInfo) {
        if (StringUtils.isBlank(finalFaceSearchBakLog.getUniqueId())) {
            RecognizeStrangerMessage strangerMsg = new RecognizeStrangerMessage(finalFaceSearchBakLog.getReceivedTime(),
                deviceInfo.getDeviceNo(), finalFaceSearchBakLog.getSceneImage(), null);
            RecognizeStrangerDisruptorQueue.publishEvent(strangerMsg);
            return;
        }
        BasePersonInfo basePersonInfo =
            basePersonInfoService.selectBasePersonInfoById(finalFaceSearchBakLog.getUniqueId());
        if (basePersonInfo == null) {
            LOG.error("uniqueId [{}] The corresponding person is empty", finalFaceSearchBakLog.getUniqueId());
            return;
        }
        if (StringUtils.isBlank(finalFaceSearchBakLog.getSceneStockScore())) {
            finalFaceSearchBakLog.setSceneStockScore("0");
            if (LOG.isDebugEnabled()) {
                LOG.debug("uniqueId [{}],receiveSeq [{}] score is null,set 0", finalFaceSearchBakLog.getUniqueId(),
                    finalFaceSearchBakLog.getReceivedSeq());
            }
        }
        RecognizeHitResultMessage resultMsg = new RecognizeHitResultMessage(finalFaceSearchBakLog.getUniqueId(),
            basePersonInfo.getName(), finalFaceSearchBakLog.getReceivedTime(), deviceInfo.getDeviceNo(),
            finalFaceSearchBakLog.getSceneImage(), Double.parseDouble(finalFaceSearchBakLog.getSceneStockScore()),
            "hit", null);
        RecognizeHitDisruptorQueue.publishEvent(resultMsg);
    }

    /**
     * 保存人脸虹膜多模态识别回传日志
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult savePersonFaceIrisMultiBakLog(String bizContent) {
        // json转换
        PersonFaceIrisMultiBakLog multiBakLog = null;
        try {
            multiBakLog = JSONObject.parseObject(bizContent, PersonFaceIrisMultiBakLog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String deviceSn = multiBakLog.getDeviceSn();
        // 如果有设备编码，那么是设备日志回传，根据设备编码查询并记录渠道编码和分库信息，防止设备传输有误
        DeviceInfo deviceInfo = null;
        if (StringUtils.isNotBlank(deviceSn)) {
            deviceInfo = getClientByDeviceNo(deviceSn);
            if (null != deviceInfo) {
                // 验证设备租户和授权appKey所属租户是否一致
                if (!validateDeviceTenant(deviceInfo)) {
                    String msg = MessageUtils.message("base.person.handler.device.tenant.error",deviceSn);
                    return HttpAjaxResult.businessError(msg);
                }
                multiBakLog.setChannelCode(deviceInfo.getChannelCode());
                multiBakLog.setDeviceModel(deviceInfo.getDeviceModelCode());
                if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                    multiBakLog.setDeviceName(deviceInfo.getDeviceName());
                }
                if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                    multiBakLog.setDeviceIp(deviceInfo.getDeviceIp());
                }
                if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                    multiBakLog.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
                }
                if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                    multiBakLog.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
                }
                multiBakLog.setDeviceType(deviceInfo.getDeviceType());
                multiBakLog.setSubTreasuryCode(deviceInfo.getPrimarySubCode());
                multiBakLog.setDeviceAddr(deviceInfo.getDeviceAddr());
                multiBakLog.setDeviceDirection(deviceInfo.getDeviceDirection());
            }
        }
        String acceptBakLog = sysConfigService.selectConfigByKey(SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(acceptBakLog)) {
            LOG.warn("The scene return log is directly discarded. To receive it, please modify the system parameters.{}=Y", SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
            return HttpAjaxResult.httpSuccess();
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = multiBakLog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 场景编码不能为空
        String channelCode = multiBakLog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 卡号
        String cardNo = multiBakLog.getCardNo();
        if (StringUtils.isNotBlank(cardNo) && cardNo.length() > 48) {
            msg = MessageUtils.message("trade.log.handler.cardno.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 比对模式不能为空
        String matchMode = multiBakLog.getMatchMode();
        List<SysDictData> dictDataList = dictTypeService.selectDictDataByType(DictConstants.MULTI_MATCH_MODE_DICT_TYPE);
        List<String> dictValues = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(dictDataList)) {
            dictValues = dictDataList.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        }
        if (StringUtils.isBlank(matchMode) || !dictValues.contains(matchMode)) {
            LOG.error("The matchMode=[{}] parameter is invalid!", matchMode);
            msg = MessageUtils.message("trade.log.handler.match.mode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求时间不能为空
        String receivedTimeStr = multiBakLog.getReceivedTime();
        if (StringUtils.isBlank(receivedTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.received.time.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, receivedTimeStr);
        } catch (Exception e) {
            LOG.error("Request time [receivedTime] malformed: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.received.time.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活分数不为空时候，必须是数值
        String checkliveScore = multiBakLog.getCheckliveScore();
        if (StringUtils.isNotBlank(checkliveScore) && !DoubleValidator.getInstance().isValid(checkliveScore)) {
            msg = MessageUtils.message("trade.log.handler.checklive.score.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活结果不为空时，必须是0或者1
        String checkliveResult = multiBakLog.getCheckliveResult();
        if (StringUtils.isNotBlank(checkliveResult) && !DictConstants.Status.ENABLE.equals(checkliveResult)
            && !DictConstants.Status.DISABLE.equals(checkliveResult)) {
            msg = MessageUtils.message("trade.log.handler.checklive.result.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 得分不为空时，必须是数值
        String matchScore = multiBakLog.getMatchScore();
        if (StringUtils.isNotBlank(matchScore) && !DoubleValidator.getInstance().isValid(matchScore)) {
            LOG.error("matchScore=[{}] type error, must be numeric", matchScore);
            msg = MessageUtils.message("trade.log.handler.match.score.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人脸比对分数
        String sceneFirAlgoScore = multiBakLog.getSceneFirAlgoScore();
        if (StringUtils.isNotBlank(sceneFirAlgoScore) && !DoubleValidator.getInstance().isValid(sceneFirAlgoScore)) {
            LOG.error("The face alignment score sceneFirAlgoScore=[{}] is of the wrong type, it must be a numeric value", sceneFirAlgoScore);
            msg = MessageUtils.message("trade.log.handler.face.match.score.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 虹膜比对分数
        String sceneSecAlgoScore = multiBakLog.getSceneSecAlgoScore();
        if (StringUtils.isNotBlank(sceneSecAlgoScore) && !DoubleValidator.getInstance().isValid(sceneSecAlgoScore)) {
            LOG.error("The iris alignment score sceneSecAlgoScore=[{}] is of the wrong type, it must be a number", sceneSecAlgoScore);
            msg = MessageUtils.message("trade.log.handler.iris.match.score.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求耗时
        String timeUsed = multiBakLog.getTimeUsed();
        if (StringUtils.isNotBlank(timeUsed) && !LongValidator.getInstance().isValid(timeUsed)) {
            LOG.error("The request time used timeUsed=[{}] is malformed, it must be an integer", timeUsed);
            msg = MessageUtils.message("trade.log.handler.timeused.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人脸比对用时ms
        String firTimeUsed = multiBakLog.getFirTimeUsed();
        if (StringUtils.isNotBlank(firTimeUsed) && !LongValidator.getInstance().isValid(firTimeUsed)) {
            LOG.error("The format of firTimeUsed=[{}] is wrong in face comparison, it must be an integer", firTimeUsed);
            msg = MessageUtils.message("trade.log.handler.fir.timeused.format");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 虹膜比对用时ms
        String secTimeUsed = multiBakLog.getSecTimeUsed();
        if (StringUtils.isNotBlank(secTimeUsed) && !LongValidator.getInstance().isValid(secTimeUsed)) {
            LOG.error("The iris comparison time secTimeUsed=[{}] is in the wrong format, it must be an integer", secTimeUsed);
            msg = MessageUtils.message("trade.log.handler.timeused.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 结果不为空时，必须是0或者1
        String result = multiBakLog.getResult();
        if (StringUtils.isNotBlank(result) && !DictConstants.BioResult.PASS.equals(result)
            && !DictConstants.BioResult.NOTPASS.equals(result)) {
            msg = MessageUtils.message("trade.log.handler.match.result.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备经度不为空时，必须是数字
        String deviceLongitude = multiBakLog.getDeviceLongitude();
        if (StringUtils.isNotBlank(deviceLongitude)) {
            try {
                Double val = Double.valueOf(deviceLongitude);
                if (val > 180 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.longitude.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 设备维度不为空时，必须是数字
        String deviceDimension = multiBakLog.getDeviceDimension();
        if (StringUtils.isNotBlank(deviceDimension)) {
            try {
                Double val = Double.valueOf(deviceDimension);
                if (val > 90 || val < 0) {
                    msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            } catch (Exception e) {
                msg = MessageUtils.message("bio.trade.handler.device.latitue.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 温度不为空时，必须是数字
        String temperature = multiBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            LOG.error("temperature temperature=[{}] type error, must be numeric", temperature);
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureFloor = multiBakLog.getTemperatureFloor();
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            LOG.error("Temperature threshold lower limit temperatureFloor=[{}] type error, must be numeric", temperatureFloor);
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureTop = multiBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            LOG.error("The upper limit of the temperature threshold temperatureTop=[{}] is of the wrong type and must be a numeric value", temperatureTop);
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            faceirisSearchLogService.savePersonFaceIrisMultiBakLog(multiBakLog);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Failed to post back the multimodal recognition log", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to post back the multimodal recognition log", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸1v1比对日志回传
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult savePersonFaceMatchBakLog(String bizContent) {
        // json转换
        PersonFaceMatchBakLog faceMatchBakLog = null;
        try {
            faceMatchBakLog = JSONObject.parseObject(bizContent, PersonFaceMatchBakLog.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String deviceCode = faceMatchBakLog.getDeviceCode();
        // 如果有设备编码，那么是设备日志回传，根据设备编码查询并记录场景编码和子场景信息，防止设备传输有误
        if (StringUtils.isNotBlank(deviceCode)) {
            DeviceInfo deviceInfo = getClientByDeviceNo(deviceCode);
            if (null != deviceInfo) {
                // 验证设备租户和授权appKey所属租户是否一致
                if (!validateDeviceTenant(deviceInfo)) {
                    String msg = MessageUtils.message("base.person.handler.device.tenant.error",deviceCode);
                    return HttpAjaxResult.businessError(msg);
                }
                faceMatchBakLog.setChannelCode(deviceInfo.getChannelCode());
                faceMatchBakLog.setDeviceModel(deviceInfo.getDeviceModelCode());
                if (StringUtils.isNotBlank(deviceInfo.getDeviceName())) {
                    faceMatchBakLog.setDeviceName(deviceInfo.getDeviceName());
                }
                if (StringUtils.isNotBlank(deviceInfo.getDeviceIp())) {
                    faceMatchBakLog.setDeviceIp(deviceInfo.getDeviceIp());
                }
                if (StringUtils.isNotNull(deviceInfo.getLongitude())) {
                    faceMatchBakLog.setDeviceLongitude(String.valueOf(deviceInfo.getLongitude()));
                }
                if (StringUtils.isNotNull(deviceInfo.getLatitude())) {
                    faceMatchBakLog.setDeviceDimension(String.valueOf(deviceInfo.getLatitude()));
                }
                faceMatchBakLog.setDeviceAddr(deviceInfo.getDeviceAddr());
                faceMatchBakLog.setDeviceDirection(deviceInfo.getDeviceDirection());
            }
        }
        String acceptBakLog = sysConfigService.selectConfigByKey(SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(acceptBakLog)) {
            LOG.warn("The scene return log is directly discarded. To receive it, please modify the system parameter {}=Y", SysConfigConstants.PLATFORM_ACCEPT_BAK_LOG_OPEN_KEY);
            return HttpAjaxResult.httpSuccess();
        }
        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = faceMatchBakLog.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String channelCode = faceMatchBakLog.getChannelCode();
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 比对得分
        String sceneOnlineScore = faceMatchBakLog.getSceneOnlineScore();
        if (StringUtils.isNotBlank(sceneOnlineScore) && !DoubleValidator.getInstance().isValid(sceneOnlineScore)) {
            LOG.error("The sceneOnlineScore=[{}] is not legal to compare the score between the on-site photo and the online verification photo, it must be a numerical value!", sceneOnlineScore);
            msg = MessageUtils.message("trade.log.handler.scene.online.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneChipScore = faceMatchBakLog.getSceneChipScore();
        if (StringUtils.isNotBlank(sceneChipScore) && !DoubleValidator.getInstance().isValid(sceneChipScore)) {
            LOG.error("现场照与芯片照比对分值sceneChipScore=[{}]不合法，必须是数值!", sceneChipScore);
            msg = "现场照与芯片照比对分值[sceneChipScore]必须是数值，请检查!";
            msg = MessageUtils.message("");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneStockScore = faceMatchBakLog.getSceneStockScore();
        if (StringUtils.isNotBlank(sceneStockScore) && !DoubleValidator.getInstance().isValid(sceneStockScore)) {
            msg = "现场照与底库照比对分值[sceneStockScore]必须是数值，请检查!";
            msg = MessageUtils.message("");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String onlineChipScore = faceMatchBakLog.getOnlineChipScore();
        if (StringUtils.isNotBlank(onlineChipScore) && !DoubleValidator.getInstance().isValid(onlineChipScore)) {
            LOG.error("OnlineChipScore=[{}] is not legal, it must be a numerical value!", onlineChipScore);
            msg = MessageUtils.message("trade.log.handler.online.chip.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 比对结果
        String sceneOnlineResult = faceMatchBakLog.getSceneOnlineResult();
        if (StringUtils.isNotBlank(sceneOnlineResult) && !DictConstants.BioResult.PASS.equals(sceneOnlineResult)
            && !DictConstants.BioResult.NOTPASS.equals(sceneOnlineResult)) {
            LOG.error("The comparison result of the on-site photo and the online verification photo sceneOnlineResult=[{}] is illegal!", sceneOnlineResult);
            msg = MessageUtils.message("trade.log.handler.scene.online.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneChipResult = faceMatchBakLog.getSceneChipResult();
        if (StringUtils.isNotBlank(sceneChipResult) && !DictConstants.BioResult.PASS.equals(sceneChipResult)
            && !DictConstants.BioResult.NOTPASS.equals(sceneChipResult)) {
            LOG.error("The comparison result between the scene photo and the chip photo sceneChipResult=[{}] is invalid!", sceneChipResult);
            msg = MessageUtils.message("trade.log.handler.scene.chip.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneStockResult = faceMatchBakLog.getSceneStockResult();
        if (StringUtils.isNotBlank(sceneStockResult) && !DictConstants.BioResult.PASS.equals(sceneStockResult)
            && !DictConstants.BioResult.NOTPASS.equals(sceneStockResult)) {
            LOG.error("The comparison result of the scene photo and the library photo sceneStockResult=[{}] is invalid, it must be [0|1]!", sceneStockResult);
            msg = MessageUtils.message("trade.log.handler.scene.stock.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String onlineChipResult = faceMatchBakLog.getOnlineChipResult();
        if (StringUtils.isNotBlank(onlineChipResult) && !DictConstants.BioResult.PASS.equals(onlineChipResult)
            && !DictConstants.BioResult.NOTPASS.equals(onlineChipResult)) {
            LOG.error("The comparison result between the online verification photo and the chip photo onlineChipResult=[{}] is illegal!", onlineChipResult);
            msg = MessageUtils.message("trade.log.handler.online.chip.match.result.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活分数不为空时候，必须是数值
        String checkliveScore = faceMatchBakLog.getCheckliveScore();
        if (StringUtils.isNotBlank(checkliveScore) && !DoubleValidator.getInstance().isValid(checkliveScore)) {
            msg = MessageUtils.message("trade.log.handler.checklive.result.number");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 检活结果
        String checkliveResult = faceMatchBakLog.getCheckliveResult();
        if (StringUtils.isNotBlank(checkliveResult) && !DictConstants.BioResult.PASS.equals(checkliveResult)
            && !DictConstants.BioResult.NOTPASS.equals(checkliveResult)) {
            LOG.error("On-site photo check result checkliveResult=[{}] is illegal!", checkliveResult);
            msg = MessageUtils.message("trade.log.handler.checklive.result.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 最终比对结果
        String result = faceMatchBakLog.getResult();
        if (StringUtils.isNotBlank(result) && !DictConstants.BioResult.PASS.equals(result)
            && !DictConstants.BioResult.NOTPASS.equals(result)) {
            LOG.error("The comparison result result=[{}] is invalid!", result);
            msg = MessageUtils.message("trade.log.handler.match.result.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求时间不能为空
        String receivedTime = faceMatchBakLog.getReceivedTime();
        if (StringUtils.isBlank(receivedTime)) {
            msg = MessageUtils.message("trade.log.handler.received.time.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, receivedTime);
        } catch (Exception e) {
            LOG.error("Request time [receivedTime] malformed: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.received.time.format.error", DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 请求耗时
        String timeUsed = faceMatchBakLog.getTimeUsed();
        if (StringUtils.isNotBlank(timeUsed) && !LongValidator.getInstance().isValid(timeUsed)) {
            LOG.error("The request time used timeUsed=[{}] is malformed, it must be an integer", timeUsed);
            msg = MessageUtils.message("trade.log.handler.timeused.format.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String sceneImageBase64 = faceMatchBakLog.getSceneImageBase64();
        if (StringUtils.isBlank(sceneImageBase64)) {
            msg = MessageUtils.message("trade.log.handler.scene.image.b64.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 测温数据
        String temperature = faceMatchBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperature) && !DoubleValidator.getInstance().isValid(temperature)) {
            LOG.error("temperature temperature=[{}] type error, must be numeric", temperature);
            msg = MessageUtils.message("bio.trade.handler.temperature.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureFloor = faceMatchBakLog.getTemperatureFloor();
        if (StringUtils.isNotBlank(temperatureFloor) && !DoubleValidator.getInstance().isValid(temperatureFloor)) {
            LOG.error("Temperature threshold lower limit temperatureFloor=[{}] type error, must be numeric", temperatureFloor);
            msg = MessageUtils.message("bio.trade.handler.temperature.min.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String temperatureTop = faceMatchBakLog.getTemperature();
        if (StringUtils.isNotBlank(temperatureTop) && !DoubleValidator.getInstance().isValid(temperatureTop)) {
            LOG.error("The upper limit of the temperature threshold temperatureTop=[{}] is of the wrong type and must be a numeric value", temperatureTop);
            msg = MessageUtils.message("bio.trade.handler.temperature.max.error");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            personFaceMatchLogService.savePersonFaceMatchBakLog(faceMatchBakLog);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            LOG.error("Failed to return face 1v1 log", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to return face 1v1 log", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸识别日志查询
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFaceSearchLogQuery(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码不能为空
        String channelCode = (String)parseObject.get("channelCode");
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 分页参数不能为空
        String pageNumStr = (String)parseObject.get("pageNum");
        if (StringUtils.isBlank(pageNumStr)) {
            msg = MessageUtils.message("trade.log.handler.page.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (!IntegerValidator.getInstance().isValid(pageNumStr)) {
            msg = MessageUtils.message("trade.log.handler.current.page.format.error");
            LOG.error("Pagination page number currPage=[{}] is not an integer", pageNumStr);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 交易起始时间格式验证
        String startTimeStr = (String)parseObject.get("startTime");
        if (StringUtils.isBlank(startTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.starttime.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Date startTime = null;
        try {
            startTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTimeStr);
        } catch (Exception e) {
            LOG.error("The start time [startTime] is malformed: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.starttime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 交易结束时间格式验证
        String endTimeStr = (String)parseObject.get("endTime");
        if (StringUtils.isBlank(endTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.endtime.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Date endTime = null;
        try {
            endTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTimeStr);
        } catch (Exception e) {
            LOG.error("Malformed end time [endTime]: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.endtime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (endTime.getTime() - startTime.getTime() > 5L * 24 * 60 * 60 * 1000) {
            msg = MessageUtils.message("trade.log.handler.query.scope.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员唯一标识不为空时不能超过48位置
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isNotBlank(uniqueId) && uniqueId.length() > 48) {
            msg = MessageUtils.message("trade.log.handler.uniqueid.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备编码
        String deviceCode = (String)parseObject.get("deviceCode");
        // 设备型号编码
        String deviceModel = (String)parseObject.get("deviceModel");
        try {
            // 查询操作
            int pageNum = Integer.valueOf(pageNumStr);// 分页
            int pageSize = 200;// 分页数量， 一次更新200条
            String orderBy = SqlUtil.escapeOrderBySql("received_time desc, id asc");
            PageHelper.startPage(pageNum, pageSize, orderBy);
            PersonFaceSearchLog personFaceSearchLog = new PersonFaceSearchLog();
            Map<String, Object> params = new HashMap<>();
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            personFaceSearchLog.setParams(params);
            personFaceSearchLog.setUniqueId(uniqueId);
            personFaceSearchLog.setDeviceCode(deviceCode);
            personFaceSearchLog.setDeviceModel(deviceModel);
            List<PersonFaceSearchLog> logList =
                faceSearchLogService.selectPersonFaceSearchLogByTimeRange(personFaceSearchLog);
            PageInfo<PersonFaceSearchLog> pageInfo = new PageInfo<PersonFaceSearchLog>(logList);
            long total = pageInfo.getTotal();// 数据总量
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("total", total);
            if (total == 0L) {
                resultMap.put("list", Collections.emptyList());
                resultMap.put("hasMore", false);
                resultMap.put("nextPage", pageInfo.getNextPage());
                return HttpAjaxResult.httpSuccess(resultMap);
            }
            List<PersonFaceSearchLogVO> logVOList = logList.stream().map(it -> {
                PersonFaceSearchLogVO logVO = new PersonFaceSearchLogVO();
                BeanUtils.copyBeanProp(logVO, it);
                logVO.setLogId(it.getId());
                String sceneImage = it.getSceneImage();
                if (StringUtils.isNotEmpty(sceneImage)) {
                    logVO.setSceneImageId(AESUtils.encryptAES(sceneImage));
                }
                // TODO 照片的ID展示不做处理，返回null，后期优化拓展使用
                return logVO;
            }).collect(Collectors.toList());
            resultMap.put("list", logVOList);
            resultMap.put("hasMore", pageInfo.getPages() > pageNum);
            resultMap.put("nextPage", pageInfo.getNextPage());
            return HttpAjaxResult.httpSuccess(resultMap);
        } catch (CustomException e) {
            LOG.error("Querying Face Recognition Log Abnormal", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Querying Face Recognition Log Abnormal", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸虹膜多模态识别日志查询
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFaceIrisLogQuery(String bizContent) {
        MulitLogQueryRequest request;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("personFaceIrisLogQuery request[{}]", bizContent);
            }
            request = JSONObject.parseObject(bizContent, MulitLogQueryRequest.class);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = validateMulitLogQueryRequest(request);
        if (StringUtils.isNotEmpty(msg)) {
            return HttpAjaxResult.businessDataValidError(msg);
        }
        String pageNumStr = request.getPageNum();
        Date startTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, request.getStartTime());
        Date endTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, request.getEndTime());
        try {
            List<SysDictData> dictDataList =
                dictTypeService.selectDictDataByType(DictConstants.MULTI_MATCH_MODE_DICT_TYPE);
            Map<String, String> matchModes = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(dictDataList)) {
                matchModes = dictDataList.stream()
                    .collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getDictLabel));
            }
            final Map<String, String> matchModeDict = matchModes;
            String uniqueId = request.getUniqueId();
            // 查询操作 分页数量， 一次更新200条
            int pageSize = 200;
            int pageNum = Integer.valueOf(pageNumStr);
            String orderBy = SqlUtil.escapeOrderBySql("received_time desc, id asc");
            PageHelper.startPage(pageNum, pageSize, orderBy);
            PersonFaceirisSearchLog personFaceirisSearchLog = new PersonFaceirisSearchLog();
            Map<String, Object> params = new HashMap<>();
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            personFaceirisSearchLog.setParams(params);
            personFaceirisSearchLog.setUniqueId(uniqueId);
            personFaceirisSearchLog.setResult(request.getResult());
            personFaceirisSearchLog.setDeviceSn(request.getDeviceSn());
            personFaceirisSearchLog.setDeviceModel(request.getDeviceModel());
            List<PersonFaceirisSearchLog> logList =
                faceirisSearchLogService.selectPersonFaceIrisSearchLogByTimeRange(personFaceirisSearchLog);
            PageInfo<PersonFaceirisSearchLog> pageInfo = new PageInfo<PersonFaceirisSearchLog>(logList);
            long total = pageInfo.getTotal();// 数据总量
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("total", total);
            if (total == 0L) {
                resultMap.put("list", Collections.emptyList());
                resultMap.put("hasMore", false);
                resultMap.put("nextPage", pageInfo.getNextPage());
                return HttpAjaxResult.httpSuccess(resultMap);
            }
            List<PersonMulitSearchVO> logVOList = logList.stream().map(it -> {
                PersonMulitSearchVO logVO = new PersonMulitSearchVO();
                BeanUtils.copyBeanProp(logVO, it);
                logVO.setLogId(it.getId());
                logVO.setDeviceCode(it.getDeviceSn());
                logVO.setMatchMode(matchModeDict.get(it.getMatchMode()));
                // TODO 照片的ID展示不做处理，返回null，后期优化拓展使用
                String sceneImage = it.getSceneFaceImage();
                if (StringUtils.isNotEmpty(sceneImage)) {
                    logVO.setSceneFirImageId(AESUtils.encryptAES(sceneImage));
                }
                return logVO;
            }).collect(Collectors.toList());
            resultMap.put("list", logVOList);
            resultMap.put("hasMore", pageInfo.getPages() > pageNum);
            resultMap.put("nextPage", pageInfo.getNextPage());
            return HttpAjaxResult.httpSuccess(resultMap);
        } catch (CustomException e) {
            LOG.error("Querying the abnormality of the multimodal recognition log of the face and iris", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Querying the abnormality of the multimodal recognition log of the face and iris", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 校验人脸虹膜多模态日志查询参数
     * 
     * @param request
     * @return
     */
    private String validateMulitLogQueryRequest(MulitLogQueryRequest request) {
        // 分页参数不能为空
        String pageNumStr = request.getPageNum();
        if (StringUtils.isBlank(pageNumStr)) {
            return MessageUtils.message("trade.log.handler.page.empty");
        }
        if (!IntegerValidator.getInstance().isValid(pageNumStr)) {
            return MessageUtils.message("trade.log.handler.current.page.format.error");
        }
        // 交易起始时间格式验证
        String startTimeStr = request.getStartTime();
        if (StringUtils.isBlank(startTimeStr)) {
            return MessageUtils.message("trade.log.handler.starttime.empty");
        }
        Date startTime = null;
        try {
            startTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTimeStr);
        } catch (Exception e) {
            LOG.error("The start time [startTime] is malformed: {}", e.getMessage());
            return MessageUtils.message("trade.log.handler.starttime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
        }
        // 交易结束时间格式验证
        String endTimeStr = request.getEndTime();
        if (StringUtils.isBlank(endTimeStr)) {
            return MessageUtils.message("trade.log.handler.endtime.empty");
        }
        Date endTime = null;
        try {
            endTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTimeStr);
        } catch (Exception e) {
            LOG.error("Malformed end time [endTime]: {}", e.getMessage());
            return MessageUtils.message("trade.log.handler.endtime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
        }
        if (endTime.getTime() - startTime.getTime() > 5L * 24 * 60 * 60 * 1000) {
            return MessageUtils.message("trade.log.handler.query.scope.limit");
        }
        // 人员唯一标识不为空时不能超过48位置
        String uniqueId = request.getUniqueId();
        if (StringUtils.isNotBlank(uniqueId) && uniqueId.length() > 48) {
            return MessageUtils.message("trade.log.handler.uniqueid.length.limit");
        }
        return StringUtils.EMPTY;
    }

    /**
     * 人脸1v1比对日志查询
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult personFaceMatchLogQuery(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 场景编码不能为空
        String channelCode = (String)parseObject.get("channelCode");
        if (StringUtils.isBlank(channelCode)) {
            msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 分页参数不能为空
        String pageNumStr = (String)parseObject.get("pageNum");
        if (StringUtils.isBlank(pageNumStr)) {
            msg = MessageUtils.message("trade.log.handler.page.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (!IntegerValidator.getInstance().isValid(pageNumStr)) {
            msg = MessageUtils.message("trade.log.handler.current.page.format.error");
            LOG.error("Pagination page number currPage=[{}] is not an integer", pageNumStr);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 交易起始时间格式验证
        String startTimeStr = (String)parseObject.get("startTime");
        if (StringUtils.isBlank(startTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.starttime.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Date startTime = null;
        try {
            startTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTimeStr);
        } catch (Exception e) {
            LOG.error("The start time [startTime] is malformed: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.starttime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 交易结束时间格式验证
        String endTimeStr = (String)parseObject.get("endTime");
        if (StringUtils.isBlank(endTimeStr)) {
            msg = MessageUtils.message("trade.log.handler.endtime.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        Date endTime = null;
        try {
            endTime = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTimeStr);
        } catch (Exception e) {
            LOG.error("Malformed end time [endTime]: {}", e.getMessage());
            msg = MessageUtils.message("trade.log.handler.endtime.format.error",DateUtils.YYYY_MM_DD_HH_MM_SS);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (endTime.getTime() - startTime.getTime() > 5L * 24 * 60 * 60 * 1000) {
            msg = MessageUtils.message("trade.log.handler.query.scope.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员唯一标识不为空时不能超过48位置
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isNotBlank(uniqueId) && uniqueId.length() > 48) {
            msg = MessageUtils.message("trade.log.handler.uniqueid.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备编码
        String deviceCode = (String)parseObject.get("deviceCode");
        // 设备型号编码
        String deviceModel = (String)parseObject.get("deviceModel");
        try {
            // 查询操作
            int pageNum = Integer.valueOf(pageNumStr);// 分页
            int pageSize = 200;// 分页数量， 一次更新200条
            String orderBy = SqlUtil.escapeOrderBySql("received_time desc, id asc");
            PageHelper.startPage(pageNum, pageSize, orderBy);
            PersonFaceMatchLog personFaceMatchLog = new PersonFaceMatchLog();
            Map<String, Object> params = new HashMap<>();
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            personFaceMatchLog.setParams(params);
            personFaceMatchLog.setUniqueId(uniqueId);
            personFaceMatchLog.setDeviceCode(deviceCode);
            personFaceMatchLog.setDeviceModel(deviceModel);
            List<PersonFaceMatchLog> logList =
                personFaceMatchLogService.selectPersonFaceMatchLogByTimeRange(personFaceMatchLog);
            PageInfo<PersonFaceMatchLog> pageInfo = new PageInfo<PersonFaceMatchLog>(logList);
            long total = pageInfo.getTotal();// 数据总量
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("total", total);
            if (total == 0L) {
                resultMap.put("list", Collections.emptyList());
                resultMap.put("hasMore", false);
                resultMap.put("nextPage", pageInfo.getNextPage());
                return HttpAjaxResult.httpSuccess(resultMap);
            }
            List<PersonFaceMatchLogVO> logVOList = logList.stream().map(it -> {
                PersonFaceMatchLogVO logVO = new PersonFaceMatchLogVO();
                BeanUtils.copyBeanProp(logVO, it);
                logVO.setLogId(it.getId());
                String sceneImage = it.getSceneImage();
                if (StringUtils.isNotEmpty(sceneImage)) {
                    logVO.setSceneImageId(AESUtils.encryptAES(sceneImage));
                }
                // TODO 其他照片的ID展示不做处理，返回null，后期优化拓展使用
                return logVO;
            }).collect(Collectors.toList());
            resultMap.put("list", logVOList);
            resultMap.put("hasMore", pageInfo.getPages() > pageNum);
            resultMap.put("nextPage", pageInfo.getNextPage());
            return HttpAjaxResult.httpSuccess(resultMap);
        } catch (CustomException e) {
            LOG.error("Querying the face 1v1 comparison log is abnormal", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            LOG.error("Querying the face 1v1 comparison log is abnormal", e);
            return HttpAjaxResult.httpError();
        }
    }
}
