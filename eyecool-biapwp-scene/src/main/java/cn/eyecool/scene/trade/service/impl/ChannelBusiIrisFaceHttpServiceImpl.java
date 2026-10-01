package cn.eyecool.scene.trade.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.MultiMatchResult;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisFaceMapper;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.DictConstants.MuliIrisFaceVerifyType;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.match.MultiFusionFeatureService;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.constant.ChannelParamConstants;
import cn.eyecool.scene.domain.ChannelBusiness;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelParam;
import cn.eyecool.scene.mapper.ChannelBusinessMapper;
import cn.eyecool.scene.mapper.ChannelInfoMapper;
import cn.eyecool.scene.mapper.ChannelParamMapper;
import cn.eyecool.scene.trade.entity.PersonIrisFaceVerify;
import cn.eyecool.scene.trade.service.IChannelBusiIrisFaceHttpService;
import cn.eyecool.scene.trade.vo.PersonIrisFaceVerifyVO;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceirisMatchLog;
import cn.eyecool.tradelog.mapper.PersonFaceirisMatchLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 场景虹膜人脸多模态业务HTTP服务层实现
 *
 * @author admin
 * @date 2019年11月28日
 */
@Service
@Slf4j
public class ChannelBusiIrisFaceHttpServiceImpl implements IChannelBusiIrisFaceHttpService {

    @Autowired
    private ChannelInfoMapper channelInfoMapper;
    @Autowired
    private ChannelParamMapper channelParamMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private BasePersonIrisFaceMapper basePersonIrisFaceMapper;
    @Autowired
    private ChannelBusinessMapper channelBusinessMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IPersonFaceRecogLogicService faceRecogLogicService;
    @Autowired
    private IPersonIrisRecogLogicService irisRecogLogicService;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private IMultiFeatureService multiFeatureService;
    @Autowired
    private MultiFusionFeatureService fusionFeatureService;
    @Autowired
    private PersonFaceirisMatchLogMapper personFaceirisMatchLogMapper;

    /**
     * 虹膜人脸多模态1:1认证
     */
    @Override
    public PersonIrisFaceVerifyVO verifyPersonIrisFace(PersonIrisFaceVerify irisFaceVerify) {
        long receivedTime = System.currentTimeMillis();
        // 校验场景是否存在
        String channelCode = irisFaceVerify.getChannelCode();
        ChannelInfo channelInfo = null;
        if (StringUtils.isNotBlank(channelCode)) {
            channelInfo = checkChannelExists(channelCode);
            // 判断场景是否开通人脸虹膜多模态
            String faceIrisMode = channelInfo.getFaceIrisMode();
            if (DictConstants.BioModeStatus.DISABLE.equals(faceIrisMode)) {
                throw new CustomException(
                    MessageUtils.message("channel.busi.iris.service.scene.not.open.iris", channelCode));
            }
        }
        // 校验人员信息是否存在
        String uniqueId = irisFaceVerify.getUniqueId();
        BasePersonInfo basePersonInfo = checkBasePersonExists(uniqueId);
        String personId = basePersonInfo.getId();
        Long deptId = basePersonInfo.getDeptId();
        String personName = basePersonInfo.getName();
        // 校验场景人员人员合法性
        if (null != channelInfo) {
            checkChannelBusiValid(basePersonInfo, channelInfo);
        }
        String deptName = null;
        if (null != deptId) {
            SysDept sysDept = sysDeptMapper.selectDeptById(deptId);
            deptName = null == sysDept ? null : sysDept.getDeptName();
        }

        BasePersonIrisFace basePersonIrisFace = getBasePersonIrisFace(personId);
        if (null == basePersonIrisFace) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.match.feature.not.exists"));
        }
        // 现场照图片或者特征
        String faceBase64Img = irisFaceVerify.getFaceBase64Img();
        String faceFeatureBase64 = irisFaceVerify.getFaceFeatureBase64();
        String irisBase64Img = irisFaceVerify.getIrisBase64Img();
        String irisFeatureBase64 = irisFaceVerify.getIrisFeatureBase64();
        // 底库模板特征
        String stockFaceFeature = basePersonIrisFace.getFaceFeature();
        String stockIrisFeature = basePersonIrisFace.getIrisFeature();
        String stockFusionFeature = basePersonIrisFace.getFusionFeature();

