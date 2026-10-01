package cn.eyecool.scene.trade.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Maps;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceSearchResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.google.common.collect.Lists;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DatamanagerConstants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.http.HttpClientUtil;
import cn.eyecool.match.service.commons.FeatureData;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelParam;
import cn.eyecool.scene.domain.ChannelSubtreasuryBusi;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelParamMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryBusiMapper;
import cn.eyecool.scene.mapper.ChannelSubtreasuryInfoMapper;
import cn.eyecool.scene.trade.entity.PersonFaceRecog;
import cn.eyecool.scene.trade.entity.PersonFaceVerify;
import cn.eyecool.scene.trade.entity.PersonIdentityVerification;
import cn.eyecool.scene.trade.service.IChannelBusiFaceHttpService;
import cn.eyecool.scene.trade.vo.PersonBioRecogBaseVO;
import cn.eyecool.scene.trade.vo.PersonBioRecogSceneVO;
import cn.eyecool.scene.trade.vo.PersonFaceRecogVO;
import cn.eyecool.scene.trade.vo.PersonFaceVerifyVO;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceCheckliveLog;
import cn.eyecool.tradelog.domain.PersonFaceMatchLog;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.mapper.PersonFaceCheckliveLogMapper;
import cn.eyecool.tradelog.mapper.PersonFaceMatchLogMapper;
import cn.eyecool.tradelog.mapper.PersonFaceSearchLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景人脸业务HTTP服务层实现
 *
 * @author admin
 * @date 2019年11月28日
 */
@Service
@Slf4j
public class ChannelBusiFaceHttpServiceImpl implements IChannelBusiFaceHttpService {

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelParamMapper channelParamMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ChannelSubtreasuryBusiMapper channelSubtreasuryBusiMapper;
    @Autowired
    private ChannelSubtreasuryInfoMapper channelSubtreasuryInfoMapper;
    @Autowired
    private IPersonFaceRecogLogicService faceRecogLogicService;
    @Autowired
    private PersonFaceMatchLogMapper faceMatchLogMapper;
    @Autowired
    private PersonFaceSearchLogMapper faceSearchLogMapper;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private PersonFaceCheckliveLogMapper faceCheckliveLogMapper;

