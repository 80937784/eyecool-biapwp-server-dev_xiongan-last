package cn.eyecool.tradelog.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.weixin4j.model.message.template.TemplateData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.eastcompeace.kmc.EcpAESCipherTools;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.http.HttpClientUtil;
import cn.eyecool.common.utils.uuid.IdUtils;
import cn.eyecool.framework.config.websocket.BigScreenWebSocketServer;
import cn.eyecool.framework.config.websocket.WebSocketServer;
import cn.eyecool.msg.trade.entity.MsgWeixinSendInfo;
import cn.eyecool.msg.trade.service.IMsgSendService;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDeptService;
import cn.eyecool.tradelog.domain.FaceRealtimeTradeLog;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.domain.RealtimeTradeLog;
import cn.eyecool.tradelog.domain.XACardPassBody;
import cn.eyecool.tradelog.domain.XACardPassHeader;
import cn.eyecool.tradelog.mapper.PersonFaceSearchLogMapper;
import cn.eyecool.tradelog.mapper.PersonHealthCodeLogMapper;
import cn.eyecool.tradelog.param.PersonFaceSearchBakLog;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸搜索日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-29
 */
@Slf4j
@Primary
@Service
public class PersonFaceSearchLogServiceImpl implements IPersonFaceSearchLogService {

    @Autowired
    private PersonFaceSearchLogMapper personFaceSearchLogMapper;
    @Autowired
    private IPersonFaceRecogLogicService faceRecogLogicService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private PersonHealthCodeLogMapper personHealthCodeLogMapper;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IMsgSendService msgSendService;

    private static final ConcurrentMap<String, String> cardPassTokenCache = new ConcurrentHashMap<>();

    /**
     * 查询人脸搜索日志
     * 
     * @param id 人脸搜索日志ID
     * @return 人脸搜索日志
     */
    @Override
    public PersonFaceSearchLog selectPersonFaceSearchLogById(String id) {
        return personFaceSearchLogMapper.selectPersonFaceSearchLogById(id);
    }

    /**
     * 查询人脸搜索日志列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog) {
        return personFaceSearchLogMapper.selectPersonFaceSearchLogList(personFaceSearchLog);
    }

    /**
     * 查询人脸搜索日志最新比对列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceSearchLog> selectLastPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog) {
        return personFaceSearchLogMapper.selectLastPersonFaceSearchLogList(personFaceSearchLog);
    }

    /**
     * 查询人脸搜索日志列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志
     */
    @Override
    public int selectPersonFaceSearchLogCount(PersonFaceSearchLog personFaceSearchLog) {
        return personFaceSearchLogMapper.selectPersonFaceSearchLogCount(personFaceSearchLog);
    }