        String verifyType = irisFaceVerify.getVerifyType();
        Double liveDetectionThreshold = null;
        Double irisThreshold = getMultiIrisCompareThreshold();
        Double faceThreshold = getMultiFaceCompareThreshold();
        Double fusionThreshold = getMultiFusionCompareThreshold();
        Double faceScore = null;
        Double irisScore = null;
        Double fusionScore = null;
        Double liveDetectionScore = null;
        MultiMatchResult handleMatchFace = null;
        MultiMatchResult handleMatchIris = null;
        String liveDetectionResult = StringUtils.EMPTY;
        boolean bioResult = false;
        // 现场照检活
        if (StringUtils.isNotBlank(faceBase64Img)
            && (DictConstants.YesOrNoState.YES.equals(irisFaceVerify.getLiveDetection()))) {
            String checkliveThreshold = irisFaceVerify.getLiveDetectionThreshold();
            if (StringUtils.isBlank(checkliveThreshold) && null != channelInfo) {
                // 如果没传阈值，则进入场景参数查询阈值
                ChannelParam channelParam = getChannelParam(channelInfo.getId(), DictConstants.BioAttestType.FACE,
                    ChannelParamConstants.FACE_COMPARE_CHECK_LIVE_THRESHOLD_CODE);
                checkliveThreshold = null == channelParam ? null : channelParam.getParamValue();
            }
            if (StringUtils.isBlank(checkliveThreshold)) {
                throw new CustomException(
                    MessageUtils.message("channel.busi.iris.service.match.threshold.not.configure"));
            }
            liveDetectionThreshold = Double.valueOf(checkliveThreshold);
            CheckLiveResponse checkLiveResponse =
                faceRecogLogicService.checkLive(faceBase64Img, liveDetectionThreshold);
            liveDetectionResult =
                checkLiveResponse.getResult() ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
            liveDetectionScore = checkLiveResponse.getScore();
        }
        switch (verifyType) {
            case MuliIrisFaceVerifyType.VERIFY_FACE:
                handleMatchFace = handleSingleMatchFeature(stockFaceFeature, faceBase64Img, faceFeatureBase64,
                    MicroConstants.AlgType.FACE);
                faceScore = handleMatchFace.getScore();
                bioResult = faceScore > faceThreshold;
                break;
            case MuliIrisFaceVerifyType.VERIFY_IRIS:
                handleMatchIris = handleSingleMatchFeature(stockIrisFeature, irisBase64Img, irisFeatureBase64,
                    MicroConstants.AlgType.IRIS);
                irisScore = handleMatchIris.getScore();
                bioResult = irisScore > irisThreshold;
                break;
            case MuliIrisFaceVerifyType.VERIFY_IRIS_AND_FACE:
                handleMatchFace = handleSingleMatchFeature(stockFaceFeature, faceBase64Img, faceFeatureBase64,
                    MicroConstants.AlgType.FACE);
                handleMatchIris = handleSingleMatchFeature(stockIrisFeature, irisBase64Img, irisFeatureBase64,
                    MicroConstants.AlgType.IRIS);
                faceScore = handleMatchFace.getScore();
                irisScore = handleMatchIris.getScore();
                bioResult = irisScore > irisThreshold && faceScore > faceThreshold;
                break;
            case MuliIrisFaceVerifyType.VERIFY_IRIS_OR_FACE:
                handleMatchFace = handleSingleMatchFeature(stockFaceFeature, faceBase64Img, faceFeatureBase64,
                    MicroConstants.AlgType.FACE);
                handleMatchIris = handleSingleMatchFeature(stockIrisFeature, irisBase64Img, irisFeatureBase64,
                    MicroConstants.AlgType.IRIS);
                faceScore = handleMatchFace.getScore();
                irisScore = handleMatchIris.getScore();
                bioResult = irisScore > irisThreshold || faceScore > faceThreshold;
                break;
            case MuliIrisFaceVerifyType.VERIFY_IRIS_FACE_MULTI:
                fusionScore = handleMatchFusionFeature(faceBase64Img, faceFeatureBase64, irisBase64Img,
                    irisFeatureBase64, stockFusionFeature);
                bioResult = fusionScore > fusionThreshold;
                break;
            default:
                break;
        }
        String result = bioResult
            && (DictConstants.BioResult.PASS.equals(liveDetectionResult) || StringUtils.isBlank(liveDetectionResult))
                ? DictConstants.BioResult.PASS : DictConstants.BioResult.NOTPASS;
        PersonIrisFaceVerifyVO verifyVO = new PersonIrisFaceVerifyVO();
        verifyVO.setLiveDetectionResult(liveDetectionResult);
        verifyVO.setLiveDetectionScore(liveDetectionScore);
        verifyVO.setLiveDetectionThreshold(liveDetectionThreshold);
        verifyVO.setFusionCompareThreshold(fusionThreshold);
        verifyVO.setFaceCompareThreshold(faceThreshold);
        verifyVO.setIrisCompareThreshold(irisThreshold);
        verifyVO.setFusionScore(fusionScore);
        verifyVO.setFaceScore(faceScore);
        verifyVO.setIrisScore(irisScore);
        verifyVO.setResult(result);
        // 异步保存比对日志
        asyncSaveMultiMatchLog(irisFaceVerify, verifyVO, basePersonIrisFace, receivedTime, deptId, deptName,
            personName);
        return verifyVO;
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
     * 处理人脸特征比对
     * 
     * @param stockFeature
     * @param base64Img
     * @param featureBase64
     * @param algType
     * @return
     */
    private MultiMatchResult handleSingleMatchFeature(String stockFeature, String base64Img, String featureBase64,
        MicroConstants.AlgType algType) {
        String bioDesc = MicroConstants.AlgType.FACE.equals(algType)
            ? MessageUtils.message("channel.common.service.scene.verify.face")
            : MessageUtils.message("channel.common.service.scene.verify.iris");
        if (StringUtils.isBlank(stockFeature)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.base.feature.not.exists", bioDesc));
        }
        String sceneFeature = featureBase64;
        if (StringUtils.isBlank(sceneFeature)) {
            try {
                sceneFeature = multiFeatureService.extractFeatureByImage(base64Img, algType);
            } catch (Exception e) {
                log.error("Multimodal{}Extract feature exception: [{}]", bioDesc, e.getMessage(), e);
                throw new CustomException(MessageUtils.message("channel.busi.iris.service.multimodal.feature.error",
                    bioDesc, e.getMessage()));
            }
        }
        try {
            MultiMatchResult matchResult =
                multiFeatureService.singleMatchByFeature(sceneFeature, stockFeature, algType);
            return matchResult;
        } catch (Exception e) {
            log.error("Multimodal{}Extract feature exception: [{}]", bioDesc, e.getMessage(), e);
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.feature.error", bioDesc, e.getMessage()));
        }
    }