    /**
     * 人脸1:1认证
     */
    @Override
    public PersonFaceVerifyVO verifyPersonFace(PersonFaceVerify faceVerify) {
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = faceVerify.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }
        // 判断场景是否开通人脸
        if (null != channelInfo) {
            String faceMode = channelInfo.getFaceMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(faceMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.common.service.scene.face.not.open", channelCode));
            }
        }
        // 是否校验人员合法
        boolean validPerson = false;
        if (null != channelInfo) {
            validPerson = getFaceMatchValidUniqueId(channelInfo.getId());
        }
        // 校验人员信息是否存在
        String uniqueId = faceVerify.getUniqueId();
        String personId = null;
        Long deptId = null;
        String personName = null;
        String deptName = null;
        if (validPerson) {
            BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);
            personId = basePersonInfo.getId();
            deptId = basePersonInfo.getDeptId();
            personName = basePersonInfo.getName();
            // 校验场景人员人员合法性
            checkChannelBusiValid(basePersonInfo, channelInfo);
        }
        if (null != deptId) {
            SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
            deptName = null == sysDept ? null : sysDept.getDeptName();
        }

        // 现场照图片
        String sceneImage = faceVerify.getSceneImage();
        // 如果是传输视频，则从视频提取照片
        boolean isVideo = Constants.STATUS_TWO.equals(faceVerify.getSceneMediaType());
        String videoBase64 = null;
        if (isVideo) {
            try {
                videoBase64 = new String(Base64.getEncoder().encode(faceVerify.getVideo().getBytes()));
            } catch (IOException e) {
                log.error("Getting video base64 error", e);
                throw new CustomException(MessageUtils.message("channel.face.service.video.base64.failed"));
            }
        }

        // 现场照检活
        String liveDetectionThresholdStr = faceVerify.getLiveDetectionThreshold();
        Double liveDetectionThreshold =
            StringUtils.isBlank(liveDetectionThresholdStr) ? null : Double.valueOf(liveDetectionThresholdStr);
        CheckLiveResponse checkLiveResponse = handleCheckLive(faceVerify.getLiveDetection(), liveDetectionThreshold,
            channelInfo == null ? null : channelInfo.getId(), sceneImage, false, isVideo, videoBase64);
        // 设置现场照faceVerify.setSceneImage
        if (isVideo) {
            faceVerify.setSceneImage(checkLiveResponse.getImage());
            sceneImage = faceVerify.getSceneImage();
        }

        // 查询底库人脸照片
        BasePersonFace basePersonFace = StringUtils.isNotBlank(personId) ? getBasePersonFace(personId) : null;
        // 定义比对List(有序放入现场照、联网核查照、芯片照、底库照,可以没有，顺序不能乱)
        List<FeatureBean> compareFeatureBeanList = handleFaceMatchFeatureExtract(faceVerify, basePersonFace);
        if (compareFeatureBeanList.size() < 2) {
            throw new CustomException(MessageUtils.message("channel.face.service.basephoto.not.exists"));
        }

        // 获取人脸比对阈值
        String compareThresholdStr = faceVerify.getCompareThreshold();
        double faceCompareThreshold = StringUtils.isBlank(compareThresholdStr)
            ? getFaceOne2OneCompareThreshold(null == channelInfo ? null : channelInfo.getId())
            : Double.valueOf(compareThresholdStr);
        // 进行1:1比对
        List<MatchBean> matchBeanList = faceRecogLogicService.faceOne2OneCompare(compareFeatureBeanList);

        // 处理比对结果并返回
        boolean hasOnlineImage = StringUtils.isNotBlank(faceVerify.getOnlineImage());
        boolean hasChipImage = StringUtils.isNotBlank(faceVerify.getChipImage());
        boolean hasStockImage = null != basePersonFace;
        PersonFaceVerifyVO faceVerifyVO = handleFaceMatchResult(checkLiveResponse, compareFeatureBeanList,
            matchBeanList, faceCompareThreshold, hasOnlineImage, hasChipImage, hasStockImage);
        // 异步保存比对日志
        asyncSaveFaceMatchLog(faceVerify, faceVerifyVO, basePersonFace, receivedTime, deptId, deptName, personName,
            matchBeanList, videoBase64);
        return faceVerifyVO;
    }

    /**
     * 获取人脸1：1是否检验人员合法性
     *
     * @param channelId
     * @return
     */
    private boolean getFaceMatchValidUniqueId(String channelId) {
        ChannelParam channelParam = getChannelParam(channelId, DictConstants.BioAttestType.FACE,
            ChannelParamConstants.FACE_COMPARE_UNIQUE_ID_VALIDATE_CODE);
        if (null != channelParam) {
            return DictConstants.YesOrNoState.YES.equals(channelParam.getParamValue());
        }
        // 默认校验
        return true;
    }

    /**
     * 获取场景参数
     *
     * @param channelId
     * @param bioAssestType
     * @param paramCode
     * @return
     */
    private ChannelParam getChannelParam(String channelId, String bioAssestType, String paramCode) {
        ChannelParam paramCondition = new ChannelParam();
        paramCondition.setChannelId(channelId);
        paramCondition.setBioAttestType(bioAssestType);
        paramCondition.setParamCode(paramCode);
        List<ChannelParam> paramList = channelParamMapper.selectChannelParamList(paramCondition);
        return CollectionUtils.isEmpty(paramList) ? null : paramList.get(0);
    }

    /**
     * 校验人员信息是否存在
     *
     * @param uniqueId
     * @return
     */
    private BasePersonInfo checkBasePersonExists(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        if (CollectionUtils.isEmpty(personList)) {
            throw new CustomException(MessageUtils.message("channel.common.service.person.not.exists", uniqueId));
        }
        return personList.get(0);
    }

    /**
     * 查询人脸信息
     *
     * @param personId
     * @return
     */
    private BasePersonFace getBasePersonFace(String personId) {
        BasePersonFace faceCondition = new BasePersonFace();
        faceCondition.setPersonId(personId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
        if (CollectionUtils.isEmpty(faceList)) {
            return null;
        }
        return faceList.get(0);
    }

    /**
     * 查询业务信息
     *
     * @param channelId
     * @param personId
     */
    private ChannelBusiness getChannelBusiness(String channelId, String personId) {
        ChannelBusiness busiCondition = new ChannelBusiness();
        busiCondition.setChannelId(channelId);
        busiCondition.setPersonId(personId);
        busiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelBusiness> businessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        return CollectionUtils.isEmpty(businessList) ? null : businessList.get(0);
    }

    /**
     * 校验场景人员人员合法性
     * 
     * @param basePersonInfo
     * @param channelInfo
     */
    private void checkChannelBusiValid(BasePersonInfo basePersonInfo, ChannelInfo channelInfo) {
        // 判断当前人员是否开通人脸
        String uniqueId = basePersonInfo.getUniqueId();
        String personId = basePersonInfo.getId();
        String channelId = channelInfo.getId();
        String channelCode = channelInfo.getChannelCode();
        ChannelBusiness channelBusiness = getChannelBusiness(channelId, personId);
        if (null == channelBusiness) {
            throw new CustomException(
                MessageUtils.message("channel.face.service.scene.person.not.exists", channelCode, uniqueId));
        }
        if (DictConstants.BioModeStatus.DISABLE.equals(channelBusiness.getFaceMode())) {
            throw new CustomException(
                MessageUtils.message("channel.face.service.scene.person.face.notopened", channelCode, uniqueId));
        }
        if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
            throw new CustomException(
                MessageUtils.message("channel.common.service.scene.person.locked", channelCode, uniqueId));
        }
    }

    /**
     * 获取人脸1：1比对阈值
     *
     * @param channelId
     * @return
     */
    private double getFaceOne2OneCompareThreshold(String channelId) {
        if (StringUtils.isNotBlank(channelId)) {
            ChannelParam channelParam = getChannelParam(channelId, DictConstants.BioAttestType.FACE,
                ChannelParamConstants.FACE_COMPARE_THRESHOLD_CODE);
            if (null != channelParam) {
                return Double.valueOf(channelParam.getParamValue());
            }
        }
        // 场景参数不存在，则查询公共参数配置
        return faceRecogLogicService.getOne2OneCompareThreshold();
    }

    /**
     * 1v1比对处理特征提取并有序返回
     * 
     * @param faceVerify
     * @param stockFace
     * @return
     */
    private List<FeatureBean> handleFaceMatchFeatureExtract(PersonFaceVerify faceVerify, BasePersonFace stockFace) {
        // 定义比对List(有序放入现场照、联网核查照、芯片照、底库照,可以没有，顺序不能乱)
        List<FeatureBean> compareFeatureBeanList = Lists.newArrayList();
        // 提取现场照人脸特征
        CompletableFuture<FeatureBean> sceneImageFeatureBeanFuture = CompletableFuture.supplyAsync(() -> {
            String sceneImgBase64 = faceVerify.getSceneImage();
            FeatureBean sceneImageFeatureBean = faceRecogLogicService.getFeatureBean(sceneImgBase64,
                MessageUtils.message("channel.face.service.liveimage.no.face"),
                MessageUtils.message("channel.face.service.liveimage.multi.face"));
            return sceneImageFeatureBean;
        });

        // 提取联网核查照人脸特征
        String onlineImage = faceVerify.getOnlineImage();
        CompletableFuture<FeatureBean> onlineImageFeatureBeanFuture = null;
        if (StringUtils.isNotBlank(onlineImage)) {
            onlineImageFeatureBeanFuture = CompletableFuture.supplyAsync(() -> {
                FeatureBean onlineImageFeatureBean = faceRecogLogicService.getFeatureBean(onlineImage,
                    MessageUtils.message("channel.face.service.onlineverify.no.face"),
                    MessageUtils.message("channel.face.service.onlineverify.multi.face"));
                return onlineImageFeatureBean;
            });
        }

        // 提取芯片照人脸特征
        String chipImage = faceVerify.getChipImage();
        CompletableFuture<FeatureBean> chipImageFeatureBeanFuture = null;
        if (StringUtils.isNotBlank(chipImage)) {
            chipImageFeatureBeanFuture = CompletableFuture.supplyAsync(() -> {
                FeatureBean chipImageFeatureBean = faceRecogLogicService.getFeatureBean(chipImage,
                    MessageUtils.message("channel.face.service.chipimage.no.face"),
                    MessageUtils.message("channel.face.service.chipimage.multi.face"));
                return chipImageFeatureBean;
            });
        }
        // 获取特征提取结果
        try {
            FeatureBean sceneImageFeatureBean = sceneImageFeatureBeanFuture.get(60000, TimeUnit.MILLISECONDS);
            compareFeatureBeanList.add(sceneImageFeatureBean);
        } catch (TimeoutException e) {
            log.error("Timeout for obtaining the feature extraction result of the scene photo", e);
            throw new CustomException(MessageUtils.message("channel.face.service.obtain.livephoto.feature.timeout"));
        } catch (Exception e) {
            log.error("Error in obtaining scene photo feature extraction results", e);
            if (e.getCause() instanceof CustomException) {
                throw new CustomException(e.getCause().getMessage());
            } else {
                throw new CustomException(MessageUtils.message("channel.face.service.obtain.livephoto.feature.error"));
            }
        }
        if (onlineImageFeatureBeanFuture != null) {
            try {
                FeatureBean onlineImageFeatureBean = onlineImageFeatureBeanFuture.get(60000, TimeUnit.MILLISECONDS);
                compareFeatureBeanList.add(onlineImageFeatureBean);
            } catch (TimeoutException e) {
                log.error("Timeout for obtaining the feature extraction result of the online verification photo", e);
                throw new CustomException(
                    MessageUtils.message("channel.face.service.obtain.onlineverify.feature.timeout"));
            } catch (Exception e) {
                log.error("Error in obtaining the feature extraction result of the online verification photo", e);
                if (e.getCause() instanceof CustomException) {
                    throw new CustomException(e.getCause().getMessage());
                } else {
                    throw new CustomException(
                        MessageUtils.message("channel.face.service.obtain.onlineverify.feature.error"));
                }
            }
        }
        if (chipImageFeatureBeanFuture != null) {
            try {
                FeatureBean chipImageFeatureBean = chipImageFeatureBeanFuture.get(1, TimeUnit.MINUTES);
                compareFeatureBeanList.add(chipImageFeatureBean);
            } catch (TimeoutException e) {
                log.error("Timeout for obtaining chip photo feature extraction results", e);
                throw new CustomException(
                    MessageUtils.message("channel.face.service.obtain.chipimage.feature.timeout"));
            } catch (Exception e) {
                log.error("Error getting chip photo feature extraction result", e);
                if (e.getCause() instanceof CustomException) {
                    throw new CustomException(e.getCause().getMessage());
                } else {
                    throw new CustomException(
                        MessageUtils.message("channel.face.service.obtain.chipimage.feature.error"));
                }
            }
        }
        if (null != stockFace) {
            FeatureBean stockImageFeatureBean = new FeatureBean();
            stockImageFeatureBean.setFeature(stockFace.getFeature());
            stockImageFeatureBean.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
            compareFeatureBeanList.add(stockImageFeatureBean);
        }
        return compareFeatureBeanList;
    }

    /**
     * 处理人脸1:1比对
     *
     * @param checkLiveResponse
     * @param compareFeatureBeanList
     * @param faceCompareThreshold
     * @param hasOnlineImage
     * @param hasChipImage
     * @param hasStockImage
     * @return
     */
    private PersonFaceVerifyVO handleFaceMatchResult(CheckLiveResponse checkLiveResponse,
        List<FeatureBean> compareFeatureBeanList, List<MatchBean> matchBeanList, double faceCompareThreshold,
        boolean hasOnlineImage, boolean hasChipImage, boolean hasStockImage) {
        if (CollectionUtils.isEmpty(matchBeanList)) {
            throw new CustomException(MessageUtils.message("channel.face.service.image.match.error"));
        }
        Boolean checkLiveResult = null == checkLiveResponse ? null : checkLiveResponse.getResult();
        Double checkliveScore = null == checkLiveResponse ? null : checkLiveResponse.getScore();
        PersonFaceVerifyVO verifyVO = new PersonFaceVerifyVO();
        if (null != checkLiveResult) {
            verifyVO.setLiveDetectionResult(
                checkLiveResult ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
            verifyVO.setLiveDetectionScore(String.valueOf(checkliveScore));
        }
        boolean result = null == checkLiveResult ? true : checkLiveResult;// 总的认证结果
        DecimalFormat df = new DecimalFormat("0.00");
        // 处理比对结果
        if (compareFeatureBeanList.size() == 4) {// 现场照、联网核查照、芯片照、底库照片均存在
            // 现场照与联网核查照比对结果
            double sceneOnlineCcore = matchBeanList.get(0).getResults().get(0).getScore();
            verifyVO.setSceneOnlineScore(df.format(sceneOnlineCcore));
            verifyVO.setSceneOnlineResult(sceneOnlineCcore > faceCompareThreshold ? DictConstants.BioResult.PASS
                : DictConstants.BioResult.NOTPASS);
            result = result && sceneOnlineCcore > faceCompareThreshold;
            // 现场照与芯片照比对结果
            double sceneChipScore = matchBeanList.get(0).getResults().get(1).getScore();
            verifyVO.setSceneChipScore(df.format(sceneChipScore));
            verifyVO.setSceneChipResult(
                sceneChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
            result = result && sceneChipScore > faceCompareThreshold;
            // 现场照与底库的比对结果
            double sceneStockScore = matchBeanList.get(0).getResults().get(2).getScore();
            verifyVO.setSceneStockScore(df.format(sceneStockScore));
            verifyVO.setSceneStockResult(sceneStockScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                : DictConstants.BioResult.NOTPASS);
            result = result && sceneStockScore > faceCompareThreshold;
            // 联网核查照与芯片照比对结果
            double onlineChipScore = matchBeanList.get(1).getResults().get(0).getScore();
            verifyVO.setOnlineChipScore(df.format(onlineChipScore));
            verifyVO.setOnlineChipResult(onlineChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                : DictConstants.BioResult.NOTPASS);
            result = result && onlineChipScore > faceCompareThreshold;
            verifyVO.setResult(result ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
        } else if (compareFeatureBeanList.size() == 3) {
            if (!hasOnlineImage) {// 没有联网核查照，存在芯片照和底库
                // 现场照与芯片照比对结果
                double sceneChipScore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneChipScore(df.format(sceneChipScore));
                verifyVO.setSceneChipResult(sceneChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneChipScore > faceCompareThreshold;
                // 现场照与底库的比对结果
                double sceneStockScore = matchBeanList.get(0).getResults().get(1).getScore();
                verifyVO.setSceneStockScore(df.format(sceneStockScore));
                verifyVO.setSceneStockResult(sceneStockScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneStockScore > faceCompareThreshold;
            } else if (!hasChipImage) {// 没有芯片照，存在联网核查照和底库
                // 现场照与联网核查照比对结果
                double sceneOnlineCcore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneOnlineScore(df.format(sceneOnlineCcore));
                verifyVO.setSceneOnlineResult(sceneOnlineCcore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneOnlineCcore > faceCompareThreshold;
                // 现场照与底库的比对结果
                double sceneStockScore = matchBeanList.get(0).getResults().get(1).getScore();
                verifyVO.setSceneStockScore(df.format(sceneStockScore));
                verifyVO.setSceneStockResult(sceneStockScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneStockScore > faceCompareThreshold;
            } else if (!hasStockImage) {// 没有底库照，存在联网核查照和芯片照
                // 现场照与联网核查照比对结果
                double sceneOnlineCcore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneOnlineScore(df.format(sceneOnlineCcore));
                verifyVO.setSceneOnlineResult(sceneOnlineCcore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneOnlineCcore > faceCompareThreshold;
                // 现场照与芯片照比对结果
                double sceneChipScore = matchBeanList.get(0).getResults().get(1).getScore();
                verifyVO.setSceneChipScore(df.format(sceneChipScore));
                verifyVO.setSceneChipResult(sceneChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneChipScore > faceCompareThreshold;
                // 联网核查照与芯片照比对结果
                double onlineChipScore = matchBeanList.get(1).getResults().get(0).getScore();
                verifyVO.setOnlineChipScore(df.format(onlineChipScore));
                verifyVO.setOnlineChipResult(onlineChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && onlineChipScore > faceCompareThreshold;
            }
        } else if (compareFeatureBeanList.size() == 2) {
            if (hasOnlineImage) {// 只有联网核查照
                // 现场照与联网核查照比对结果
                double sceneOnlineCcore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneOnlineScore(df.format(sceneOnlineCcore));
                verifyVO.setSceneOnlineResult(sceneOnlineCcore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneOnlineCcore > faceCompareThreshold;
            } else if (hasChipImage) {// 只有芯片照
                // 现场照与芯片照比对结果
                double sceneChipScore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneChipScore(df.format(sceneChipScore));
                verifyVO.setSceneChipResult(sceneChipScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneChipScore > faceCompareThreshold;
            } else if (hasStockImage) {// 只有底库照
                // 现场照与底库的比对结果
                double sceneStockScore = matchBeanList.get(0).getResults().get(0).getScore();
                verifyVO.setSceneStockScore(df.format(sceneStockScore));
                verifyVO.setSceneStockResult(sceneStockScore > faceCompareThreshold ? DictConstants.BioResult.PASS
                    : DictConstants.BioResult.NOTPASS);
                result = result && sceneStockScore > faceCompareThreshold;
            }
        }
        verifyVO.setResult(result ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
        return verifyVO;
    }

    /**
     * 检活逻辑
     *
     * @param afferenLliveDetection 是否检活入参
     * @param afferentThreshold 检活阈值人参
     * @param channelId 场景主键
     * @param sceneImage 现场照
     * @param isSearchN 是否是1-N搜索（是:1-N,否：1-1）
     * @param isVideo 是否视频检活
     * @param videoBase64 视频base64
     * @return
     */
    private CheckLiveResponse handleCheckLive(String afferenLliveDetection, Double afferentThreshold, String channelId,
        String sceneImage, boolean isSearchN, boolean isVideo, String videoBase64) {
        boolean isCheckLive = true;// 默认执行检活
        CheckLiveResponse checkLiveResponse = null;
        if (!isVideo) {// 图片根据参数配置确定是否检活，视频必须检活
            String checkliveParamKey = isSearchN ? ChannelParamConstants.FACE_SEARCH_N_CHECK_LIVE_CODE
                : ChannelParamConstants.FACE_COMPARE_CHECK_LIVE_CODE;
            if (StringUtils.isNotBlank(afferenLliveDetection)) {
                isCheckLive = DictConstants.YesOrNoState.YES.equals(afferenLliveDetection);
            } else {
                // 去场景参数查询该场景是否要进行检活操作,需要则检活，不需要则不检活
                if (StringUtils.isNotBlank(channelId)) {
                    ChannelParam channelParam =
                        getChannelParam(channelId, DictConstants.BioAttestType.FACE, checkliveParamKey);
                    if (null == channelParam) {// 没有配置，默认检活
                        isCheckLive = true;
                    } else {
                        String paramValue = channelParam.getParamValue();
                        isCheckLive = DictConstants.YesOrNoState.YES.equals(paramValue);
                    }
                }
            }
            if (!isCheckLive) {
                return checkLiveResponse;
            }
        }
        String checkliveThresholdParamKey = isSearchN
            ? (isVideo ? ChannelParamConstants.FACE_SEARCH_N_VIDEO_CHECK_LIVE_THRESHOLD_CODE
                : ChannelParamConstants.FACE_SEARCH_N_CHECK_LIVE_THRESHOLD_CODE)
            : (isVideo ? ChannelParamConstants.FACE_COMPARE_VIDEO_CHECK_LIVE_THRESHOLD_CODE
                : ChannelParamConstants.FACE_COMPARE_CHECK_LIVE_THRESHOLD_CODE);
        // 执行检活操作
        Double detectionThreshold = afferentThreshold;
        // 如果没传阈值，则进入场景参数查询阈值
        if (null == detectionThreshold && StringUtils.isNotBlank(channelId)) {
            ChannelParam channelParam =
                getChannelParam(channelId, DictConstants.BioAttestType.FACE, checkliveThresholdParamKey);
            detectionThreshold = null == channelParam ? null : Double.valueOf(channelParam.getParamValue());

        }
        if (isVideo) {
            return faceRecogLogicService.videoCheckLive(videoBase64, detectionThreshold);
        }
        return faceRecogLogicService.checkLive(sceneImage, detectionThreshold);
    }

    /**
     * 异步保存人脸1:1比对日志
     * 
     * @param faceVerify
     * @param verifyVO
     * @param stockPersonFace
     * @param receivedTime
     * @param deptId
     * @param matchBeanList
     * @param videoBase64
     */
    private void asyncSaveFaceMatchLog(PersonFaceVerify faceVerify, PersonFaceVerifyVO verifyVO,
        BasePersonFace stockPersonFace, long receivedTime, Long deptId, String deptName, String personName,
        List<MatchBean> matchBeanList, String videoBase64) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String stockImageBase64 = null;
            if (StringUtils.isNotBlank(stockPersonFace.getImageUrl())) {
                stockImageBase64 = PlatformFileUtils.getImageBase64(stockPersonFace.getImageUrl());
                stockImageBase64 = DictConstants.Encrypted.ENABLE.equals(stockPersonFace.getEncrypted())
                    ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
            }
            PersonFaceMatchLog faceMatchLog = new PersonFaceMatchLog();
            // 进行比对图片的上传
            String baseDir = getSysFaceOne2OnePicBaseDir();
            // 加密上传现场照
            if (StringUtils.isNotBlank(faceVerify.getSceneImage())) {
                faceMatchLog.setSceneImage(
                    faceRecogLogicService.uploadFaceImg(true, null, faceVerify.getSceneImage(), baseDir));
            }
            if (null != videoBase64) {
                String videoOriginalName = faceVerify.getVideo().getOriginalFilename();
                faceMatchLog
                    .setSceneVideo(faceRecogLogicService.uploadFaceImg(true, videoOriginalName, videoBase64, baseDir));
            }
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockImageBase64)) {
                faceMatchLog.setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
            }
            // 加密上传联网核查照
            if (StringUtils.isNotBlank(faceVerify.getOnlineImage())) {
                faceMatchLog.setOnlineImage(
                    faceRecogLogicService.uploadFaceImg(true, null, faceVerify.getOnlineImage(), baseDir));
            }
            // 加密上传芯片照
            if (StringUtils.isNotBlank(faceVerify.getChipImage())) {
                faceMatchLog
                    .setChipImage(faceRecogLogicService.uploadFaceImg(true, null, faceVerify.getChipImage(), baseDir));
            }
            faceMatchLog.setId(IdWorker.getNextStringId());
            faceMatchLog.setChannelCode(faceVerify.getChannelCode());
            faceMatchLog.setDeviceCode(faceVerify.getDeviceCode());
            if (StringUtils.isNotBlank(faceVerify.getDeviceDimension())) {
                faceMatchLog.setDeviceDimension(Double.parseDouble(faceVerify.getDeviceDimension()));
            }
            if (StringUtils.isNotBlank(faceVerify.getDeviceLongitude())) {
                faceMatchLog.setDeviceLongitude(Double.parseDouble(faceVerify.getDeviceLongitude()));
            }
            faceMatchLog.setDeviceIp(faceVerify.getDeviceIp());
            faceMatchLog.setDeviceModel(faceVerify.getDeviceModel());
            faceMatchLog.setDeviceName(faceVerify.getDeviceName());
            faceMatchLog.setDeviceDirection(faceVerify.getDeviceDirection());
            faceMatchLog.setReceivedSeq(faceVerify.getReceivedSeq());
            faceMatchLog.setCreateTime(DateUtils.getNowDate());
            faceMatchLog.setUniqueId(faceVerify.getUniqueId());
            faceMatchLog.setDeptId(deptId);
            faceMatchLog.setDeptName(deptName);
            faceMatchLog.setPersonName(personName);
            faceMatchLog.setReceivedTime(new Date(receivedTime));
            faceMatchLog.setTimeUsed(responseTime - receivedTime);
            faceMatchLog.setOnlineChipResult(verifyVO.getOnlineChipResult());
            faceMatchLog.setOnlineChipScore(StringUtils.isBlank(verifyVO.getOnlineChipScore()) ? null
                : Double.valueOf(verifyVO.getOnlineChipScore()));
            faceMatchLog.setSceneChipResult(verifyVO.getSceneChipResult());
            faceMatchLog.setSceneChipScore(StringUtils.isBlank(verifyVO.getSceneChipScore()) ? null
                : Double.valueOf(verifyVO.getSceneChipScore()));
            faceMatchLog.setSceneOnlineResult(verifyVO.getSceneOnlineResult());
            faceMatchLog.setSceneOnlineScore(StringUtils.isBlank(verifyVO.getSceneOnlineScore()) ? null
                : Double.valueOf(verifyVO.getSceneOnlineScore()));
            faceMatchLog.setSceneStockResult(verifyVO.getSceneStockResult());
            faceMatchLog.setSceneStockScore(StringUtils.isBlank(verifyVO.getSceneStockScore()) ? null
                : Double.valueOf(verifyVO.getSceneStockScore()));
            faceMatchLog.setCheckliveScore(StringUtils.isBlank(verifyVO.getLiveDetectionScore()) ? null
                : Double.valueOf(verifyVO.getLiveDetectionScore()));
            faceMatchLog.setCheckliveResult(verifyVO.getLiveDetectionResult());
            String result = verifyVO.getResult();
            result = DictConstants.BioResult.NOTPASS.equals(faceVerify.getTemperatureResult())
                ? DictConstants.BioResult.NOTPASS : result;// 测温没通过认为失败
            // 测温温度信息
            faceMatchLog.setTemperature(
                StringUtils.isBlank(faceVerify.getTemperature()) ? null : Double.valueOf(faceVerify.getTemperature()));
            faceMatchLog.setTemperatureFloor(StringUtils.isBlank(faceVerify.getTemperatureFloor()) ? null
                : Double.valueOf(faceVerify.getTemperatureFloor()));
            faceMatchLog.setTemperatureTop(StringUtils.isBlank(faceVerify.getTemperatureTop()) ? null
                : Double.valueOf(faceVerify.getTemperatureTop()));
            faceMatchLog.setResult(result);
            faceMatchLog.setVendorCode("eyecool");
            if (CollectionUtils.isNotEmpty(matchBeanList)) {
                faceMatchLog.setServerId(matchBeanList.get(0).getServerId());
                faceMatchLog.setAlgsVersion(matchBeanList.get(0).getAlgVersion());
            }
            faceMatchLogMapper.insertPersonFaceMatchLog(faceMatchLog);
        });
    }

    /**
     * 获取人脸1:1比对图片存放文件夹
     *
     * @return
     */
    private String getSysFaceOne2OnePicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_COMPARE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.face.service.image.match.folder.need",
                SysConfigConstants.BUSI_FACE_COMPARE_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 人脸识别(1:N识别)
     */
    @Override
    public List<PersonFaceRecogVO> recogPersonFace(PersonFaceRecog faceRecog) {
        // 查询平台是否开启了1-N功能
        String config = configService.selectConfigByKey(SysConfigConstants.PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY);
        if (DictConstants.YesOrNoState.NO.equals(config)) {
            throw new CustomException(MessageUtils.message("channel.face.service.1n.search.not.opened"));
        }
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在并判断场景是否开通人脸
        String channelCode = faceRecog.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
        }
        if (null != channelInfo) {
            String faceMode = channelInfo.getFaceMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(faceMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.common.service.scene.face.not.open", channelCode));
            }
        }

        // 现场照图片
        String sceneImage = faceRecog.getSceneImage();
        // 如果是传输视频，则从视频提取照片
        boolean isVideo = Constants.STATUS_TWO.equals(faceRecog.getSceneMediaType());
        String videoBase64 = null;
        if (isVideo) {
            try {
                videoBase64 = new String(Base64.getEncoder().encode(faceRecog.getVideo().getBytes()));
            } catch (IOException e) {
                log.error("Getting video base64 error", e);
                throw new CustomException(MessageUtils.message("channel.face.service.video.base64.failed"));
            }
        }
        // 异步检活逻辑处理
        String liveDetectionThresholdStr = faceRecog.getLiveDetectionThreshold();
        Double liveDetectionThreshold =
            StringUtils.isBlank(liveDetectionThresholdStr) ? null : Double.valueOf(liveDetectionThresholdStr);
        // 检活结果
        Boolean checkLiveResult = null;
        CheckLiveResponse checkLiveResponse = null;
        CompletableFuture<CheckLiveResponse> checkLiveCompletableFuture = null;
        // 如果是视频，则直接检活(需要获取现场照图片才能继续进行,所以不能异步)；如果是图片，执行异步检活(提高效率)
        if (isVideo) {
            checkLiveResponse = handleCheckLive(faceRecog.getLiveDetection(), liveDetectionThreshold,
                null == channelInfo ? null : channelInfo.getId(), faceRecog.getSceneImage(), true, isVideo,
                videoBase64);
            checkLiveResult = checkLiveResponse.getResult();
            // 设置现场照faceRecog.setSceneImage
            faceRecog.setSceneImage(checkLiveResponse.getImage());
            sceneImage = faceRecog.getSceneImage();
        } else {
            final ChannelInfo tmpChannelInfo = channelInfo;
            String tenantId = TenantContextHolder.getTenantId();
            checkLiveCompletableFuture = CompletableFuture.supplyAsync(() -> {
                TenantContextHolder.setTenantId(tenantId);
                return handleCheckLive(faceRecog.getLiveDetection(), liveDetectionThreshold,
                    null == tmpChannelInfo ? null : tmpChannelInfo.getId(), faceRecog.getSceneImage(), true, isVideo,
                    null);
            });
        }

        // 获取现场照特征
        FeatureBean featureBean = faceRecogLogicService.getFeatureBean(sceneImage);
        String tmpChannelCode = StringUtils.EMPTY;// 1-N识别方式, 初始化为空串
        String searchN = DictConstants.SearchNType.SEARCH_N_TYPE_ALL;
        if (null != channelInfo) {
            searchN = channelInfo.getSearchN();
        }
        boolean isSeqQuery = false;// 1-N校验方式是否为依次校验
        List<String> tmpChannelCodeList = Lists.newArrayList();// 1-N校验方式为依次校验时，有序放入非空子场景编码、非空场景号和全库查询参数
        if (DictConstants.SearchNType.SEARCH_N_TYPE_SUB.equals(searchN)) {
            tmpChannelCode = faceRecog.getSubTreasury();
            log.info("Sub-scene search, search library number [sub-scene code]:{}", tmpChannelCode);
            // 场景设置的校验方式为查询子场景，但是没有传入子场景编码，不搜索直接报错返回
            if (StringUtils.isBlank(tmpChannelCode)) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scenesearch.subscene.scenecode.empty"));
            }
            ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
            condition.setChannelId(channelInfo.getId());
            condition.setSubTreasuryCode(tmpChannelCode);
            List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
            if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
                throw new CustomException(
                    MessageUtils.message("channel.face.service.scene.subscene.not.exists", tmpChannelCode));
            }
        } else if (DictConstants.SearchNType.SEARCH_N_TYPE_CHANNEL.equals(searchN)) {
            tmpChannelCode = faceRecog.getChannelCode();
            log.info("Scene library search, search library number [scene number]:{}", tmpChannelCode);
        } else if (DictConstants.SearchNType.SEARCH_N_TYPE_SEQ.equals(searchN)) { // 依次查询
            isSeqQuery = true;
            if (StringUtils.isNotBlank(faceRecog.getSubTreasury())) {
                tmpChannelCodeList.add(faceRecog.getSubTreasury());
            }
            if (StringUtils.isNotBlank(faceRecog.getChannelCode())) {
                tmpChannelCodeList.add(faceRecog.getChannelCode());
            }
            tmpChannelCodeList.add(StringUtils.EMPTY);
            log.info("Query search in turn, query parameters: {}", tmpChannelCodeList.toString());
        } else {
            log.info("Full library search, query parameters: {}", tmpChannelCode);
        }
        tmpChannelCode = tmpChannelCode == null ? StringUtils.EMPTY : tmpChannelCode;
        // 不传默认返回一条
        Integer topN = StringUtils.isBlank(faceRecog.getTopN()) ? ChannelParamConstants.FACE_SEARCH_TOP_N_VALUE
            : Integer.valueOf(faceRecog.getTopN());// 返回数据条数
        // 获取场景1-N搜索阈值参数
        String searchNThresholdStr = faceRecog.getSearchNThreshold();
        Double searchNThreahold = StringUtils.isBlank(searchNThresholdStr) ? null : Double.valueOf(searchNThresholdStr);
        if (null == searchNThreahold && null != channelInfo) { // 从场景参数获取1-N搜索阈值
            ChannelParam channelParam = getChannelParam(channelInfo.getId(), DictConstants.BioAttestType.FACE,
                ChannelParamConstants.FACE_SEARCH_N_THRESHOLD_CODE);
            searchNThreahold = null == channelParam ? null : Double.valueOf(channelParam.getParamValue());
        }
        // 执行1-N比对搜索
        List<FaceSearchResult> faceSearchNResults = null;
        if (isSeqQuery) {
            for (String tmpArg : tmpChannelCodeList) {
                faceSearchNResults =
                    faceRecogLogicService.faceSearchN(featureBean.getFeature(), tmpArg, topN, searchNThreahold);
                if (CollectionUtils.isNotEmpty(faceSearchNResults)) {
                    break;
                }
            }
        } else {
            faceSearchNResults =
                faceRecogLogicService.faceSearchN(featureBean.getFeature(), tmpChannelCode, topN, searchNThreahold);
        }

        // 处理比对结果
        List<PersonFaceRecogVO> recogVoList = handleFaceSearchNResult(faceSearchNResults, searchN,
            null == channelInfo ? null : channelInfo.getId(), faceRecog.getSubTreasury());
        // 处理异步检活结果(图像检活)
        if (!isVideo) {
            try {
                checkLiveResponse = checkLiveCompletableFuture.get();
                checkLiveResult = null == checkLiveResponse ? null : checkLiveResponse.getResult();
            } catch (InterruptedException e) {
                log.error("Error in obtaining the biopsy result", e);
                Thread.currentThread().interrupt();
                throw new CustomException(MessageUtils.message("channel.face.service.face.checklive.error"));
            } catch (ExecutionException e) {
                log.error("Error in obtaining the biopsy result", e);
                throw new CustomException(MessageUtils.message("channel.face.service.face.checklive.error"));
            }
        }
        // 异步保存搜索日志
        asyncSaveFaceSearchLog(faceRecog, checkLiveResponse, faceSearchNResults, recogVoList, receivedTime,
            videoBase64);
        if (checkLiveResult != null && !checkLiveResult) {
            throw new CustomException(MessageUtils.message("channel.face.service.checklive.failed"));
        }
        return recogVoList.stream().filter(it -> !it.getUniqueId().startsWith(DatamanagerConstants.STRANGER_ID_PREFIX))
            .collect(Collectors.toList());
    }

    /**
     * 处理人脸1-N比对搜索结果
     *
     * @param faceSearchNResults 1-N结果
     * @param searchN 1-N校验方式
     * @param channelId 场景Id
     * @return
     */
    private List<PersonFaceRecogVO> handleFaceSearchNResult(List<FaceSearchResult> faceSearchNResults, String searchN,
        String channelId, String subTreasuryCode) {
        if (CollectionUtils.isEmpty(faceSearchNResults)) {
            return Collections.emptyList();
        }
        List<PersonFaceRecogVO> personFaceRecogVOList = faceSearchNResults.stream().filter(it -> null != it).map(it -> {
            String featureId = it.getFeatureId();
            PersonBioRecogBaseVO recogBaseVO = handleSearchNSingleResult(it.getUserId(), searchN, channelId,
                it.getScore(), featureId.substring(featureId.indexOf("_") + 1), subTreasuryCode);
            if (null == recogBaseVO) {
                return null;
            }
            PersonFaceRecogVO vo = new PersonFaceRecogVO();
            BeanUtils.copyBeanProp(vo, recogBaseVO);
            return vo;
        }).filter(it -> null != it).collect(Collectors.toList());
        return personFaceRecogVOList;
    }

    /**
     * 处理1：N结果,添加场景信息
     *
     * @param uniqueId
     * @param searchN
     * @param channelId
     * @param score
     * @param faceId
     * @param subTreasuryCode
     * @return
     */
    private PersonBioRecogBaseVO handleSearchNSingleResult(String uniqueId, String searchN, String channelId,
        double score, String faceId, String subTreasuryCode) {
        PersonBioRecogBaseVO recogVO = new PersonBioRecogBaseVO();
        recogVO.setUniqueId(uniqueId);
        recogVO.setScore(String.valueOf(score));
        recogVO.setScenesInfo(Lists.newArrayList());
        recogVO.setSearchNType(searchN);
        // 查询basePersonInfo是否存在有效信息, 只有有效才返回(防止abis搜索结果有误)
        BasePersonInfo personInfoCondition = new BasePersonInfo();
        personInfoCondition.setUniqueId(uniqueId);
        personInfoCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personInfoList = basePersonInfoMapper.selectBasePersonInfoList(personInfoCondition);
        if (CollectionUtils.isEmpty(personInfoList)) {
            log.error(
                "【FOX_MINISEARCH】The face 1-N search result is wrong, and the person [uniqueId:{}] that does not exist (or the status is invalid) in the relation database is recognized!!!",
                uniqueId);
            return null;
        }
        BasePersonInfo personInfo = personInfoList.get(0);
        recogVO.setName(personInfo.getName());
        // 查询部门信息
        Long deptId = personInfo.getDeptId();
        if (null != deptId) {
            SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
            recogVO.setDeptName(null == sysDept ? null : sysDept.getDeptName());
        }
        // 查询人脸是否存在有效信息(防止abis搜索结果有误)
        BasePersonFace face = basePersonFaceMapper.selectBasePersonFaceById(faceId);
        if (null == face || DictConstants.Status.DISABLE.equals(face.getStatus())) {
            log.error(
                "【FOX_MINISEARCH】The face 1-N search result is wrong, and the person [uniqueId:{}] that does not exist (or the status is invalid) in the relation database !!!",
                uniqueId, faceId);
            return null;
        }
        if (DictConstants.SearchNType.SEARCH_N_TYPE_SUB.equals(searchN)) {// 查询人在子场景下是否存在(防止abis搜索结果有误)
            ChannelSubtreasuryBusi subCondition = new ChannelSubtreasuryBusi();
            subCondition.setChannelId(channelId);
            subCondition.setSubTreasuryCode(subTreasuryCode);
            subCondition.setPersonId(personInfo.getId());
            subCondition.setStatus(DictConstants.Status.ENABLE);
            List<ChannelSubtreasuryBusi> subtreasuryBusiList =
                channelSubtreasuryBusiMapper.selectChannelSubtreasuryBusiList(subCondition);
            if (CollectionUtils.isEmpty(subtreasuryBusiList)) {
                log.error(
                    "【FOX_MINISEARCH】Face 1-N search results are wrong, people who do not exist (or have invalid status) in the relation library (sub-scene)[subTreasuryCode:{},uniqueId:{}, faceId:{}] is recognized!!!",
                    subTreasuryCode, uniqueId, faceId);
                return null;
            }
        }
        // 查询场景(场景)业务信息
        ChannelBusiness busiCondition = new ChannelBusiness();
        if (!DictConstants.SearchNType.SEARCH_N_TYPE_ALL.equals(searchN)) {
            // 查询全库的话，就不传入场景ID
            busiCondition.setChannelId(channelId);
        }
        busiCondition.setPersonId(personInfo.getId());
        busiCondition.setStatus(DictConstants.Status.ENABLE);
        List<ChannelBusiness> channelBusinessList = channelBusinessMapper.selectChannelBusinessList(busiCondition);
        // 查询人在场景下是否存在(防止abis搜索结果有误)
        if (DictConstants.SearchNType.SEARCH_N_TYPE_CHANNEL.equals(searchN)
            && CollectionUtils.isEmpty(channelBusinessList)) {
            log.error(
                "【FOX_MINISEARCH】Face 1-N search results are wrong, people who do not exist (or have invalid status) in the relation library (scene library)[channelId:{},uniqueId:{}, faceId:{}] is recognized!!!",
                channelId, uniqueId, faceId);
            return null;
        }
        if (CollectionUtils.isNotEmpty(channelBusinessList)) {
            List<PersonBioRecogSceneVO> sceneInfos = channelBusinessList.stream().map(busi -> {
                // 查询场景信息
                ChannelInfo sceneChannel = channelInfoMapper.selectChannelInfoById(busi.getChannelId());
                PersonBioRecogSceneVO recogSceneVO = new PersonBioRecogSceneVO();
                recogSceneVO.setChannelCode(sceneChannel.getChannelCode());
                recogSceneVO.setBusiCodeFirst(StringUtils.trimToEmpty(busi.getBusiCodeFirst()));
                recogSceneVO.setBusiCodeSecond(StringUtils.trimToEmpty(busi.getBusiCodeSecond()));
                recogSceneVO.setBusiCodeThird(StringUtils.trimToEmpty(busi.getBusiCodeThird()));
                return recogSceneVO;
            }).filter(it -> null != it).collect(Collectors.toList());
            recogVO.setScenesInfo(sceneInfos);
        }
        return recogVO;
    }

    /**
     * 异步保存人脸搜索日志
     * 
     * @param faceRecog
     * @param checkLiveResponse
     * @param faceSearchNResults
     * @param recogVoList
     * @param receivedTime
     * @param videoBase64
     */
    private void asyncSaveFaceSearchLog(PersonFaceRecog faceRecog, CheckLiveResponse checkLiveResponse,
        List<FaceSearchResult> faceSearchNResults, List<PersonFaceRecogVO> recogVoList, long receivedTime,
        String videoBase64) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            PersonFaceRecogVO recogVO = CollectionUtils.isNotEmpty(recogVoList) ? recogVoList.get(0) : null;
            // 校验是否短时间内重复比对
            if (null != recogVO && StringUtils.isNotBlank(faceRecog.getDuplicateTime()) && validDuplicate(
                recogVO.getUniqueId(), Long.valueOf(faceRecog.getDuplicateTime()), faceRecog.getChannelCode())) {
                return;
            }
            // 进行比对图片的上传
            String baseDir = getSysFaceSearchPicBaseDir();
            PersonFaceSearchLog faceSearchLog = new PersonFaceSearchLog();
            String serverId =
                CollectionUtils.isNotEmpty(faceSearchNResults) ? faceSearchNResults.get(0).getServerId() : null;
            String algsVersion =
                CollectionUtils.isNotEmpty(faceSearchNResults) ? faceSearchNResults.get(0).getAlgVersion() : null;
            if (null != recogVO) {
                // 查询人员部门信息
                BasePersonInfo personInfo = checkBasePersonExists(recogVO.getUniqueId());
                faceSearchLog.setUniqueId(recogVO.getUniqueId());
                faceSearchLog.setPersonName(personInfo.getName());
                faceSearchLog.setDeptId(personInfo.getDeptId());
                if (null != personInfo.getDeptId()) {
                    SysDept sysDept = sysDeptMapper.selectDeptById(personInfo.getDeptId());
                    faceSearchLog.setDeptName(null == sysDept ? null : sysDept.getDeptName());
                }
                // 查询底库图片
                BasePersonFace basePersonFace = getBasePersonFace(personInfo.getId());
                if (null != basePersonFace && StringUtils.isNotBlank(basePersonFace.getImageUrl())) {
                    String stockImageBase64 = PlatformFileUtils.getImageBase64(basePersonFace.getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(basePersonFace.getEncrypted())) {
                        stockImageBase64 = PlatformCryptUtils.decryptImageBase64(stockImageBase64);
                    }
                    // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
                    if (StringUtils.isNotBlank(stockImageBase64)) {
                        faceSearchLog
                            .setStockImage(faceRecogLogicService.uploadFaceImg(true, null, stockImageBase64, baseDir));
                    }
                }
            }
            // 加密上传现场照
            if (StringUtils.isNotBlank(faceRecog.getSceneImage())) {
                faceSearchLog
                    .setSceneImage(faceRecogLogicService.uploadFaceImg(true, null, faceRecog.getSceneImage(), baseDir));
            }
            if (null != videoBase64) {
                String videoOriginalName = faceRecog.getVideo().getOriginalFilename();
                faceSearchLog
                    .setSceneVideo(faceRecogLogicService.uploadFaceImg(true, videoOriginalName, videoBase64, baseDir));
            }
            Boolean checkLiveResult = null == checkLiveResponse ? null : checkLiveResponse.getResult();
            Double checkLiveScore = null == checkLiveResponse ? null : checkLiveResponse.getScore();
            String checkLiveResultStr = checkLiveResult == null ? null
                : (checkLiveResult ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
            String result = (checkLiveResult == null || checkLiveResult) && null != recogVO
                ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
            result = DictConstants.BioResult.NOTPASS.equals(faceRecog.getTemperatureResult())
                ? DictConstants.BioResult.NOTPASS : result;// 测温没通过认为失败
            faceSearchLog.setId(IdWorker.getNextStringId());
            faceSearchLog.setChannelCode(faceRecog.getChannelCode());
            faceSearchLog.setReceivedSeq(faceRecog.getReceivedSeq());
            faceSearchLog.setCreateTime(DateUtils.getNowDate());
            faceSearchLog.setReceivedTime(new Date(receivedTime));
            faceSearchLog.setTimeUsed(responseTime - receivedTime);
            faceSearchLog.setServerId(serverId);
            faceSearchLog.setVendorCode("eyecool");
            faceSearchLog.setAlgsVersion(algsVersion);
            faceSearchLog.setSceneStockScore(null == recogVO ? null : Double.valueOf(recogVO.getScore()));
            faceSearchLog.setSubTreasuryCode(faceRecog.getSubTreasury());
            // 查询子场景名
            if (StringUtils.isNotBlank(faceRecog.getSubTreasury())) {
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.setChannelCode(faceRecog.getChannelCode());
                List<ChannelInfo> channelInfoList = channelInfoMapper.selectChannelInfoList(channelInfo);
                ChannelSubtreasuryInfo condition = new ChannelSubtreasuryInfo();
                condition.setChannelId(channelInfoList.get(0).getId());
                condition.setSubTreasuryCode(faceRecog.getSubTreasury());
                List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                    channelSubtreasuryInfoMapper.selectChannelSubtreasuryInfoList(condition);
                if (CollectionUtils.isNotEmpty(subtreasuryInfoList)) {
                    faceSearchLog.setSubTreasuryName(subtreasuryInfoList.get(0).getSubTreasuryName());
                }
            }
            // 测温温度信息
            faceSearchLog.setTemperature(
                StringUtils.isBlank(faceRecog.getTemperature()) ? null : Double.valueOf(faceRecog.getTemperature()));
            faceSearchLog.setTemperatureFloor(StringUtils.isBlank(faceRecog.getTemperatureFloor()) ? null
                : Double.valueOf(faceRecog.getTemperatureFloor()));
            faceSearchLog.setTemperatureTop(StringUtils.isBlank(faceRecog.getTemperatureTop()) ? null
                : Double.valueOf(faceRecog.getTemperatureTop()));
            faceSearchLog.setResult(result);
            faceSearchLog.setCheckliveScore(checkLiveScore);
            faceSearchLog.setCheckliveResult(checkLiveResultStr);
            faceSearchLog.setSceneType(DictConstants.SearchNLogSceneType.SEARCH_N_HTTP);
            faceSearchLog.setDeviceCode(faceRecog.getDeviceCode());
            faceSearchLog.setDeviceModel(faceRecog.getDeviceModel());
            faceSearchLog.setDeviceName(faceRecog.getDevicName());
            faceSearchLog.setDeviceIp(faceRecog.getDeviceIp());
            faceSearchLog.setDeviceLongitude(StringUtils.isBlank(faceRecog.getDeviceLongitude()) ? null
                : Double.valueOf(faceRecog.getDeviceLongitude()));
            faceSearchLog.setDeviceDimension(StringUtils.isBlank(faceRecog.getDeviceDimension()) ? null
                : Double.valueOf(faceRecog.getDeviceDimension()));
            faceSearchLog.setDeviceDirection(faceRecog.getDeviceDirection());
            faceSearchLogMapper.insertPersonFaceSearchLog(faceSearchLog);
        });
    }

    /**
     * 获取人脸1:N搜索图片存放文件夹
     *
     * @return
     */
    private String getSysFaceSearchPicBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_SEARCH_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.face.service.image.search.folder.need"));
        }
        return baseDir;
    }

    /**
     * 校验1:N比对是否在某时间段重复比对 重复返回true，不重复返回false
     * 
     * @param uniqueId
     * @param duplicateTime
     * @param channelCode
     * @return
     */
    private boolean validDuplicate(String uniqueId, Long duplicateTime, String channelCode) {
        String needDistinct =
            configService.selectConfigByKey(SysConfigConstants.BUSI_FACE_SEARCH_LOG_SAVE_DISTINCT_KEY);
        // 不配置参数或者配置为不做去重处理都认为是不重复直接返回
        if (StringUtils.isBlank(needDistinct) || DictConstants.YesOrNoState.NO.equals(needDistinct)) {
            return false;
        }
        String tenantId = TenantContextHolder.getTenantId();
        tenantId = StringUtils.isBlank(tenantId) ? StringUtils.EMPTY : tenantId;
        String cacheKey = "recogRecord:" + tenantId + channelCode + uniqueId;
        Long recogTime = redisCache.getCacheObject(cacheKey);
        long currTime = System.currentTimeMillis();
        if (currTime - recogTime > duplicateTime) {
            redisCache.setCacheObject(cacheKey, currTime);
            return false;
        }
        return true;
    }

    /**
     * 联网核查
     * 
     * @param personIdVerification
     * @return
     */
    @Override
    public Map<String, Object> personIdentityVerification(PersonIdentityVerification personIdVerification) {
        String serviceAddress = configService.selectConfigByKey(SysConfigConstants.IDENTITY_VERIFICATION_URL_KEY);
        if (StringUtils.isBlank(serviceAddress)) {
            throw new CustomException(
                MessageUtils.message("channel.face.service.onlineverify.interface.address.not.configure",
                    SysConfigConstants.IDENTITY_VERIFICATION_URL_KEY));
        }
        String authCode = configService.selectConfigByKey(SysConfigConstants.IDENTITY_VERIFICATION_AUTHCODE_KEY);
        if (StringUtils.isBlank(authCode)) {
            throw new CustomException(
                MessageUtils.message("channel.face.service.onlineverify.interface.authcode.not.configure",
                    SysConfigConstants.IDENTITY_VERIFICATION_AUTHCODE_KEY));
        }
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();

        params.put("idNum", personIdVerification.getUniqueId());
        params.put("idName", personIdVerification.getName());
        params.put("authCode", authCode);
        params.put("liveImage", personIdVerification.getSceneImageBase64());
        params.put("serialNumber", String.valueOf(personIdVerification.getReceivedSeq()));
        header.put("Content-Type", "application/x-www-form-urlencoded");

        String rst = HttpClientUtil.doPost(serviceAddress, params, header);
        log.info("Personnel verification results:" + rst);
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isEmpty(rst)) {
            CompletableFuture.runAsync(() -> {
                TenantContextHolder.setTenantId(tenantId);
                // 保存联网核查日志
                saveIdentityVerifyLog(null, personIdVerification);
            });
            throw new CustomException(MessageUtils.message("channel.face.service.onlineverify.response.empty"));
        }
        JSONObject resObj = JSONObject.parseObject(rst);
        if ("000000".equals(resObj.getString("code"))) {
            CompletableFuture.runAsync(() -> {
                TenantContextHolder.setTenantId(tenantId);
                // 保存联网核查日志
                saveIdentityVerifyLog(resObj, personIdVerification);
            });
            // 返回是否同一个人
            int similar = resObj.getIntValue("similar");
            boolean verifyResult = resObj.getBooleanValue("verifyResult");
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("similar", similar);
            resultMap.put("verifyResult", verifyResult);
            return resultMap;
        }
        String msg = resObj.getString("message");
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            // 保存联网核查日志
            saveIdentityVerifyLog(resObj, personIdVerification);
        });
        throw new CustomException(MessageUtils.message("channel.face.service.onlineverify.error", msg));
    }

    /**
     * 保存联网核查日志
     * 
     * @param resObj
     * @param personIdVerification
     */
    private void saveIdentityVerifyLog(JSONObject resObj, PersonIdentityVerification personIdVerification) {
        String sceneImageBase64 = personIdVerification.getSceneImageBase64();
        // 进行比对图片的上传
        String baseDir = getSysFaceOne2OnePicBaseDir();
        // 加密上传现场照
        String imgPath = faceRecogLogicService.uploadFaceImg(true, null, sceneImageBase64, baseDir);
        // 比对成功入库
        PersonFaceMatchLog faceMatchLog = new PersonFaceMatchLog();
        faceMatchLog.setChannelCode(personIdVerification.getChannelCode());
        faceMatchLog.setDeviceCode(personIdVerification.getDeviceCode());
        if (StringUtils.isNotBlank(personIdVerification.getDeviceDimension())) {
            faceMatchLog.setDeviceDimension(Double.parseDouble(personIdVerification.getDeviceDimension()));
        }
        if (StringUtils.isNotBlank(personIdVerification.getDeviceLongitude())) {
            faceMatchLog.setDeviceLongitude(Double.parseDouble(personIdVerification.getDeviceLongitude()));
        }
        faceMatchLog.setDeviceIp(personIdVerification.getDeviceIp());
        faceMatchLog.setDeviceModel(personIdVerification.getDeviceModel());
        faceMatchLog.setDeviceName(personIdVerification.getDevicName());
        faceMatchLog.setDeviceDirection(personIdVerification.getDeviceDirection());
        faceMatchLog.setId(IdWorker.getNextStringId());
        faceMatchLog.setReceivedSeq(personIdVerification.getReceivedSeq());
        faceMatchLog.setReceivedTime(DateUtils.getNowDate());
        faceMatchLog.setCreateTime(faceMatchLog.getReceivedTime());
        faceMatchLog.setUniqueId(personIdVerification.getUniqueId());
        faceMatchLog.setSceneImage(imgPath);
        // 温度信息
        if (StringUtils.isNotBlank(personIdVerification.getTemperature())) {
            faceMatchLog.setTemperature(Double.parseDouble(personIdVerification.getTemperature()));
        }
        if (StringUtils.isNotBlank(personIdVerification.getTemperatureFloor())) {
            faceMatchLog.setTemperatureFloor(Double.parseDouble(personIdVerification.getTemperatureFloor()));
        }
        if (StringUtils.isNotBlank(personIdVerification.getTemperatureTop())) {
            faceMatchLog.setTemperatureTop(Double.parseDouble(personIdVerification.getTemperatureTop()));
        }
        String result = DictConstants.BioResult.NOTPASS.equals(personIdVerification.getTemperatureResult())
            ? DictConstants.BioResult.NOTPASS : DictConstants.BioResult.PASS;// 测温没通过认为失败
        if (null == resObj) {
            faceMatchLog.setResult(result);
            faceMatchLogMapper.insertPersonFaceMatchLog(faceMatchLog);
            return;
        }
        // similar 比对分数
        int similar = resObj.getIntValue("similar");
        faceMatchLog.setSceneOnlineScore(Double.valueOf(similar));
        if (!"000000".equals(resObj.getString("code"))) {
            faceMatchLog.setResult(DictConstants.BioResult.NOTPASS);
            faceMatchLog.setSceneOnlineResult(DictConstants.BioResult.NOTPASS);
            faceMatchLog.setRemark(resObj.getString("message"));
            faceMatchLogMapper.insertPersonFaceMatchLog(faceMatchLog);
            return;
        }
        // 判断是否同一个人
        boolean verifyResult = resObj.getBooleanValue("verifyResult");
        if (verifyResult) {
            faceMatchLog.setResult(result);
            faceMatchLog.setSceneOnlineResult(DictConstants.BioResult.PASS);
        } else {
            faceMatchLog.setResult(DictConstants.BioResult.NOTPASS);
            faceMatchLog.setSceneOnlineResult(DictConstants.BioResult.NOTPASS);
        }
        faceMatchLogMapper.insertPersonFaceMatchLog(faceMatchLog);
    }

    /**
     * 比对两张图片
     */
    @Override
    public Map<String, Object> compareTwoImage(String imageBase64_1, String imageBase64_2, Double threshold,
        String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        FeatureBean featureBean1 = faceRecogLogicService.getFeatureBean(imageBase64_1);
        FeatureBean featureBean2 = faceRecogLogicService.getFeatureBean(imageBase64_2);
        List<MatchBean> matchBeanList =
            faceRecogLogicService.faceOne2OneCompare(Arrays.asList(featureBean1, featureBean2));
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (null == threshold) {
            threshold = faceRecogLogicService.getOne2OneCompareThreshold();
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("score", score);
        map.put("threshold", threshold);
        map.put("result", score > threshold);
        return map;
    }

    /**
     * 获取人脸特征
     */
    @Override
    public List<FeatureBean> getPersonFaceFeature(String sceneImage, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        // 提取现场照人脸特征
        return faceRecogLogicService.getMultiPersonFaceFeature(sceneImage);
    }

    /**
     * 人脸图片检活
     */
    @Override
    public Map<String, Object> checklivePersonFaceImage(String sceneImage, Double threshold, String channelCode) {
        long startTime = System.currentTimeMillis();
        Date receivedTime = DateUtils.getNowDate();
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        double checkliveThreshold =
            StringUtils.isNull(threshold) ? faceRecogLogicService.getCheckLiveThreshold(false) : threshold;
        CheckLiveResponse response = faceRecogLogicService.checkLive(sceneImage, threshold);
        Map<String, Object> map = Maps.newHashMap();
        map.put("result", response.getResult());
        map.put("score", response.getScore());
        map.put("message", response.getMessage());
        map.put("threshold", checkliveThreshold);
        long endTime = System.currentTimeMillis();
        saveCheckliveLog(receivedTime, sceneImage, null, checkliveThreshold, channelCode, response,
            endTime - startTime);
        return map;
    }

    /**
     * 保存检活日志
     * 
     * @param receivedTime
     * @param sceneImageBase64
     * @param sceneVideoBase64
     * @param threshold
     * @param channelCode
     * @param response
     * @param timeUsed
     */
    private void saveCheckliveLog(Date receivedTime, String sceneImageBase64, String sceneVideoBase64, double threshold,
        String channelCode, CheckLiveResponse response, long timeUsed) {
        CompletableFuture.runAsync(() -> {
            PersonFaceCheckliveLog log = new PersonFaceCheckliveLog();
            log.setId(IdWorker.getNextStringId());
            log.setChannelCode(channelCode);
            log.setCheckliveMsg(response.getMessage());
            log.setCheckliveResult(
                response.getResult() ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS);
            log.setCheckliveScore(response.getScore());
            log.setReceivedTime(receivedTime);
            log.setCreateTime(DateUtils.getNowDate());
            String baseDir = getCheckliveImageBaseDir();
            if (StringUtils.isNotBlank(sceneVideoBase64)) {
                String videoPath = faceRecogLogicService.uploadFaceImg(true, null, sceneVideoBase64, baseDir);
                log.setSceneVideo(videoPath);
            }
            String sceneImg = StringUtils.isBlank(sceneImageBase64) ? response.getImage() : sceneImageBase64;
            String faceImgPath = faceRecogLogicService.uploadFaceImg(true, null, sceneImg, baseDir);
            log.setSceneImage(faceImgPath);
            log.setThreshold(threshold);
            log.setTimeUsed(timeUsed);
            faceCheckliveLogMapper.insertPersonFaceCheckliveLog(log);
        });
    }

    /**
     * 查询检活日志图片存储基础目录
     * 
     * @return
     */
    private String getCheckliveImageBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_CHECKLIVE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            log.error("Platform [parameter settings] face detection live image storage folder [{}] is not configured",
                SysConfigConstants.BUSI_CHECKLIVE_DIR_KEY);
            throw new CustomException(MessageUtils.message("channel.face.service.image.checklive.folder.need",
                SysConfigConstants.BUSI_CHECKLIVE_DIR_KEY));
        }
        return baseDir;
    }

    /**
     * 人脸视频检活
     */
    @Override
    public Map<String, Object> checklivePersonFaceVideo(String sceneVideo, Double threshold, String channelCode) {
        long startTime = System.currentTimeMillis();
        Date receivedTime = DateUtils.getNowDate();
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        double checkliveThreshold =
            StringUtils.isNull(threshold) ? faceRecogLogicService.getCheckLiveThreshold(true) : threshold;
        CheckLiveResponse response = faceRecogLogicService.videoCheckLive(sceneVideo, checkliveThreshold);
        Map<String, Object> map = Maps.newHashMap();
        map.put("image", response.getImage());
        map.put("result", response.getResult());
        map.put("score", response.getScore());
        map.put("message", response.getMessage());
        map.put("threshold", checkliveThreshold);
        long endTime = System.currentTimeMillis();
        saveCheckliveLog(receivedTime, null, sceneVideo, checkliveThreshold, channelCode, response,
            endTime - startTime);
        return map;
    }

    /**
     * 人脸图片质量检测
     */
    @Override
    public Map<String, Object> personFaceQualityDetect(String sceneImage, Double threshold, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        double score = faceRecogLogicService.qualityDetect(sceneImage, -1D, null);
        if (null == threshold) {
            threshold = faceRecogLogicService.getDetectThreshold();
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("score", score);
        map.put("threshold", threshold);
        map.put("result", score > threshold);
        return map;
    }

    /**
     * 人脸视频检活和比对
     */
    @Override
    public Map<String, Object> checkliveFaceVideoAndCompare(String sceneVideo, String sceneImage,
        Double checkliveThreshold, String channelCode) {
        // 校验场景是否存在
        if (StringUtils.isNotBlank(channelCode)) {
            checkChannelExists(channelCode);
        }
        Map<String, Object> map = Maps.newHashMap();
        double threshold = StringUtils.isNull(checkliveThreshold) ? faceRecogLogicService.getCheckLiveThreshold(true)
            : checkliveThreshold;
        CheckLiveResponse response = faceRecogLogicService.videoCheckLive(sceneVideo, threshold);
        boolean checkliveResult = response.getResult();
        String optimalImage = response.getImage();
        map.put("optimalImage", optimalImage);
        map.put("checklivResult", checkliveResult);
        map.put("checkliveScore", response.getScore());
        map.put("checkliveMessage", response.getMessage());
        map.put("checkliveThreshold", threshold);
        if (!checkliveResult) {
            return map;
        }
        FeatureBean featureBean1 = faceRecogLogicService.getFeatureBean(sceneImage);
        FeatureBean featureBean2 = faceRecogLogicService.getFeatureBean(optimalImage);
        List<MatchBean> matchBeanList =
            faceRecogLogicService.faceOne2OneCompare(Arrays.asList(featureBean1, featureBean2));
        double compareScore = matchBeanList.get(0).getResults().get(0).getScore();
        map.put("compareScore", compareScore);
        return map;
    }

    /**
     * 校验场景是否存在
     *
     * @param channelCode
     * @return
     */
    private ChannelInfo checkChannelExists(String channelCode) {
        ChannelInfo infoCondition = new ChannelInfo();
        infoCondition.setChannelCode(channelCode);
        List<ChannelInfo> infoList = channelInfoMapper.selectChannelInfoList(infoCondition);
        if (CollectionUtils.isEmpty(infoList)) {
            throw new CustomException(
                MessageUtils.message("channel.common.service.channelcode.not.exists", channelCode));
        }
        ChannelInfo channelInfo = infoList.get(0);
        return channelInfo;
    }

}