    /**
     * 新增人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    @Override
    public int insertPersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog) {
        personFaceSearchLog.setCreateTime(DateUtils.getNowDate());
        return personFaceSearchLogMapper.insertPersonFaceSearchLog(personFaceSearchLog);
    }

    /**
     * 修改人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    @Override
    public int updatePersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog) {
        return personFaceSearchLogMapper.updatePersonFaceSearchLog(personFaceSearchLog);
    }

    /**
     * 批量删除人脸搜索日志
     * 
     * @param ids 需要删除的人脸搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceSearchLogByIds(String[] ids) {
        return personFaceSearchLogMapper.deletePersonFaceSearchLogByIds(ids);
    }

    /**
     * 删除人脸搜索日志信息
     * 
     * @param id 人脸搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceSearchLogById(String id) {
        return personFaceSearchLogMapper.deletePersonFaceSearchLogById(id);
    }

    /**
     * 保存人脸回传日志
     * 
     * @param faceSearchBakLog
     */
    @Override
    @Transactional
    public void savePersonFaceSearchBakLog(PersonFaceSearchBakLog faceSearchBakLog) {
        PersonFaceSearchLog faceSearchLog = new PersonFaceSearchLog();
        faceSearchLog.setId(IdWorker.getNextStringId());
        faceSearchLog.setHealthcodeLogId(faceSearchBakLog.getHealthcodeLogId());
        faceSearchLog.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_LOG_BAK);
        faceSearchLog.setAlgsVersion(faceSearchBakLog.getAlgsVersion());
        faceSearchLog.setChannelCode(faceSearchBakLog.getChannelCode());
        faceSearchLog.setSubTreasuryCode(faceSearchBakLog.getSubtreasuryCode());
        faceSearchLog.setSubTreasuryName(faceSearchBakLog.getSubtreasuryName());
        faceSearchLog.setCheckliveScore(StringUtils.isBlank(faceSearchBakLog.getCheckliveScore()) ? null
            : Double.valueOf(faceSearchBakLog.getCheckliveScore()));
        faceSearchLog.setCheckliveResult(
            StringUtils.isEmpty(faceSearchBakLog.getCheckliveResult()) ? null : faceSearchBakLog.getCheckliveResult());
        faceSearchLog.setDeviceCode(faceSearchBakLog.getDeviceCode());
        faceSearchLog.setDeviceName(faceSearchBakLog.getDeviceName());
        faceSearchLog.setDeviceIp(faceSearchBakLog.getDeviceIp());
        faceSearchLog.setDeviceModel(faceSearchBakLog.getDeviceModel());
        faceSearchLog.setDeviceAddr(faceSearchBakLog.getDeviceAddr());
        faceSearchLog.setDeviceLongitude(StringUtils.isBlank(faceSearchBakLog.getDeviceLongitude()) ? null
            : Double.valueOf(faceSearchBakLog.getDeviceLongitude()));
        faceSearchLog.setDeviceDimension(StringUtils.isBlank(faceSearchBakLog.getDeviceDimension()) ? null
            : Double.valueOf(faceSearchBakLog.getDeviceDimension()));
        faceSearchLog.setDeviceDirection(faceSearchBakLog.getDeviceDirection());
        faceSearchLog.setReceivedSeq(faceSearchBakLog.getReceivedSeq());
        faceSearchLog
            .setReceivedTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, faceSearchBakLog.getReceivedTime()));
        faceSearchLog.setCreateTime(faceSearchLog.getReceivedTime());
        faceSearchLog.setResult(faceSearchBakLog.getResult());
        faceSearchLog.setSceneStockScore(StringUtils.isBlank(faceSearchBakLog.getSceneStockScore()) ? null
            : Double.valueOf(faceSearchBakLog.getSceneStockScore()));
        faceSearchLog.setTemperature(StringUtils.isBlank(faceSearchBakLog.getTemperature()) ? null
            : Double.valueOf(faceSearchBakLog.getTemperature()));
        faceSearchLog.setTemperatureFloor(StringUtils.isBlank(faceSearchBakLog.getTemperatureFloor()) ? null
            : Double.valueOf(faceSearchBakLog.getTemperatureFloor()));
        faceSearchLog.setTemperatureTop(StringUtils.isBlank(faceSearchBakLog.getTemperatureTop()) ? null
            : Double.valueOf(faceSearchBakLog.getTemperatureTop()));
        faceSearchLog.setValidType(faceSearchBakLog.getValidType());
        faceSearchLog.setCardNo(faceSearchBakLog.getCardNo());
        if (StringUtils.isNotBlank(faceSearchBakLog.getTemperatureResult())) {
            faceSearchLog.setTemperatureResult(faceSearchBakLog.getTemperatureResult());
        } else if (null != faceSearchLog.getTemperature()) {
            Double temperatureVal = faceSearchLog.getTemperature();
            Double temperatureTopVal = faceSearchLog.getTemperatureTop();
            Double temperatureFloorVal = faceSearchLog.getTemperatureFloor();
            if (null != temperatureTopVal && temperatureVal >= temperatureTopVal) {
                faceSearchLog.setTemperatureResult(DictConstants.TemperatureResult.HIGHER);
            } else if (null != temperatureFloorVal && temperatureVal < temperatureFloorVal) {
                faceSearchLog.setTemperatureResult(DictConstants.TemperatureResult.LOWER);
            } else {
                faceSearchLog.setTemperatureResult(DictConstants.TemperatureResult.NORMAL);
            }
        }
        faceSearchLog.setBioRecognized(faceSearchBakLog.getBioRecognized());
        String sceneImageBase64 = faceSearchBakLog.getSceneImage();
        // 进行比对图片的上传
        String baseDir = getSysFaceSearchPicBaseDir();
        // 加密上传现场照
        faceSearchLog.setSceneImage(faceRecogLogicService.uploadFaceImg(true, null, sceneImageBase64, baseDir));
        String uniqueId = faceSearchBakLog.getUniqueId();
        String stockImageBase64 = faceSearchBakLog.getStockImage();
        if (StringUtils.isNotBlank(stockImageBase64)) {
            // 加密上传底库照
            faceSearchLog.setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
        } else if (StringUtils.isNotBlank(uniqueId)) {
            // 查询人员底库照
            BasePersonFace faceCondition = new BasePersonFace();
            faceCondition.setUniqueId(uniqueId);
            faceCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
            if (CollectionUtils.isNotEmpty(faceList)) {
                String imageUrl = faceList.get(0).getImageUrl();
                String encrypted = faceList.get(0).getEncrypted();
                stockImageBase64 = PlatformFileUtils.getImageBase64(imageUrl);
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(encrypted);
                stockImageBase64 =
                    isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
                if (null != stockImageBase64) {
                    faceSearchLog
                        .setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
                }
            }
        }
        if (StringUtils.isNotBlank(faceSearchBakLog.getTakePhoto1())) {
            // 加密上传抓拍照1
            faceSearchLog.setTakePhoto1(
                faceRecogLogicService.uploadFaceImg(true, null, faceSearchBakLog.getTakePhoto1(), baseDir));
        }
        if (StringUtils.isNotBlank(faceSearchBakLog.getTakePhoto2())) {
            // 加密上传抓拍照2
            faceSearchLog.setTakePhoto2(
                faceRecogLogicService.uploadFaceImg(true, null, faceSearchBakLog.getTakePhoto2(), baseDir));
        }
        faceSearchLog.setServerId(faceSearchBakLog.getServerId());
        if (StringUtils.isNotBlank(faceSearchBakLog.getTimeUsed())) {
            faceSearchLog.setTimeUsed(Long.valueOf(faceSearchBakLog.getTimeUsed()));
        }
        if (StringUtils.isNotBlank(uniqueId)) {
            faceSearchLog.setUniqueId(uniqueId);
            BasePersonInfo personInfo = getPersonInfo(uniqueId);
            faceSearchLog.setPersonName(null == personInfo ? null : personInfo.getName());
            Long deptId = null == personInfo ? null : personInfo.getDeptId();
            if (null != deptId) {
                faceSearchLog.setDeptId(deptId);
                SysDept sysDept = sysDeptService.selectDeptById(deptId);
                faceSearchLog.setDeptName(null != sysDept ? sysDept.getDeptName() : null);
            }
        }
        faceSearchLog.setVendorCode(faceSearchBakLog.getVendorCode());
        personFaceSearchLogMapper.insertPersonFaceSearchLog(faceSearchLog);
        // 推送实时交易
        execWebsocketSendMsg(faceSearchLog);
        execWebsocketSendMsgToBigScreen(faceSearchLog);
        // 推送高温信息到微信公众号
        sendAbnormalTemperatureWeixinMsg(faceSearchLog);
        /** 刷卡的记录 雄安管委会 推送的一卡通 */
        if (DictConstants.PassValidType.CARD.equals(faceSearchLog.getValidType())) {
            execXACardPassSendMsg(faceSearchLog);
        }
    }

    /**
     * 查询实时交易，推送给前端监控页面
     */
    private void execWebsocketSendMsg(PersonFaceSearchLog faceSearchLog) {
        String tenantId = StringUtils.isBlank(TenantContextHolder.getTenantId()) ? WebSocketServer.COMMON_CID
            : TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            try {
                RealtimeTradeLog realtimeTradeLog = new RealtimeTradeLog();
                realtimeTradeLog.setReceivedSeq(faceSearchLog.getReceivedSeq());
                realtimeTradeLog.setReceivedTime(faceSearchLog.getReceivedTime());
                realtimeTradeLog.setTenantId(tenantId);
                realtimeTradeLog.setUniqueId(faceSearchLog.getUniqueId());
                realtimeTradeLog.setPersonName(faceSearchLog.getPersonName());
                realtimeTradeLog.setResult(faceSearchLog.getResult());
                realtimeTradeLog.setDeviceNo(faceSearchLog.getDeviceCode());
                realtimeTradeLog.setDeviceName(faceSearchLog.getDeviceName());
                realtimeTradeLog.setDeviceAddr(faceSearchLog.getDeviceAddr());
                WebSocketServer.sendInfo(JSON.toJSONString(realtimeTradeLog), tenantId);
            } catch (IOException e) {
                log.error("Push face recognition real-time transaction exception:[{}]", e.getMessage(), e);
            }
        });
    }

    /**
     * 查询实时交易，推送给大屏监控页面
     */
    private void execWebsocketSendMsgToBigScreen(PersonFaceSearchLog faceSearchLog) {
        String tenantId = StringUtils.isBlank(TenantContextHolder.getTenantId()) ? UserConstants.SUPER_TENANT
            : TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            List<String> bigScreenTokenList = redisCache.getCacheList("bigscreen-token:" + tenantId);
            if (CollectionUtils.isEmpty(bigScreenTokenList)) {
                return;
            }
            FaceRealtimeTradeLog realtimeTradeLog = new FaceRealtimeTradeLog();
            realtimeTradeLog.setReceivedSeq(faceSearchLog.getReceivedSeq());
            realtimeTradeLog.setReceivedTime(faceSearchLog.getReceivedTime());
            realtimeTradeLog.setTenantId(tenantId);
            realtimeTradeLog.setUniqueId(faceSearchLog.getUniqueId());
            realtimeTradeLog.setPersonName(faceSearchLog.getPersonName());
            realtimeTradeLog.setResult(faceSearchLog.getResult());
            realtimeTradeLog.setDeviceNo(faceSearchLog.getDeviceCode());
            realtimeTradeLog.setDeviceName(faceSearchLog.getDeviceName());
            realtimeTradeLog.setDeviceAddr(faceSearchLog.getDeviceAddr());
            realtimeTradeLog.setTemperature(faceSearchLog.getTemperature());
            realtimeTradeLog.setTemperatureFloor(faceSearchLog.getTemperatureFloor());
            realtimeTradeLog.setTemperatureTop(faceSearchLog.getTemperatureTop());
            realtimeTradeLog.setTemperatureResult(faceSearchLog.getTemperatureResult());
            String healthcodeLogId = faceSearchLog.getHealthcodeLogId();
            if (StringUtils.isNotEmpty(healthcodeLogId)) {
                PersonHealthCodeLog healthCodeLog =
                    personHealthCodeLogMapper.selectPersonHealthCodeLogById(healthcodeLogId);
                if (null != healthCodeLog) {
                    String message = healthCodeLog.getMessage();
                    realtimeTradeLog.setHealthResult(healthCodeLog.getResult());
                    realtimeTradeLog.setHealthMessage(message);
                    realtimeTradeLog.setHealthCodeState(transHealthCode(message));
                }
            }
            bigScreenTokenList.parallelStream().forEach(cid -> {
                try {
                    BigScreenWebSocketServer.sendInfo(JSON.toJSONString(realtimeTradeLog), cid);
                } catch (IOException e) {
                    log.error("Pushing face recognition real-time transactions to the big screen is abnormal:[{}]",
                        e.getMessage(), e);
                }
            });
        });
    }

    /**
     * 推送异常体温人员信息到微信公众号
     *
     * @param request
     */
    private void sendAbnormalTemperatureWeixinMsg(PersonFaceSearchLog faceSearchLog) {
        String tenantId =
            StringUtils.isBlank(TenantContextHolder.getTenantId()) ? null : TenantContextHolder.getTenantId();
        if (!DictConstants.TemperatureResult.HIGHER.equals(faceSearchLog.getTemperatureResult()))
            return;
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String configKey = SysConfigConstants.ABNORMAL_TEMPURATURE_PUSH_WEIXIN_PARAMS;
            String weixinParamJson = sysConfigService.selectConfigByKey(configKey);
            if (StringUtils.isBlank(weixinParamJson)) {
                log.warn(
                    "Tenant [{}] does not configure the WeChat public account parameters for abnormal body temperature push["
                        + configKey + "]",
                    tenantId);
                return;
            }
            log.info("Tenant [{}] abnormal body temperature push WeChat public account parameters: [{}]", tenantId,
                weixinParamJson);
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
                    log.warn(
                        "Tenant [{}] platform system parameter [" + configKey
                            + "] is malformed, appId:[{}], templateId[{}], phone:[{}]",
                        tenantId, appId, templateId, phoneStr);
                    return;
                }
            } catch (Exception e) {
                log.error("Tenant [{}] platform system parameter [" + configKey + "] malformed", e);
                return;
            }
            MsgWeixinSendInfo sendInfo = new MsgWeixinSendInfo();
            sendInfo.setReceivedSeq(IdWorker.getNextStringId());
            sendInfo.setAppId(appId);
            sendInfo.setMsgType(DictConstants.MsgType.TEMPLATE_MSG);
            sendInfo.setMsgSubject(MessageUtils.message("person.face.search.temperature.abnormal"));
            sendInfo.setTemplateId(templateId);
            sendInfo.setPhoneStr(phoneStr);
            List<TemplateData> list = Lists.newArrayList();
            // 姓名
            list.add(new TemplateData("keyword1", faceSearchLog.getPersonName()));
            // 编号
            list.add(new TemplateData("keyword2", faceSearchLog.getUniqueId()));
            // 体温
            list.add(new TemplateData("keyword3", String.valueOf(faceSearchLog.getTemperature())));
            // 地点
            list.add(new TemplateData("keyword4", faceSearchLog.getDeviceAddr()));
            // 时间
            list.add(new TemplateData("keyword5",
                DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, faceSearchLog.getReceivedTime())));
            // 备注
            list.add(new TemplateData("remark", MessageUtils.message("health.code.service.msg.remark")));
            sendInfo.setTemplateData(list);
            msgSendService.sendWeixinMessage(sendInfo);
        });
    }

    /**
     * 健康码状态转换
     * 
     * @param message
     * @return
     */
    private String transHealthCode(String message) {
        String code = null;
        if (StringUtils.isEmpty(message)) {
            return code;
        }
        switch (message) {
            case "绿码":
                code = "0";
                break;
            case "黄码":
                code = "1";
                break;
            case "红码":
                code = "2";
                break;
            default:
                code = "-1";
                break;
        }
        return code;
    }

    /**
     * 获取人脸1:N搜索图片存放文件夹
     * 
     * @return
     */
    private String getSysFaceSearchPicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_SEARCH_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.face.service.image.search.folder.need",
                SysConfigConstants.BUSI_FACE_SEARCH_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 查询人员信息是否存在
     * 
     * @param uniqueId
     * @return
     */
    private BasePersonInfo getPersonInfo(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        return CollectionUtils.isEmpty(personList) ? null : personList.get(0);
    }

    /**
     * 根据时间范围查询日志
     * 
     * @param personFaceSearchLog
     * @return
     */
    @Override
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogByTimeRange(PersonFaceSearchLog personFaceSearchLog) {
        return personFaceSearchLogMapper.selectPersonFaceSearchLogByTimeRange(personFaceSearchLog);
    }

    @Override
    public void execXACardPassSendMsg(PersonFaceSearchLog faceSearchLog) {
        CompletableFuture.runAsync(() -> {
            try {
                XACardPassBody cardPassBody = new XACardPassBody();
                String cardNo = faceSearchLog.getCardNo();
                if (StringUtils.isEmpty(cardNo)) {
                    log.info("推送刷卡记录到一卡通异常cardNo is null");
                    return;
                }
                cardPassBody.setCardNo(cardNo);
                cardPassBody.setRecordTime(
                    DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, faceSearchLog.getReceivedTime()));
                String areaCode = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_AREACODE);
                String subCode = faceSearchLog.getSubTreasuryCode();
                if (StringUtils.isNotEmpty(areaCode)) {
                    cardPassBody.setAreaCode(areaCode);
                } else {
                    cardPassBody.setAreaCode(StringUtils.isNotEmpty(subCode) ? subCode : areaCode);
                }
                String body = JSONObject.toJSONString(cardPassBody);
                String recordUrl = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_RECORD_URL);
                log.info("刷卡记录到一卡通body[{}]", body);
                sendToOneCardPass(body, recordUrl, Constants.STATUS_ZERO);
            } catch (Exception e) {
                log.error("推送刷卡记录到一卡通实时交易异常:[{}]", e.getMessage(), e);
            }
        });
    }

    @Override
    public void sendToOneCardPass(String body, String url, String type) {
        String msg = "";

        XACardPassHeader cpHeader = new XACardPassHeader();
        if (Constants.STATUS_ZERO.equals(type)) {
            msg = "刷卡记录";
        } else if (Constants.STATUS_ONE.equals(type)) {
            msg = "权限状态回传";
            cpHeader.setCommandID("1002");
        }
        if (StringUtils.isEmpty(url)) {
            log.info("往一卡通推送[{}]数据失败,url is null", msg);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("往一卡通推送[{}]数据,url[{}]", msg, url);
        }
        String appId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_APPID);
        // 加密
        String enBodyStr = EcpAESCipherTools.encrypt(body);
        String random = IdUtils.getSevenRandom();
        long curTimeStamp = System.currentTimeMillis();
        cpHeader.setTimestamp(curTimeStamp + "");
        cpHeader.setTransactionID(curTimeStamp + random);
        cpHeader.setOpenId(appId);
        cpHeader.setToken(getCardPassToken());
        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("header", cpHeader);
        reqMap.put("body", enBodyStr);
        String request = JSONObject.toJSONString(reqMap);
        /** 往一卡通发送日志记录|权限状态 */
        String response = HttpClientUtil.doPostJson(url, request);
        JSONObject res = JSONObject.parseObject(response);
        String rspCode = res.getJSONObject("header").getString("rspCode");
        if ("0000".equals(rspCode)) {
            if (log.isDebugEnabled()) {
                log.debug("推送[{}]数据到一卡通request[{}]response[{}]", msg, request, response);
            }
        } else {
            log.error("推送[{}]数据到一卡通rspCode[{}]request[{}]response[{}]", msg, rspCode, request, response);
        }
    }

    /**
     *  getCardPassToken
     *  {
     *       "header": {
     *          "rspCode": "0000",
     *          "rspMessage": "成功"
     *        },
     *       "body": {
     *           "token": "6ff9493d586e18ffabe00dc5ff8ae149",
     *           "expires": 7200
     *       }
     *   }
     * @param
     * @return java.lang.String
     * @author zfx
     * @since 2022/11/29 11:19
     */
    private String getCardPassToken() {
        String tokenCa = cardPassTokenCache.get("cPToken");
        String expiresCa = cardPassTokenCache.get("cPTokenExpires");
        String cPTokenStart = cardPassTokenCache.get("cPTokenStart");
        if (StringUtils.isNotEmpty(tokenCa)) {
            long curTimp = System.currentTimeMillis();
            if (curTimp - Long.valueOf(cPTokenStart) < Long.valueOf(expiresCa)) {
                return tokenCa;
            }
        }
        Map<String, Object> request = Maps.newHashMap();
        Map<String, String> header = Maps.newHashMap();
        String sysName = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_SYSNAME);
        String appId = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_APPID);
        String appSecret = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_APPSECRET);
        String tokenUrl = sysConfigService.selectConfigByKey(SysConfigConstants.XA_CARD_PASS_TOKEN_URL);
        header.put("sysName", sysName);
        header.put("appId", appId);
        header.put("appSecret", appSecret);
        request.put("header", header);
        String req = JSONObject.toJSONString(request);
        String response = HttpClientUtil.doPostJson(tokenUrl, req);
        JSONObject resObj = JSONObject.parseObject(response);
        String code = resObj.getJSONObject("header").getString("rspCode");
        if ("0000".equals(code)) {
            String token = resObj.getJSONObject("body").getString("token");
            String expires = resObj.getJSONObject("body").getString("expires");
            cardPassTokenCache.put("cPToken", token);
            cardPassTokenCache.put("cPTokenExpires", expires);
            cardPassTokenCache.put("cPTokenStart", System.currentTimeMillis() + "");
            return token;
        } else {
            String msg = resObj.getJSONObject("header").getString("rspMessage");
            log.error("获取token失败code[{}]msg[{}]", code, msg);
        }
        return "";
    }
}