    /**
     * 处理人脸虹膜多模态融合特征比对
     * 
     * @param faceBase64Img
     * @param faceFeatureBase64
     * @param irisBase64Img
     * @param irisFeatureBase64
     * @param stockFusionFeature
     * @return
     */
    private Double handleMatchFusionFeature(String faceBase64Img, String faceFeatureBase64, String irisBase64Img,
        String irisFeatureBase64, String stockFusionFeature) {
        if (StringUtils.isBlank(stockFusionFeature)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.base.feature.not.exists"));
        }
        String sceneFaceFeature = faceFeatureBase64;
        if (StringUtils.isBlank(sceneFaceFeature)) {
            try {
                sceneFaceFeature =
                    multiFeatureService.extractFeatureByImage(faceBase64Img, MicroConstants.AlgType.FACE);
            } catch (Exception e) {
                log.error("Abnormal multimodal face extraction feature: [{}]", e.getMessage(), e);
                throw new CustomException(
                    MessageUtils.message("channel.busi.iris.service.multimodal.face.feature.error", e.getMessage()));
            }
        }
        String sceneIrisFeature = irisFeatureBase64;
        if (StringUtils.isBlank(sceneIrisFeature)) {
            try {
                sceneIrisFeature =
                    multiFeatureService.extractFeatureByImage(irisBase64Img, MicroConstants.AlgType.IRIS);
            } catch (Exception e) {
                log.error("Multimodal iris extraction feature anomaly: [{}]", e.getMessage(), e);
                throw new CustomException(
                    MessageUtils.message("channel.busi.iris.service.multimodal.iris.feature.error", e.getMessage()));
            }
        }
        String fusionFeature = fusionFeatureService.fusionFeature(sceneFaceFeature, sceneIrisFeature);
        float score = fusionFeatureService.matchFusionFeatures(fusionFeature, stockFusionFeature);
        return Double.valueOf(String.valueOf(score));
    }

