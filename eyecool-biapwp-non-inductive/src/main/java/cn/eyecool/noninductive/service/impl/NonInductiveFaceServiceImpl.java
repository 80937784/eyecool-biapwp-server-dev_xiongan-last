package cn.eyecool.noninductive.service.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eyecool.abis.callmicroservice.IBioFaceMicroService;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceSearchResult;
import com.eyecool.syncrmduplicate.service.DuplicateResponse;
import com.eyecool.syncrmduplicate.service.RmDuplicateService;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.exception.user.UserPasswordNotMatchException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.uuid.UUID;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.framework.manager.AsyncManager;
import cn.eyecool.framework.manager.factory.AsyncFactory;
import cn.eyecool.noninductive.NonInductiveConstants;
import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;
import cn.eyecool.noninductive.disruptor.event.RecognizeHitEvent;
import cn.eyecool.noninductive.disruptor.event.RecognizeStrangerEvent;
import cn.eyecool.noninductive.disruptor.queue.FaceSearceResultNoticeDisruptorQueue;
import cn.eyecool.noninductive.disruptor.queue.RecognizeHitDisruptorQueue;
import cn.eyecool.noninductive.disruptor.queue.RecognizeStrangerDisruptorQueue;
import cn.eyecool.noninductive.domain.Bio1nSearchResult;
import cn.eyecool.noninductive.service.IBioFaceService;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.service.IChannelBusinessService;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.service.IPersonFaceSearchLogService;

/**
 * 人脸搜索接口实现
 *
 * @author 李强
 * @version [版本号, 2019年5月8日]
 * @since [应用/版本]
 */
