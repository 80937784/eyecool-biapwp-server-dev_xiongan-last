package cn.eyecool.tradelog.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weixin4j.model.message.template.TemplateData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.dingtalk.api.response.OapiAttendanceRecordUploadResponse;
import com.google.common.collect.Maps;
import com.taobao.api.ApiException;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisFaceMapper;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.config.DigitalSystemConfig;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.http.HttpClientUtil;
import cn.eyecool.common.utils.sign.EyecoolPmSign;
import cn.eyecool.framework.config.websocket.WebSocketServer;
import cn.eyecool.msg.configure.ding.DingTalkInstanceCache;
import cn.eyecool.msg.configure.ding.PlatformDingTalk;
import cn.eyecool.msg.domain.MsgDingApplication;
import cn.eyecool.msg.service.IMsgDingApplicationService;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDeptService;
import cn.eyecool.tradelog.domain.PersonFaceirisSearchLog;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;
import cn.eyecool.tradelog.mapper.PersonFaceirisSearchLogMapper;
import cn.eyecool.tradelog.param.PersonFaceIrisMultiBakLog;
import cn.eyecool.tradelog.service.IPersonFaceirisSearchLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸虹膜搜索日志Service业务层处理
 *
 * @author admin
 * @date 2021-05-08
 */
@Service
@Slf4j
public class PersonFaceirisSearchLogServiceImpl implements IPersonFaceirisSearchLogService {

    @Autowired
    private PersonFaceirisSearchLogMapper personFaceirisSearchLogMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;
    @Autowired
    private IPersonIrisRecogLogicService personIrisRecogLogicService;
    @Autowired
    private BasePersonIrisFaceMapper basePersonIrisFaceMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IMsgDingApplicationService dingApplicationService;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IMsgSendService msgSendService;

    @Autowired
    private ISysDeptService sysDeptService;
    @Value("${eyecool.serverDomain}")
    private String serverDomain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private DigitalSystemConfig digitalSystemConfig;

    /**
     * 查询人脸虹膜搜索日志
     *
     * @param id 人脸虹膜搜索日志ID
     * @return 人脸虹膜搜索日志
     */
    @Override
    public PersonFaceirisSearchLog selectPersonFaceirisSearchLogById(String id) {
        return personFaceirisSearchLogMapper.selectPersonFaceirisSearchLogById(id);
    }