    /**
     * 获取多模态虹膜1:1比对阈值
     *
     * @return
     */
    private Double getMultiIrisCompareThreshold() {
        String threshold =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_MULTI_IRIS_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.iris.match.threshold.need",
                    SysConfigConstants.BASEDATA_MULTI_IRIS_COMPARE_THRESHOLD_KEY));
        }
        if (!DoubleValidator.getInstance().isValid(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.iris.match.threshold.format.error",
                    SysConfigConstants.BASEDATA_MULTI_IRIS_COMPARE_THRESHOLD_KEY));
        }
        return Double.valueOf(threshold);
    }

    /**
     * 获取多模态人脸1:1比对阈值
     *
     * @return
     */
    private Double getMultiFaceCompareThreshold() {
        String threshold =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_MULTI_FACE_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.face.match.threshold.need",
                    SysConfigConstants.BASEDATA_MULTI_FACE_COMPARE_THRESHOLD_KEY));
        }
        if (!DoubleValidator.getInstance().isValid(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.face.match.threshold.format.error",
                    SysConfigConstants.BASEDATA_MULTI_FACE_COMPARE_THRESHOLD_KEY));
        }
        return Double.valueOf(threshold);
    }

    /**
     * 获取多模态融合特征1:1比对阈值
     *
     * @return
     */
    private Double getMultiFusionCompareThreshold() {
        String threshold =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.irisface.match.threshold.need",
                    SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY));
        }
        if (!DoubleValidator.getInstance().isValid(threshold)) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.multimodal.irisface.match.threshold.format.error",
                    SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY));
        }
        return Double.valueOf(threshold);
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
     * 查询虹膜人脸多模态信息
     *
     * @param personId
     * @return
     */
    private BasePersonIrisFace getBasePersonIrisFace(String personId) {
        BasePersonIrisFace irisFaceCondition = new BasePersonIrisFace();
        irisFaceCondition.setPersonId(personId);
        irisFaceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList =
            basePersonIrisFaceMapper.selectBasePersonIrisFaceList(irisFaceCondition);
        if (CollectionUtils.isEmpty(irisFaceList)) {
            return null;
        }
        return irisFaceList.get(0);
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
        if (DictConstants.BioModeStatus.DISABLE.equals(channelBusiness.getFaceIrisMode())) {
            throw new CustomException(
                MessageUtils.message("channel.busi.iris.service.scene.not.open.iris", channelCode, uniqueId));
        }
        if (DictConstants.YesOrNoState.YES.equalsIgnoreCase(channelBusiness.getLocked())) {
            throw new CustomException(
                MessageUtils.message("channel.common.service.scene.person.locked", channelCode, uniqueId));
        }
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

    /**
     * 异步保存虹膜人脸多模态1v1比对日志
     * 
     * @param irisFaceVerify
     * @param verifyVO
     * @param basePersonIrisFace
     * @param receivedTime
     * @param deptId
     * @param deptName
     * @param personName
     */
    private void asyncSaveMultiMatchLog(PersonIrisFaceVerify irisFaceVerify, PersonIrisFaceVerifyVO verifyVO,
        BasePersonIrisFace basePersonIrisFace, long receivedTime, Long deptId, String deptName, String personName) {
        long responseTime = System.currentTimeMillis();
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String stockFaceImageBase64 = null;
            String stockIrisImageBase64 = null;
            if (StringUtils.isNotBlank(basePersonIrisFace.getFaceImageUrl())) {
                stockFaceImageBase64 = PlatformFileUtils.getImageBase64(basePersonIrisFace.getFaceImageUrl());
                if (StringUtils.isNotBlank(stockFaceImageBase64)) {
                    stockFaceImageBase64 = DictConstants.Encrypted.ENABLE.equals(basePersonIrisFace.getEncrypted())
                        ? PlatformCryptUtils.decryptImageBase64(stockFaceImageBase64) : stockFaceImageBase64;
                }
            }
            if (StringUtils.isNotBlank(basePersonIrisFace.getIrisImageUrl())) {
                stockIrisImageBase64 = PlatformFileUtils.getImageBase64(basePersonIrisFace.getIrisImageUrl());
                if (StringUtils.isNotBlank(stockIrisImageBase64)) {
                    stockIrisImageBase64 = DictConstants.Encrypted.ENABLE.equals(basePersonIrisFace.getEncrypted())
                        ? PlatformCryptUtils.decryptImageBase64(stockIrisImageBase64) : stockIrisImageBase64;
                }
            }
            PersonFaceirisMatchLog faceirisMatchLog = new PersonFaceirisMatchLog();
            // 进行比对图片的上传
            String baseDir = getMulitCompareImgBaseDir();
            String faceBaseDir = baseDir + "face";
            String irisBaseDir = baseDir + "iris";
            // 加密上传现场照
            if (StringUtils.isNotBlank(irisFaceVerify.getFaceBase64Img())) {
                faceirisMatchLog.setSceneFaceImage(
                    faceRecogLogicService.uploadFaceImg(true, null, irisFaceVerify.getFaceBase64Img(), faceBaseDir));
            }
            if (StringUtils.isNotBlank(irisFaceVerify.getIrisBase64Img())) {
                faceirisMatchLog.setSceneIrisImage(
                    irisRecogLogicService.uploadIrisImg(true, null, irisFaceVerify.getIrisBase64Img(), irisBaseDir));
            }
            // 底库照，重新存储，不能使用底库图片数据，底库图片可能会发生改变
            if (StringUtils.isNotBlank(stockFaceImageBase64)) {
                faceirisMatchLog.setStockFaceImage(
                    faceRecogLogicService.uploadFaceImg(true, null, stockFaceImageBase64, faceBaseDir));
            }
            if (StringUtils.isNotBlank(stockIrisImageBase64)) {
                faceirisMatchLog.setStockIrisImage(
                    faceRecogLogicService.uploadFaceImg(true, null, stockIrisImageBase64, irisBaseDir));
            }
            faceirisMatchLog.setId(IdWorker.getNextStringId());
            faceirisMatchLog.setChannelCode(irisFaceVerify.getChannelCode());
            faceirisMatchLog.setDeviceSn(irisFaceVerify.getDeviceCode());
            if (StringUtils.isNotBlank(irisFaceVerify.getDeviceDimension())) {
                faceirisMatchLog.setDeviceDimension(Double.parseDouble(irisFaceVerify.getDeviceDimension()));
            }
            if (StringUtils.isNotBlank(irisFaceVerify.getDeviceLongitude())) {
                faceirisMatchLog.setDeviceLongitude(Double.parseDouble(irisFaceVerify.getDeviceLongitude()));
            }
            faceirisMatchLog.setDeviceIp(irisFaceVerify.getDeviceIp());
            faceirisMatchLog.setDeviceModel(irisFaceVerify.getDeviceModel());
            faceirisMatchLog.setDeviceName(irisFaceVerify.getDeviceName());
            faceirisMatchLog.setDeviceDirection(irisFaceVerify.getDeviceDirection());
            faceirisMatchLog.setReceivedSeq(irisFaceVerify.getReceivedSeq());
            faceirisMatchLog.setDeviceAddr(irisFaceVerify.getDeviceAddr());
            faceirisMatchLog.setCreateTime(DateUtils.getNowDate());
            faceirisMatchLog.setUniqueId(irisFaceVerify.getUniqueId());
            faceirisMatchLog.setDeptId(deptId);
            faceirisMatchLog.setDeptName(deptName);
            faceirisMatchLog.setPersonName(personName);
            faceirisMatchLog.setReceivedTime(new Date(receivedTime));
            faceirisMatchLog.setTimeUsed(responseTime - receivedTime);
            faceirisMatchLog.setFaceTimeUsed(null);
            faceirisMatchLog.setIrisTimeUsed(null);
            faceirisMatchLog.setCheckliveResult(verifyVO.getLiveDetectionResult());
            faceirisMatchLog.setCheckliveScore(verifyVO.getLiveDetectionScore());
            faceirisMatchLog.setSceneFaceScore(verifyVO.getFaceScore());
            faceirisMatchLog.setSceneIrisScore(verifyVO.getIrisScore());
            faceirisMatchLog.setMatchScore(verifyVO.getFusionScore());
            String result = verifyVO.getResult();
            // 测温温度信息
            faceirisMatchLog.setTemperature(StringUtils.isBlank(irisFaceVerify.getTemperature()) ? null
                : Double.valueOf(irisFaceVerify.getTemperature()));
            faceirisMatchLog.setTemperatureFloor(StringUtils.isBlank(irisFaceVerify.getTemperatureFloor()) ? null
                : Double.valueOf(irisFaceVerify.getTemperatureFloor()));
            faceirisMatchLog.setTemperatureTop(StringUtils.isBlank(irisFaceVerify.getTemperatureTop()) ? null
                : Double.valueOf(irisFaceVerify.getTemperatureTop()));
            if (StringUtils.isNotBlank(irisFaceVerify.getTemperatureResult())) {
                faceirisMatchLog.setTemperatureResult(irisFaceVerify.getTemperatureResult());
            } else if (null != faceirisMatchLog.getTemperature()) {
                Double temperatureVal = faceirisMatchLog.getTemperature();
                Double temperatureTopVal = faceirisMatchLog.getTemperatureTop();
                Double temperatureFloorVal = faceirisMatchLog.getTemperatureFloor();
                if (null != temperatureTopVal && temperatureVal >= temperatureTopVal) {
                    faceirisMatchLog.setTemperatureResult(DictConstants.TemperatureResult.HIGHER);
                } else if (null != temperatureFloorVal && temperatureVal < temperatureFloorVal) {
                    faceirisMatchLog.setTemperatureResult(DictConstants.TemperatureResult.LOWER);
                } else {
                    faceirisMatchLog.setTemperatureResult(DictConstants.TemperatureResult.NORMAL);
                }
            }
            result = DictConstants.BioResult.NOTPASS.equals(irisFaceVerify.getTemperatureResult())
                ? DictConstants.BioResult.NOTPASS : result;// 测温没通过认为失败
            faceirisMatchLog.setResult(result);
            faceirisMatchLog.setMatchMode(irisFaceVerify.getVerifyType());
            faceirisMatchLog.setVendorCode("eyecool");
            try {
                personFaceirisMatchLogMapper.insertPersonFaceirisMatchLog(faceirisMatchLog);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 获取多模态识别图片存储基础路径
     * 
     * @return
     */
    private String getMulitCompareImgBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.BUSI_MULIT_COMPARE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("channel.busi.iris.service.multimodal.match.folder.need",
                SysConfigConstants.BUSI_MULIT_COMPARE_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }

}