@Service("nonInductive")
public class NonInductiveFaceServiceImpl implements IBioFaceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonInductiveFaceServiceImpl.class);

    private static final AtomicLong REQUEST_SEQUENCE = new AtomicLong();

    @Value("${image1nSearch:true}")
    private boolean isSearch;

    @Value("${duplicate.threshold:70}")
    private int duplicateThreshold;

    @Value("${duplicate.time:0}")
    private long duplicateTime;

    @Autowired
    private IBioFaceMicroService bioFaceMicroService;

    @Autowired
    private RmDuplicateService rmDuplicateService;

    @Value("${biapwp.dfrs.message.send.switch:false}")
    private boolean sendMssSwitch;

    @Value("${duplicate.algs.run:false}")
    private boolean duplicateAlgsRun;

    @Value("${biapwp.dfrs.abis.enable:true}")
    private boolean abisEnable;

    @Value("${biapwp.dfrs.hkTherm.enable:false}")
    private boolean biapwpDfrsHkThermEbable;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IPersonFaceSearchLogService personFaceSearchLogService;

    @Autowired
    private IDeviceInfoService deviceInfoService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IBasePersonInfoService basePersonInfoService;

    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;

    @Autowired
    private IChannelBusinessService channelBusinessService;

    @Autowired
    private IBasePersonFaceService basePersonFaceService;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public void search(byte[] imageContent, String deviceSerialNo, JSONObject comment) {
        // faceId存在，并且质量比之前的好，替换
        String topNString = ChannelParamConstants.FACE_SEARCH_TOP_N_VALUE.toString();
        Integer topN = StringUtils.isNotEmpty(topNString) ? Integer.parseInt(topNString)
            : ChannelParamConstants.FACE_SEARCH_TOP_N_VALUE;
        String uuid = Thread.currentThread().getName();
        String captureImageRootPath = configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_SEARCH_DIR_KEY);
        long matchStart = System.currentTimeMillis();
        String configStrangerPush =
            configService.selectConfigByKey(NonInductiveConstants.BIAPWP_NON_INDUCTIVE_STRANGER_PUSH);
        boolean pushStrangerToView = Boolean.parseBoolean(configStrangerPush);
        LOGGER.debug("abis服务是否打开:[{}],陌生人是否推送到前端:[{}]", abisEnable, pushStrangerToView);
        if (StringUtils.isEmpty(deviceSerialNo)) {
            LOGGER.error("---deviceSerialNo is null---");
            return;
        }
        DeviceInfo deviceInfo = null;
        if (redisCache.getCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceSerialNo) != null) {
            deviceInfo = redisCache.getCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceSerialNo);
            if (deviceInfo == null) {
                DeviceInfo condition = new DeviceInfo();
                condition.setDeviceNo(deviceSerialNo.trim());
                List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
                if (CollectionUtils.isNotEmpty(list)) {
                    deviceInfo = list.get(0);
                    redisCache.setCacheObject(deviceSerialNo, deviceInfo);
                }
            }
        } else {
            deviceInfo = getClientByDeviceNo(deviceSerialNo.trim());
            redisCache.setCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceSerialNo, deviceInfo);
        }
        LOGGER.debug("query device end uuid [{}],used time [{}]", uuid, System.currentTimeMillis() - matchStart);
        if (deviceInfo == null) {
            LOGGER.error("设备 {} 不存在", deviceSerialNo);
            return;
        }
        // 不是无感设备
        if (!DictConstants.DeviceType.NONINDUCTIVE_DEVICE.equals(deviceInfo.getDeviceType())) {
            LOGGER.error("设备 [{}] 不是无感设备！", deviceInfo.getDeviceNo());
            return;
        }
        // String deviceSubtreasuryIds = null;
        // if (StringUtils.isEmpty(deviceInfo.getSubtreasuryCode())) {
        // deviceSubtreasuryIds = null;
        // } else if
        // (redisCache.getCacheObject(DictConstants.DeviceCachePrefix.SUBTREASURY_DEVICE_CACHE_PREFIX
        // + deviceInfo.getDeviceNo()) != null) {
        // deviceSubtreasuryIds =
        // redisCache.getCacheObject(DictConstants.DeviceCachePrefix.SUBTREASURY_DEVICE_CACHE_PREFIX
        // + deviceInfo.getDeviceNo());
        // LOGGER.debug("从缓存中获取设备组信息 ,设备Id [{}],设备组 大小 [{}]", deviceInfo.getDeviceNo(),
        // deviceSubtreasuryIds == null ? "0" : Convert.toStrArray(",",
        // deviceSubtreasuryIds).length);
        // }
        // if (StringUtils.isEmpty(deviceSubtreasuryIds)&& LOGGER.isDebugEnabled()) {
        // LOGGER.debug("设备:[{}]无区域分库，即subtreasuryDevices表没有关联数据，现在设定设备不绑定人员库即可进行识别，温控版本更改",
        // deviceInfo.getDeviceIp());
        // }
        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                personFaceRecogLogicService.checkLive(Base64.encodeBase64String(imageContent), null);
            if (!checkLiveResponse.getResult()) {
                LOGGER.error("人脸入库活体检测未通过，设备serialNo [{}]!", deviceSerialNo);
                throw new CustomException("人脸图片活体检测未通过，请检查!");
            }
        }
        long featureStart = System.currentTimeMillis();
        String feature = null;
        String filePath = personFaceRecogLogicService.uploadFaceImg(true, null, Base64.encodeBase64String(imageContent),
            captureImageRootPath);
        LOGGER.debug("file saved, filePath:{}", filePath);
        if (abisEnable) {
            List<String> features =
                personFaceRecogLogicService.getFaceExtractResult(Base64.encodeBase64String(imageContent)).getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                feature = null;
            } else {
                feature = features.get(0);
            }
        }
        LOGGER.debug("get feature end uuid [{}],used [{}]", uuid, System.currentTimeMillis() - featureStart);
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = currentTime.format(formatter);
        PersonFaceSearchLog log = new PersonFaceSearchLog();
        long startTimeLong = System.currentTimeMillis();
        log.setId(IdWorker.getNextStringId());
        log.setCreateTime(DateTime.parse(formattedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate());
        log.setSceneImage(filePath);
        log.setReceivedTime(DateTime.parse(formattedDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate());
        log.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_LOG_BAK);
        log.setReceivedSeq(IdWorker.getNextStringId());
        log.setChannelCode(deviceInfo.getChannelCode());
        log.setDeviceCode(deviceInfo.getDeviceNo());
        log.setDeviceName(deviceInfo.getDeviceName());
        log.setSubTreasuryCode(deviceInfo.getSubtreasuryCode());
        log.setDeviceModel(deviceInfo.getDeviceModelCode());
        if (StringUtils.isBlank(feature)) {
            log.setResult(DictConstants.BioResult.NOTPASS);
            log.setSceneStockScore(0d);
            if (pushStrangerToView(comment, pushStrangerToView, deviceInfo, filePath, formattedDate)) {
                long endTimeLong = System.currentTimeMillis();
                log.setTimeUsed(endTimeLong - startTimeLong);
                personFaceSearchLogService.insertPersonFaceSearchLog(log);
            }
            return;
        }
        if (isSearch && abisEnable) {
            // :调用人脸搜索为微服务
            long searchStart = System.currentTimeMillis();
            double threshold = Double
                .parseDouble(configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY));
            boolean isHit = false;
            FaceSearchResult maxResult = new FaceSearchResult();
            // 如果设备关联组大于1即用组合参数，否则直接设置groupID
            List<FaceSearchResult> result = new ArrayList<>();
            try {
                result = personFaceRecogLogicService.faceSearchN(feature, deviceInfo.getChannelCode(), topN, threshold);
                isHit = this.isFaceHit(result, threshold);
                maxResult = this.getMaxCompareResult(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("search complete time used[{}] result [{}] result list [{}]",
                    (System.currentTimeMillis() - searchStart), result, result.size());
            }
            if (CollectionUtils.isEmpty(result) && !pushStrangerToView) {
                // 不向前端发布陌生人事件
                LOGGER.debug("无搜索结果，并且不置为陌生人");
                log.setSceneStockScore(0d);
                log.setResult(DictConstants.BioResult.NOTPASS);
                long endTimeLong = System.currentTimeMillis();
                log.setTimeUsed(endTimeLong - startTimeLong);
                personFaceSearchLogService.insertPersonFaceSearchLog(log);
                return;
            }
            // 保存识别记录
            if (maxResult == null) {
                LOGGER.debug("无搜索结果，置为陌生人");
                log.setSceneStockScore(0d);
            } else {
                log.setSceneStockScore(maxResult.getScore());
                isHit = true;
                assert result != null;
                Optional.ofNullable(result.get(0).getUserId()).ifPresent(id -> {
                    BasePersonFace face = new BasePersonFace();
                    face.setUniqueId(id);
                    face.setStatus(DictConstants.Status.ENABLE);
                    List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(face);
                    if (CollectionUtils.isNotEmpty(faceList)) {
                        BasePersonFace basePersonFace = faceList.get(0);
                        log.setStockImage(basePersonFace.getImageUrl());
                    }
                });
            }
            ChannelBusiness channelBusiness;
            // 如果缓存中存在此人，则直接在缓存中获取此人的信息
            if (maxResult != null && redisCache
                .getCacheObject(RedisKeyConstants.CHANNEL_BUSINESS_CACHE_PREFIX + maxResult.getUserId()) != null) {
                channelBusiness =
                    redisCache.getCacheObject(RedisKeyConstants.CHANNEL_BUSINESS_CACHE_PREFIX + maxResult.getUserId());
                isHit = true;
            } else {
                // 如果缓存中不存在此人，则查询数据库获取此人的信息，查询后将此人放置缓存中
                LOGGER.debug("缓存中不存在此人,maxResult is null ? {}", maxResult == null);
                if (maxResult == null) {
                    log.setResult(DictConstants.BioResult.NOTPASS);
                    channelBusiness = null;
                    isHit = false;
                } else {
                    ChannelBusiness condition = new ChannelBusiness();
                    condition.setUniqueId(maxResult.getUserId());
                    condition.setChannelCode(deviceInfo.getChannelCode());
                    List<ChannelBusiness> channelBusinesses =
                        channelBusinessService.selectChannelBusinessList(condition);
                    if (CollectionUtils.isEmpty(channelBusinesses)) {
                        log.setResult(DictConstants.BioResult.NOTPASS);
                        channelBusiness = null;
                        isHit = false;
                    } else {
                        channelBusiness = channelBusinesses.get(0);
                        redisCache
                            .deleteObject(RedisKeyConstants.CHANNEL_BUSINESS_CACHE_PREFIX + maxResult.getUserId());
                        redisCache.setCacheObject(
                            RedisKeyConstants.CHANNEL_BUSINESS_CACHE_PREFIX + maxResult.getUserId(), channelBusiness);
                        isHit = true;
                    }
                }
                LOGGER.debug("channelBusiness is null ? {}", channelBusiness == null);

            }

            // 未命中
            if (!isHit) {
                LOGGER.debug("isHit {},duplicateAlgsRun {}", isHit, duplicateAlgsRun);
                if (duplicateAlgsRun) {
                    try {
                        String configDuplicateTime =
                            configService.selectConfigByKey("bio.personDuplicateCache.personRecogniseCache");
                        LOGGER.debug("deviceSerialNo:{}", deviceSerialNo);
                        DuplicateResponse duplicateResponse = rmDuplicateService.duplicateDetection(
                            StringUtils.isEmpty(deviceSerialNo) ? "1" : deviceSerialNo,
                            feature.getBytes(StandardCharsets.UTF_8), duplicateThreshold,
                            StringUtils.isEmpty(configDuplicateTime) ? this.duplicateTime
                                : Long.parseLong(configDuplicateTime) * 1000);
                        LOGGER.debug("组件提取的特征是否重复:{}", duplicateResponse.isResult());
                        if (duplicateResponse.isResult()) {
                            LOGGER.debug("deviceSerialNo:{} 陌生人组件识别重复，不进行陌生人推送。", deviceSerialNo);
                        } else {
                            LOGGER.debug("deviceSerialNo:{} 陌生人组件识别未重复，进行陌生人推送。", deviceSerialNo);
                            log.setResult(DictConstants.BioResult.NOTPASS);
                            long endTimeLong = System.currentTimeMillis();
                            log.setTimeUsed(endTimeLong - startTimeLong);
                            personFaceSearchLogService.insertPersonFaceSearchLog(log);
                            pushStrangerToView(comment, pushStrangerToView, deviceInfo, filePath, formattedDate);
                        }
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.error(e.getMessage());
                        return;
                    }
                } else {
                    LOGGER.debug("duplicateAlgsRun开关关闭，进行陌生人推送。");
                    log.setResult(DictConstants.BioResult.NOTPASS);
                    long endTimeLong = System.currentTimeMillis();
                    log.setTimeUsed(endTimeLong - startTimeLong);
                    personFaceSearchLogService.insertPersonFaceSearchLog(log);
                    pushStrangerToView(comment, pushStrangerToView, deviceInfo, filePath, formattedDate);
                    return;
                }
            }
            long queryPersonInfoStart = System.currentTimeMillis();
            LOGGER.debug("query personInfo end uuid [{}],used time [{}]", uuid,
                System.currentTimeMillis() - queryPersonInfoStart);
            log.setUniqueId(Objects.requireNonNull(channelBusiness).getUniqueId());
            long queryFeatureStart = System.currentTimeMillis();
            LOGGER.debug("query feature end uuid [{}] ,used time [{}]", uuid,
                System.currentTimeMillis() - queryFeatureStart);
            log.setResult(DictConstants.BioResult.PASS);
            long cacheStart = System.currentTimeMillis();
            Long cacheTime = redisCache
                .getCacheObject(RedisKeyConstants.PERSON_RECOGNISE_CACHE_PREFIX + channelBusiness.getUniqueId());
            if (redisCache
                .getCacheObject(RedisKeyConstants.PERSON_RECOGNISE_CACHE_PREFIX + channelBusiness.getUniqueId()) != null
                && (System.currentTimeMillis() - cacheTime) < duplicateTime) {
                LOGGER.debug("[{}]在指定时间内重复出现，过滤 [{}] feature id [{}]", maxResult.getFeatureId(), maxResult,
                    maxResult.getFeatureId());
                return;
            } else {
                // 未在指定时间内重复出现
                redisCache.setCacheObject(
                    RedisKeyConstants.PERSON_RECOGNISE_CACHE_PREFIX + channelBusiness.getUniqueId(),
                    System.currentTimeMillis());
                LOGGER.debug("首位命中 [{}]", maxResult);
                // 向其他业务系统推送识别结果（此处是否应该启用多线程队列的方式）
                if (sendMssSwitch) {
                    FaceSearchResultNoticeEvent.FaceSearchResultMessage message =
                        new FaceSearchResultNoticeEvent.FaceSearchResultMessage();
                    message.setPersonId(channelBusiness.getUniqueId());
                    message.setResult("1");
                    message.setTmplImageUrl(log.getSceneImage());
                    Optional.ofNullable(deviceInfo).ifPresent(i -> {
                        message.setDeviceAddr(i.getDeviceAddr());
                        message.setDeviceNo(i.getDeviceNo());
                    });
                    message.setLiveFaceDataUrl(filePath);
                    message.setMatchScore(maxResult.getScore());
                    message.setMatchTime(formattedDate);
                    FaceSearceResultNoticeDisruptorQueue.publishEvent(message);
                }
            }
            LOGGER.debug("personDuplicateCache end uuid [{}] ,used time [{}]", uuid,
                System.currentTimeMillis() - cacheStart);
            // 未缓存（第一次出现）或者缓存了但是不去重
            String stringBase64 = PlatformFileUtils.getImageBase64(filePath);
            if (StringUtils.isNotBlank(stringBase64)) {
                stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
            }
            String uniqueId = channelBusiness.getUniqueId();
            BasePersonInfo condition=new BasePersonInfo();
            condition.setUniqueId(uniqueId);
            BasePersonInfo basePersonInfo = basePersonInfoService.selectByUidOrName(condition).get(0);
            RecognizeHitEvent.RecognizeHitResultMessage resultMsg =
                new RecognizeHitEvent.RecognizeHitResultMessage(channelBusiness.getUniqueId(), formattedDate,
                    deviceInfo == null ? "" : deviceInfo.getDeviceNo(), stringBase64, basePersonInfo.getName(),
                    maxResult.getScore(), "hit", comment == null ? null : comment.toJSONString());
            RecognizeHitDisruptorQueue.publishEvent(resultMsg);
            long endTimeLong = System.currentTimeMillis();
            log.setTimeUsed(endTimeLong - startTimeLong);
            log.setPersonName(basePersonInfo.getName());
            SysDept sysDept = sysDeptMapper.selectDeptById(basePersonInfo.getDeptId());
            log.setDeptName(null == sysDept ? null : sysDept.getDeptName());
            personFaceSearchLogService.insertPersonFaceSearchLog(log);
        } else {
            log.setResult(DictConstants.BioResult.NOTPASS);
            log.setSceneStockScore(0d);
            long endTimeLong = System.currentTimeMillis();
            log.setTimeUsed(endTimeLong - startTimeLong);
            personFaceSearchLogService.insertPersonFaceSearchLog(log);
        }
        LOGGER.debug("总耗时 uuid [{}] [{}]", uuid, (System.currentTimeMillis() - matchStart));
    }

    private boolean pushStrangerToView(JSONObject comment, boolean pushStrangerToView, DeviceInfo deviceInfo,
        String filePath, String formattedDate) {
        String stringBase64 = PlatformFileUtils.getImageBase64(filePath);
        if (StringUtils.isNotBlank(stringBase64)) {
            stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
        }
        if (pushStrangerToView) {
            RecognizeStrangerEvent.RecognizeStrangerMessage strangerMsg =
                new RecognizeStrangerEvent.RecognizeStrangerMessage(formattedDate, deviceInfo.getDeviceNo(),
                    stringBase64, comment == null ? null : comment.toJSONString());
            RecognizeStrangerDisruptorQueue.publishEvent(strangerMsg);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("push stranger formattedDate:{}", formattedDate);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Bio1nSearchResult> search(byte[] feature, String groupName, Integer topCount, Double threshold) {
        long searchStart = System.currentTimeMillis();
        String topNString = ChannelParamConstants.FACE_SEARCH_TOP_N_VALUE.toString();
        Integer topN = StringUtils.isNotEmpty(topNString) ? Integer.parseInt(topNString)
            : ChannelParamConstants.FACE_SEARCH_TOP_N_VALUE;
        List<FaceSearchResult> faceSearchResults;
        try {
            faceSearchResults = bioFaceMicroService.faceMiniSearch("",
                Long.toString(REQUEST_SEQUENCE.incrementAndGet()), Base64.encodeBase64String(feature),
                Convert.toStrArray(groupName).length > 1 ? "" : groupName, topCount == null ? topN : topCount,
                threshold == null
                    ? Double.parseDouble(
                        configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY))
                    : threshold);
        } catch (Exception e) {
            e.printStackTrace();
            faceSearchResults = Collections.emptyList();
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("search complete time used[{}] result [{}]", (System.currentTimeMillis() - searchStart),
                faceSearchResults);
        }

        if (faceSearchResults == null) {
            return Collections.emptyList();
        }
        List<Bio1nSearchResult> list = new ArrayList<>();
        faceSearchResults.forEach(t -> {
            Bio1nSearchResult r = new Bio1nSearchResult();
            BasePersonInfo basePersonInfo = basePersonInfoService.selectBasePersonInfoById(t.getUserId());
            if (basePersonInfo != null) {
                r.setPersonName(basePersonInfo.getName());
            }
            r.setImageId(t.getFeatureId());
            r.setFeatureId(t.getFeatureId());
            Optional.ofNullable(basePersonInfo).ifPresent(b -> r.setPersonCode(b.getUniqueId()));
            r.setScore((double)Math.round(t.getScore() * 100) / 100);
            list.add(r);
        });
        LOGGER.debug("排序之后  result list [{}]", list);
        return list;
    }

    @Override
    public List<Bio1nSearchResult> search(byte[] feature, String groupName, Integer topCount, Double threshold,
        String image) {
        long searchStart = System.currentTimeMillis();
        List<FaceSearchResult> faceSearchResults = null;
        try {
            faceSearchResults = bioFaceMicroService.faceMiniSearch("",
                Long.toString(REQUEST_SEQUENCE.incrementAndGet()), Base64.encodeBase64String(feature),
                Convert.toStrArray(groupName).length > 1 ? "" : groupName,
                topCount == null ? Integer.parseInt(configService.selectConfigByKey("bio.face.search.topn")) : topCount,
                threshold == null
                    ? Double.parseDouble(
                        configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY))
                    : threshold);
        } catch (Exception e) {
            e.printStackTrace();
            faceSearchResults = Collections.emptyList();
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("search complete time used[{}] result [{}]", (System.currentTimeMillis() - searchStart),
                faceSearchResults);
        }

        if (faceSearchResults == null) {
            return Collections.emptyList();
        }
        List<Bio1nSearchResult> list = new ArrayList<>();
        faceSearchResults.forEach(t -> {
            Bio1nSearchResult r = new Bio1nSearchResult();
            BasePersonInfo basePersonInfo = basePersonInfoService.selectBasePersonInfoById(t.getUserId());
            if (basePersonInfo != null) {
                r.setPersonName(basePersonInfo.getName());
            }
            r.setImageId(t.getFeatureId());
            r.setFeatureId(t.getFeatureId());
            Optional.ofNullable(basePersonInfo).ifPresent(b -> r.setPersonCode(b.getUniqueId()));
            r.setScore((double)Math.round(t.getScore() * 100) / 100);
            list.add(r);
        });
        LOGGER.debug("排序之后  result list [{}]", list);
        return list;
    }

    public boolean isFaceHit(List<FaceSearchResult> faceSearchResultList, double threshold) {
        if (org.springframework.util.CollectionUtils.isEmpty(faceSearchResultList)) {
            return false;
        }
        double maxScore = 0;
        if (CollectionUtils.isNotEmpty(faceSearchResultList)) {
            for (FaceSearchResult searchResult : faceSearchResultList) {
                if (searchResult.getScore() >= maxScore) {
                    maxScore = searchResult.getScore();
                }
            }
        }
        return maxScore >= threshold;
    }

    public FaceSearchResult getMaxCompareResult(Collection<FaceSearchResult> faceSearchResultList) {
        FaceSearchResult topResult = null;
        double maxScore = 0;
        if (CollectionUtils.isNotEmpty(faceSearchResultList)) {
            for (FaceSearchResult searchResult : faceSearchResultList) {
                if (searchResult.getScore() >= maxScore) {
                    maxScore = searchResult.getScore();
                    topResult = searchResult;
                }
            }
        }
        return topResult;
    }

    public boolean hkThermEnable() {
        return biapwpDfrsHkThermEbable;
    }

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
            LOGGER.error("设备[{}]不存在", deviceNo);
            return null;
        }
        return list.get(0);
    }

    @Resource
    private AuthenticationManager authenticationManager;

    @Override
    public JSONObject login(String username, String password) throws UnsupportedEncodingException {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + "show:" + username;
        redisCache.deleteObject(verifyKey);

        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me()
                    .execute(AsyncFactory.recordLogininfor(null, username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new CustomException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        String tenantId = loginUser.getUser().getTenantId();
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(tenantId, username, Constants.LOGIN_SUCCESS,
            MessageUtils.message("user.login.success")));
        // 生成token
        String token = AESUtils.encryptAES(tenantId + ":" + loginUser.getUsername() + UUID.fastUUID().toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("tenantId", tenantId);
        return jsonObject;
    }
}