    /**
     * 查询人脸虹膜搜索日志列表
     *
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceirisSearchLog>
        selectPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog) {
        return personFaceirisSearchLogMapper.selectPersonFaceirisSearchLogList(personFaceirisSearchLog);
    }

    /**
     * 查询人脸虹膜搜索日志最新列表
     *
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceirisSearchLog>
        selectLastPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog) {
        return personFaceirisSearchLogMapper.selectLastPersonFaceirisSearchLogList(personFaceirisSearchLog);
    }

    /**
     * 新增人脸虹膜搜索日志
     *
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    @Override
    public int insertPersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog) {
        personFaceirisSearchLog.setCreateTime(DateUtils.getNowDate());
        return personFaceirisSearchLogMapper.insertPersonFaceirisSearchLog(personFaceirisSearchLog);
    }

    /**
     * 修改人脸虹膜搜索日志
     *
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    @Override
    public int updatePersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog) {
        return personFaceirisSearchLogMapper.updatePersonFaceirisSearchLog(personFaceirisSearchLog);
    }

    /**
     * 批量删除人脸虹膜搜索日志
     *
     * @param ids 需要删除的人脸虹膜搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceirisSearchLogByIds(String[] ids) {
        return personFaceirisSearchLogMapper.deletePersonFaceirisSearchLogByIds(ids);
    }

    /**
     * 删除人脸虹膜搜索日志信息
     *
     * @param id 人脸虹膜搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceirisSearchLogById(String id) {
        return personFaceirisSearchLogMapper.deletePersonFaceirisSearchLogById(id);
    }

    /**
     * 保存人脸虹膜多模态回传日志
     *
     * @param multiBakLog
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePersonFaceIrisMultiBakLog(PersonFaceIrisMultiBakLog multiBakLog) {
        PersonFaceirisSearchLog faceirisSearchLog = new PersonFaceirisSearchLog();
        faceirisSearchLog.setId(IdWorker.getNextStringId());
        faceirisSearchLog.setHealthcodeLogId(multiBakLog.getHealthcodeLogId());
        faceirisSearchLog.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_LOG_BAK);
        faceirisSearchLog.setReceivedSeq(multiBakLog.getReceivedSeq());
        faceirisSearchLog.setChannelCode(multiBakLog.getChannelCode());
        faceirisSearchLog.setSubTreasuryCode(multiBakLog.getSubTreasuryCode());
        faceirisSearchLog.setDeviceSn(multiBakLog.getDeviceSn());
        faceirisSearchLog.setDeviceModel(multiBakLog.getDeviceModel());
        faceirisSearchLog.setDeviceName(multiBakLog.getDeviceName());
        faceirisSearchLog.setDeviceIp(multiBakLog.getDeviceIp());
        faceirisSearchLog.setDeviceAddr(multiBakLog.getDeviceAddr());
        faceirisSearchLog.setDeviceLongitude(StringUtils.isBlank(multiBakLog.getDeviceLongitude()) ? null
            : Double.valueOf(multiBakLog.getDeviceLongitude()));
        faceirisSearchLog.setDeviceDimension(StringUtils.isBlank(multiBakLog.getDeviceDimension()) ? null
            : Double.valueOf(multiBakLog.getDeviceDimension()));
        faceirisSearchLog.setDeviceDirection(multiBakLog.getDeviceDirection());
        faceirisSearchLog.setCheckliveScore(StringUtils.isBlank(multiBakLog.getCheckliveScore()) ? null
            : Double.valueOf(multiBakLog.getCheckliveScore()));
        faceirisSearchLog.setCheckliveResult(
            StringUtils.isEmpty(multiBakLog.getCheckliveResult()) ? null : multiBakLog.getCheckliveResult());
        faceirisSearchLog.setUniqueId(multiBakLog.getUniqueId());
        if (StringUtils.isNotBlank(multiBakLog.getSceneFirAlgoScore())) {
            faceirisSearchLog.setSceneFaceScore(Double.valueOf(multiBakLog.getSceneFirAlgoScore()));
        }
        if (StringUtils.isNotBlank(multiBakLog.getSceneSecAlgoScore())) {
            faceirisSearchLog.setSceneIrisScore(Double.valueOf(multiBakLog.getSceneSecAlgoScore()));
        }
        if (StringUtils.isNotBlank(multiBakLog.getMatchScore())) {
            faceirisSearchLog.setMatchScore(Double.valueOf(multiBakLog.getMatchScore()));
        }
        faceirisSearchLog.setMatchMode(multiBakLog.getMatchMode());
        faceirisSearchLog.setResult(multiBakLog.getResult());
        faceirisSearchLog.setCardNo(multiBakLog.getCardNo());
        faceirisSearchLog.setValidType(multiBakLog.getValidType());
        faceirisSearchLog
            .setReceivedTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, multiBakLog.getReceivedTime()));
        faceirisSearchLog.setCreateTime(new Date());
        if (StringUtils.isNotBlank(multiBakLog.getTimeUsed())) {
            faceirisSearchLog.setTimeUsed(Long.valueOf(multiBakLog.getTimeUsed()));
        }
        if (StringUtils.isNotBlank(multiBakLog.getFirTimeUsed())) {
            faceirisSearchLog.setFaceTimeUsed(Long.valueOf(multiBakLog.getFirTimeUsed()));
        }
        if (StringUtils.isNotBlank(multiBakLog.getSecTimeUsed())) {
            faceirisSearchLog.setIrisTimeUsed(Long.valueOf(multiBakLog.getSecTimeUsed()));
        }
        faceirisSearchLog.setTemperature(
            StringUtils.isBlank(multiBakLog.getTemperature()) ? null : Double.valueOf(multiBakLog.getTemperature()));
        faceirisSearchLog.setTemperatureFloor(StringUtils.isBlank(multiBakLog.getTemperatureFloor()) ? null
            : Double.valueOf(multiBakLog.getTemperatureFloor()));
        faceirisSearchLog.setTemperatureTop(StringUtils.isBlank(multiBakLog.getTemperatureTop()) ? null
            : Double.valueOf(multiBakLog.getTemperatureTop()));
        if (StringUtils.isNotBlank(multiBakLog.getTemperatureResult())) {
            faceirisSearchLog.setTemperatureResult(multiBakLog.getTemperatureResult());
        } else if (null != faceirisSearchLog.getTemperature()) {
            Double temperatureVal = faceirisSearchLog.getTemperature();
            Double temperatureTopVal = faceirisSearchLog.getTemperatureTop();
            Double temperatureFloorVal = faceirisSearchLog.getTemperatureFloor();
            if (null != temperatureTopVal && temperatureVal >= temperatureTopVal) {
                faceirisSearchLog.setTemperatureResult(DictConstants.TemperatureResult.HIGHER);
            } else if (null != temperatureFloorVal && temperatureVal < temperatureFloorVal) {
                faceirisSearchLog.setTemperatureResult(DictConstants.TemperatureResult.LOWER);
            } else {
                faceirisSearchLog.setTemperatureResult(DictConstants.TemperatureResult.NORMAL);
            }
        }
        // 加密上传现场照
        String baseDir = getMulitSearchImgBaseDir();
        String sceneFaceImageBase64 = multiBakLog.getSceneFirAlgoImageBase64();
        if (StringUtils.isNotBlank(sceneFaceImageBase64)) {
            faceirisSearchLog.setSceneFaceImage(personFaceRecogLogicService.uploadFaceImg(true, null,
                sceneFaceImageBase64, baseDir + "face" + File.separator));
        }
        String sceneIrisImageBase64 = multiBakLog.getSceneSecAlgoImageBase64();
        if (StringUtils.isNotBlank(sceneIrisImageBase64)) {
            faceirisSearchLog.setSceneIrisImage(personIrisRecogLogicService.uploadIrisImg(true, null,
                sceneIrisImageBase64, baseDir + "iris" + File.separator));
        }
        String uniqueId = multiBakLog.getUniqueId();
        String phone = null;
        if (StringUtils.isNotBlank(uniqueId)) {
            // 查询人员底库照
            BasePersonIrisFace faceIrisCondition = new BasePersonIrisFace();
            faceIrisCondition.setUniqueId(uniqueId);
            faceIrisCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonIrisFace> irisFaceList =
                basePersonIrisFaceMapper.selectBasePersonIrisFaceList(faceIrisCondition);
            if (CollectionUtils.isNotEmpty(irisFaceList)) {
                String faceImageUrl = irisFaceList.get(0).getFaceImageUrl();
                String irisImageUrl = irisFaceList.get(0).getIrisImageUrl();
                String encrypted = irisFaceList.get(0).getEncrypted();
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(encrypted);
                if (StringUtils.isNotBlank(faceImageUrl)) {
                    String stockFaceImageBase64 = PlatformFileUtils.getImageBase64(faceImageUrl);
                    stockFaceImageBase64 = isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockFaceImageBase64)
                        : stockFaceImageBase64;
                    faceirisSearchLog.setStockFaceImage(personFaceRecogLogicService.uploadFaceImg(true, null,
                        stockFaceImageBase64, baseDir + "face" + File.separator));
                }
                if (StringUtils.isNotBlank(irisImageUrl)) {
                    String stockIrisImageBase64 = PlatformFileUtils.getImageBase64(irisImageUrl);
                    stockIrisImageBase64 = isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockIrisImageBase64)
                        : stockIrisImageBase64;
                    faceirisSearchLog.setStockIrisImage(personIrisRecogLogicService.uploadIrisImg(true, null,
                        stockIrisImageBase64, baseDir + "iris" + File.separator));
                }
            }
            // 查询人员信息确定部门
            BasePersonInfo personCondition = new BasePersonInfo();
            personCondition.setUniqueId(uniqueId);
            personCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
            if (CollectionUtils.isNotEmpty(personList)) {
                BasePersonInfo person = personList.get(0);
                faceirisSearchLog.setPersonName(person.getName());
                Long deptId = person.getDeptId();
                if (null != deptId) {
                    faceirisSearchLog.setDeptId(String.valueOf(deptId));
                    SysDept sysDept = sysDeptService.selectDeptById(deptId);
                    faceirisSearchLog.setDeptName(null != sysDept ? sysDept.getDeptName() : null);
                }
                phone = person.getPhone();
            }
        }
        personFaceirisSearchLogMapper.insertPersonFaceirisSearchLog(faceirisSearchLog);
        // 推送实时交易
        execWebsocketSendMsg(faceirisSearchLog);
        // 异步回传识别蓝凌OA日志
        asyncOaAttendanceRecordUpload(faceirisSearchLog, multiBakLog.getDeviceType());
        // 异步回传识别钉钉日志
        asyncDingTalkAttendanceRecordUpload(faceirisSearchLog, phone, multiBakLog.getDeviceType());
        // 推送高温信息到微信公众号
        sendAbnormalTemperatureWeixinMsg(faceirisSearchLog);
    }

    /**
     * 异步上传信息到蓝凌OA
     *
     * @param faceirisSearchLog 识别日志
     * @param deviceType 设备类型
     */
    private void asyncOaAttendanceRecordUpload(PersonFaceirisSearchLog faceirisSearchLog, String deviceType) {
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            if (!DictConstants.DeviceType.ATTENDANCE_DEVICE.equals(deviceType)) {
                if (log.isDebugEnabled()) {
                    log.debug("设备[{}]类型[{}]不是考勤设备类型，不需要进行考勤记录上传，租户ID:[{}]", faceirisSearchLog.getDeviceSn(), deviceType,
                        tenantId);
                }
                return;
            }
            if (!digitalSystemConfig.getEnabled()) {
                log.debug("对接OA系统功能未开启");
                return;
            }
            if (!digitalSystemConfig.getTenantId().equals(tenantId)) {
                log.debug("此租户未开启对接OA功能");
                return;
            }
            if (!DictConstants.BioResult.PASS.equals(faceirisSearchLog.getResult())) {
                if (log.isDebugEnabled()) {
                    log.debug("识别不通过的日志不需要上传,租户Id：[{}],业务流水码:[{}]", tenantId, faceirisSearchLog.getReceivedSeq());
                }
                return;
            }
            executeOaUploadAttendanceRecord(faceirisSearchLog);
        });
    }

    /**
     * 上传数据至蓝凌OA
     *
     * @param faceirisSearchLog
     */
    private void executeOaUploadAttendanceRecord(PersonFaceirisSearchLog faceirisSearchLog) {
        String transCode = "EYECOOLPM_ATTEND";
        String nonce = IdWorker.getNextStringId();
        Map<String, String> param = Maps.newHashMap();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String appKey = digitalSystemConfig.getAppKey();
        String appSecrcet = digitalSystemConfig.getAppSecrect();
        param.put("appKey", appKey);
        param.put("transCode", transCode);
        param.put("timestamp", timestamp);
        param.put("nonce", nonce);
        param.put("sign", EyecoolPmSign.generateSign(appKey, transCode, timestamp, nonce, appSecrcet));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobNum", faceirisSearchLog.getUniqueId());
        jsonObject.put("personName", faceirisSearchLog.getPersonName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonObject.put("attendTime", simpleDateFormat.format(faceirisSearchLog.getReceivedTime()));
        jsonObject.put("location", faceirisSearchLog.getDeviceAddr());
        param.put("bizContent", jsonObject.toJSONString());
        Map<String, String> header = Maps.newHashMap();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        log.info(
            "Invoke digital integrated management system to add business opportunity interface ,url:[{}],param:[{}]",
            digitalSystemConfig.getBaseUrl(), JSON.toJSONString(param));
        String response = HttpClientUtil.doPost(digitalSystemConfig.getBaseUrl(), param, header);
        log.info("Invoke digital integrated management system to add business opportunity interface,response:[{}]",
            response);
        if (StringUtils.isBlank(response)) {
            log.error("the response is null");
            return;
        }
        JSONObject parseObject = JSON.parseObject(response);
        if (!"0".equals(parseObject.getString("code"))) {
            log.error(parseObject.getString("msg"));
            return;
        }
    }

    /**
     * 查询实时交易，推送给前端监控页面
     */
    private void execWebsocketSendMsg(PersonFaceirisSearchLog faceirisSearchLog) {
        String tenantId = StringUtils.isBlank(TenantContextHolder.getTenantId()) ? WebSocketServer.COMMON_CID
            : TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            try {
                RealtimeTradeLog realtimeTradeLog = new RealtimeTradeLog();
                realtimeTradeLog.setReceivedSeq(faceirisSearchLog.getReceivedSeq());
                realtimeTradeLog.setReceivedTime(faceirisSearchLog.getReceivedTime());
                realtimeTradeLog.setTenantId(tenantId);
                realtimeTradeLog.setUniqueId(faceirisSearchLog.getUniqueId());
                realtimeTradeLog.setPersonName(faceirisSearchLog.getPersonName());
                realtimeTradeLog.setResult(faceirisSearchLog.getResult());
                realtimeTradeLog.setDeviceNo(faceirisSearchLog.getDeviceSn());
                realtimeTradeLog.setDeviceName(faceirisSearchLog.getDeviceName());
                realtimeTradeLog.setDeviceAddr(faceirisSearchLog.getDeviceAddr());
                WebSocketServer.sendInfo(JSON.toJSONString(realtimeTradeLog), tenantId);
            } catch (IOException e) {
                log.error("Push face and iris multi-modality to identify real-time transaction exceptions:[{}]",
                    e.getMessage(), e);
            }
        });
    }

    /**
     * 获取多模态识别图片存储基础路径
     *
     * @return
     */
    private String getMulitSearchImgBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_MULIT_SEARCH_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(
                "请先在平台[参数设置]中配置多模态日志图片存储文件夹[" + SysConfigConstants.BUSI_MULIT_SEARCH_DIR_KEY + "]!");
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }

    /**
     * 异步上传考勤打卡记录
     *
     * @param mulitSearchLog
     * @param phone
     * @param deviceType
     */
    private void asyncDingTalkAttendanceRecordUpload(PersonFaceirisSearchLog mulitSearchLog, String phone,
        String deviceType) {
        String tenantId = TenantContextHolder.getTenantId();
        String deviceName = mulitSearchLog.getDeviceName();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            if (!DictConstants.DeviceType.ATTENDANCE_DEVICE.equals(deviceType)) {
                if (log.isDebugEnabled()) {
                    log.debug("设备[{}]类型[{}]不是考勤设备类型，不需要进行考勤记录上传，租户ID:[{}]", mulitSearchLog.getDeviceSn(), deviceType,
                        tenantId);
                }
                return;
            }
            String appKey = configService.selectConfigByKey(SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
            if (StringUtils.isBlank(appKey) || "".equals(appKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("租户[{}]没有进行参数[{}]配置，不需要和钉钉进行数据交互和数据同步", tenantId,
                        SysConfigConstants.DINGTALK_SYNCDATA_APPKEY);
                }
                return;
            }
            if (!DictConstants.BioResult.PASS.equals(mulitSearchLog.getResult())) {
                if (log.isDebugEnabled()) {
                    log.debug("识别不通过的日志不需要上传,租户Id：[{}],业务流水码:[{}]", tenantId, mulitSearchLog.getReceivedSeq());
                }
                return;
            }

            if (StringUtils.isBlank(phone)) {
                log.error("租户[{}]人员[{}]手机号为空，无法进行钉钉考勤信息上传", tenantId, mulitSearchLog.getUniqueId());
                return;
            }
            if (StringUtils.isBlank(deviceName)) {
                log.error("租户[{}]设备[{}]名称为空，无法进行钉钉考勤信息上传", tenantId, mulitSearchLog.getDeviceSn());
                return;
            }
            MsgDingApplication appCondition = new MsgDingApplication();
            appCondition.setAppKey(appKey);
            List<MsgDingApplication> list = dingApplicationService.selectMsgDingApplicationList(appCondition);
            if (CollectionUtils.isEmpty(list)) {
                log.error("无法上传考勤记录,钉钉微应用没有在平台配置,tenantId=[{}],appKey=[{}]");
                return;
            }
            MsgDingApplication msgDingApplication = list.get(0);
            String agentId = msgDingApplication.getAgentId();
            String appSecrect = msgDingApplication.getAppSecrect();
            PlatformDingTalk dingTalk = DingTalkInstanceCache.get(Long.parseUnsignedLong(agentId), appKey, appSecrect);
            String userId = null;
            try {
                userId = dingTalk.getUserIdByMobile(phone);
            } catch (ApiException e) {
                log.error("根据手机号获取userId异常，租户ID:[{}],appKey:[{}],phone：[{}]", tenantId, appKey, phone, e);
                return;
            }
            executeUploadAttendanceRecord(dingTalk, mulitSearchLog, deviceName, userId, 1);
        });
    }

    /**
     * 执行考勤记录上传
     *
     * @param dingTalk 钉钉对象
     * @param mulitSearchLog 日志对象
     * @param deviceName 设备名称
     * @param userId 钉钉人员ID
     * @param tryCount 重试次数
     */
    private void executeUploadAttendanceRecord(PlatformDingTalk dingTalk, PersonFaceirisSearchLog mulitSearchLog,
        String deviceName, String userId, int tryCount) {
        String tenantId = TenantContextHolder.getTenantId();
        String uniqueId = mulitSearchLog.getUniqueId();
        if (tryCount > 3) {
            log.error("租户[{}]人员[{}]钉钉考勤记录上传重试次数已经达到3次，上传失败！", tenantId, uniqueId);
            return;
        }
        String deviceSn = mulitSearchLog.getDeviceSn();
        String faceImgUrl = mulitSearchLog.getSceneFaceImage();
        Date receivedTime = mulitSearchLog.getReceivedTime();
        Long userCheckTime = receivedTime.getTime();
        String photoUrl = null;
        if (StringUtils.isNotBlank(faceImgUrl)) {
            photoUrl =
                serverDomain + contextPath + "/api/standard/getImgStreamByPath?path=" + AESUtils.encryptAES(faceImgUrl);
        }
        try {
            OapiAttendanceRecordUploadResponse resp =
                dingTalk.attendanceRecordUpload(userId, deviceSn, deviceName, photoUrl, userCheckTime);
            if (resp.getErrcode() != 0) {
                log.error("上传钉钉考勤记录异常,租户ID:[{}],errorCode:[{}], errmsg:[{}]", tenantId, resp.getErrcode(),
                    resp.getErrmsg());
                executeUploadAttendanceRecord(dingTalk, mulitSearchLog, deviceName, userId, ++tryCount);
            }
            log.info("上传钉钉考勤记录成功,租户ID:[{}],uniqueId:[{}]", tenantId, uniqueId);
        } catch (ApiException e) {
            log.error("上传钉钉考勤记录异常,租户ID:[{}],errcode:[{}], errmsg:[{}]", tenantId, e.getErrCode(), e.getErrMsg(), e);
            executeUploadAttendanceRecord(dingTalk, mulitSearchLog, deviceName, userId, ++tryCount);
        }
    }

    /**
     * 推送异常体温人员信息到微信公众号
     *
     * @param faceirisSearchLog
     */
    private void sendAbnormalTemperatureWeixinMsg(PersonFaceirisSearchLog faceirisSearchLog) {
        String tenantId =
            StringUtils.isBlank(TenantContextHolder.getTenantId()) ? null : TenantContextHolder.getTenantId();
        if (!DictConstants.TemperatureResult.HIGHER.equals(faceirisSearchLog.getTemperatureResult()))
            return;
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String configKey = SysConfigConstants.ABNORMAL_TEMPURATURE_PUSH_WEIXIN_PARAMS;
            String weixinParamJson = sysConfigService.selectConfigByKey(configKey);
            if (StringUtils.isBlank(weixinParamJson)) {
                log.warn("租户[{}]未配置异常体温推送微信公众号参数[" + configKey + "]", tenantId);
                return;
            }
            log.info("租户[{}]异常体温推送微信公众号参数:[{}]", tenantId, weixinParamJson);
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
                    log.warn("租户[{}]平台系统参数[" + configKey + "]格式错误， appId:[{}], templateId[{}], phone:[{}]", tenantId,
                        appId, templateId, phoneStr);
                    return;
                }
            } catch (Exception e) {
                log.error("租户[{}]平台系统参数[" + configKey + "]格式错误", e);
                return;
            }
            MsgWeixinSendInfo sendInfo = new MsgWeixinSendInfo();
            sendInfo.setReceivedSeq(IdWorker.getNextStringId());
            sendInfo.setAppId(appId);
            sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
            sendInfo.setMsgSubject("体温异常提醒");
            sendInfo.setTemplateId(templateId);
            sendInfo.setPhoneStr(phoneStr);
            List<TemplateData> list = Lists.newArrayList();
            // 姓名
            list.add(new TemplateData("keyword1", faceirisSearchLog.getPersonName()));
            // 编号
            list.add(new TemplateData("keyword2", faceirisSearchLog.getUniqueId()));
            // 体温
            list.add(new TemplateData("keyword3", String.valueOf(faceirisSearchLog.getTemperature())));
            // 地点
            list.add(new TemplateData("keyword4", faceirisSearchLog.getDeviceAddr()));
            // 时间
            list.add(new TemplateData("keyword5",
                DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, faceirisSearchLog.getReceivedTime())));
            // 备注
            list.add(new TemplateData("remark", "请及时联系相关人员处理~"));
            sendInfo.setTemplateData(list);
            msgSendService.sendWeixinMessage(sendInfo);
        });
    }

    /**
     * 根据时间范围查询人脸虹膜多模态识别日志
     *
     * @param personFaceirisSearchLog
     * @return
     */
    @Override
    public List<PersonFaceirisSearchLog>
        selectPersonFaceIrisSearchLogByTimeRange(PersonFaceirisSearchLog personFaceirisSearchLog) {
        return personFaceirisSearchLogMapper.selectPersonFaceIrisSearchLogByTimeRange(personFaceirisSearchLog);
    }
}
