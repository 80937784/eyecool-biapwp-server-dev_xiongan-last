package cn.eyecool.basedata.manager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.IBioFingerMicroService;
import com.eyecool.abis.callmicroservice.IQualityDetectService;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.FingerSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.eyecool.abis.callmicroservice.common.QualityDetectReult;
import com.google.common.collect.Lists;

import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.manager.IPersonFingerRecogLogicService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.FileModel;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.match.service.commons.FeatureData;
import cn.eyecool.system.service.ISysConfigService;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * 指纹识别、比对逻辑实现
 * 
 * @author admin
 * @date 2019年10月24日
 */
@Service
@Slf4j
public class PersonFingerRecogLogicServiceImpl implements IPersonFingerRecogLogicService {

    // 序列号原子对象
    private final AtomicLong requestSeqGen = new AtomicLong();

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioFingerMicroService bioFingerMicroService;
    @Autowired
    private IQualityDetectService qualityDetectService;

    /**
     * 获取指纹特征
     * 
     * @param imageBase64
     * @return
     */
    @Override
    public FingerExtractResult getFingerExtractResult(String imageBase64) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        try {
            // 指纹特征提取(参数：节点，平台流水[便于跟踪日志],图片的base64)
            long fingerExtractStart = System.currentTimeMillis();
            log.info("Finger feature extraction algorithm call starts,start:{}", fingerExtractStart);
            FingerExtractResult fingerExtractResult =
                bioFingerMicroService.fingerExteaction(null, handleSeq, imageBase64);
            long fingerExtractEnd = System.currentTimeMillis();
            log.info("Fingerprint feature extraction algorithm call ends,start:{},end:{},usedTime:{}ms", fingerExtractStart, fingerExtractEnd,
                fingerExtractEnd - fingerExtractStart);
            return fingerExtractResult;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] to extract the finger feature timeout", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.extract.feature.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] to extract finger feature error", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.extract.feature.error", msg));
        }
    }

    /**
     * 获取FeatureBean
     * 
     * @param imgBase64
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64) {
        return getFeatureBean(imgBase64, null, null);
    }

    /**
     * 获取FeatureBean
     * 
     * @param imgBase64
     * @param emptyFingerMsg
     * @param multiFingerMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyFingerMsg, String multiFingerMsg) {
        FingerExtractResult fingerExtractResult = getFingerExtractResult(imgBase64);
        return getFeatureBean(fingerExtractResult, emptyFingerMsg, multiFingerMsg);
    }

    /**
     * 获取FeatureBean
     * 
     * @param fingerExtractResult
     * @param emptyFingerMsg
     * @param multiFingerMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(FingerExtractResult fingerExtractResult, String emptyFingerMsg,
        String multiFingerMsg) {
        List<String> features = fingerExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            log.error("Image is not detected finger");
            String defaultMsg = MessageUtils.message("person.finger.recog.image.no.finger");
            String msg = StringUtils.isBlank(emptyFingerMsg) ? defaultMsg : emptyFingerMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            log.error("Image is detected multiple fingers");
            String defaultMsg = MessageUtils.message("person.finger.recog.image.multiple.fingers");
            String msg = StringUtils.isBlank(multiFingerMsg) ? defaultMsg : multiFingerMsg;
            throw new CustomException(msg);
        }
        FeatureBean imgFeature = new FeatureBean();
        imgFeature.setFeature(features.get(0));
        imgFeature.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
        return imgFeature;
    }

    /**
     * 指纹图像质量检测
     * 
     * @param imageBase64
     * @param threshold
     * @param notPassMsg
     * @return
     */
    @Override
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg) {
        ArrayList<Double> thresholdList = Lists.newArrayList(null == threshold ? getDetectThreshold() : threshold);
        QualityDetectReult imageFingerQuality = null;
        try {
            long qualityDetectStart = System.currentTimeMillis();
            log.info("Fingerprint image quality detection algorithm call starts,start:{}", qualityDetectStart);
            imageFingerQuality = qualityDetectService.imageFingerQuality(imageBase64, thresholdList.get(0));
            long qualityDetectEnd = System.currentTimeMillis();
            log.info("Fingerprint image quality detection algorithm call ends,start:{},end:{},usedTime:{}ms", qualityDetectStart, qualityDetectEnd,
                qualityDetectEnd - qualityDetectStart);
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_DETECT] to check the quality timeout", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("The physical quality of the fingerprint image is abnormally detected", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.error", msg));
        }
        Boolean result = imageFingerQuality.getResult();
        double score = imageFingerQuality.getScore();
        if (null == result || !result) {
            String s = MessageUtils.message("person.finger.recog.detect.quality.check.failed", score,thresholdList.get(0));
            log.info(s);
            String defaultMsg = s;
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 获取指纹质量检测阈值参数
     * 
     * @return
     */
    @Override
    public double getDetectThreshold() {
        String thresholdStr =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.finger.recog.detect.quality.threshold.configure", SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.finger.recog.detect.quality.threshold.need.number", SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 进行1:1图片比对
     * 
     * @param imageBase64_1
     * @param imageBase64_2
     * @param threshold
     * @param notPassMsg
     * @return
     */
    @Override
    public double fingerOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold,
        String notPassMsg) {
        Double thresholdScore = threshold;
        if (null == threshold) {
            thresholdScore = getOne2OneCompareThreshold();
        }
        List<FeatureBean> featureBeanList = new ArrayList<>();
        featureBeanList.add(getFeatureBean(imageBase64_1));
        featureBeanList.add(getFeatureBean(imageBase64_2));
        return fingerOne2OneCompare(featureBeanList, thresholdScore, notPassMsg);
    }

    /**
     * 获取1:1比对阈值参数
     * 
     * @return
     */
    @Override
    public double getOne2OneCompareThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.finger.recog.one.match.one.threshold.configure", SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.finger.recog.one.match.one.threshold.need.number", SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 指纹图像1:1认证
     * 
     * @param featureBeanList
     * @return
     */
    @Override
    public List<MatchBean> fingerOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long fingerMatchStart = System.currentTimeMillis();
            log.info("Finger image 1:1 comparison algorithm call starts,start:{}", fingerMatchStart);
            List<MatchBean> matchBeanList =
                bioFingerMicroService.fingerMatch(null, null, new ArrayList<>(), featureBeanList);
            long fingerMatchEnd = System.currentTimeMillis();
            log.info("Finger image 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", fingerMatchStart, fingerMatchEnd,
                fingerMatchEnd - fingerMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] theo compare finger timeout", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] to compare finger error", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.error", msg));
        }
    }

    /**
     * 查询指纹入库是否进行1-N校验
     * 
     * @return
     */
    @Override
    public boolean getFingerAddIsValidateN() {
        // 平台没有配置1-N功能，则直接就返回不校验
        if (!getPlatformIsOpenSearchN()) {
            return false;
        }
        // 平配置了1-N功能，则再查询是否入库进行校验
        String validConfig = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_ADD_ISVALIDN_KEY);
        // 不进行参数配置则默认进行1：N校验
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    /**
     * 上传入库指纹图像
     * 
     * @param encrypted
     * @param fileName
     * @param imageBase64
     * @param baseDir
     * @return
     */
    @Override
    public String uploadFingerImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传指纹图像, 拓展名的获取需要在加密之前获取
        String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(imageBase64);
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }

        if (StringUtils.isBlank(baseDir)) {
            baseDir = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_DIR_KEY);
            if (StringUtils.isBlank(baseDir)) {
                String msg = MessageUtils.message("person.finger.recog.upload.finger.folder.need", SysConfigConstants.BASEDATA_FINGER_DIR_KEY);
                throw new CustomException(msg);
            }
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        try {
            if (StringUtils.isBlank(fileName)) {
                fileName =
                    String.valueOf(System.currentTimeMillis() + UUID.randomUUID().hashCode()) + imageFileExtendName;
            }
            String filePathName = PlatformFileUploadUtils.upload(baseDir, fileName, imageBase64);
            return filePathName;
        } catch (Exception e) {
            log.error("Fingerprint image upload exception", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.upload.error"));
        }
    }

    /**
     * 校验是否有重复指纹
     * 
     * @param featureList
     * @param threshold
     * @return
     */
    @Override
    public boolean checkHasRepeatFinger(List<String> featureList, Double threshold) {
        if (CollectionUtils.isEmpty(featureList) || featureList.size() <= 1) {
            return false;
        }
        Double thresholdScore = threshold;
        if (null == threshold) {
            String thresholdStr =
                configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY);
            if (StringUtils.isBlank(thresholdStr)) {
                String msg = MessageUtils.message("person.finger.recog.repeat.threshold.configure", SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY );
                throw new CustomException(msg);
            }
            try {
                thresholdScore = Double.valueOf(thresholdStr);
            } catch (Exception e) {
                String msg  =MessageUtils.message("person.finger.recog.repeat.threshold.need.number",  SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY);
                throw new CustomException(msg);
            }
        }
        List<FeatureBean> featureBeanList = featureList.stream().map(it -> {
            FeatureBean imgFeature = new FeatureBean();
            imgFeature.setFeature(it);
            imgFeature.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            return imgFeature;
        }).collect(Collectors.toList());
        List<MatchBean> matchBeanList = fingerOne2OneCompare(featureBeanList);
        for (int i = 0; i < featureList.size(); i++) {
            for (int j = 0; j < featureList.size() - i - 1; j++) {
                double score = matchBeanList.get(i).getResults().get(j).getScore();
                if (thresholdScore < score) {
                    log.info("There are duplicate fingerprint images");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 指纹1-N搜索
     * 
     * @param feature
     * @param channelCode
     * @param topN
     * @param threshold
     * @return
     */
    @Override
    public List<FingerSearchResult> fingerSearchN(String feature, String channelCode, Integer topN, Double threshold) {
        // 平台没有开启1-N功能，直接返回空列表
        if (!getPlatformIsOpenSearchN()) {
            return Collections.emptyList();
        }
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        Double thresholdScore = null == threshold ? getSearchNThreshold() : threshold;
        try {
            // 如果开启了多租户，传入的场景编码为空的时候，则是租户大库搜索
            String tenantId = TenantContextHolder.getTenantId();
            String realLibId = StringUtils.EMPTY;
            if (StringUtils.isBlank(tenantId)) {
                realLibId = channelCode;
            } else if (StringUtils.isBlank(channelCode)) {
                realLibId = tenantId;
            } else {
                realLibId = tenantId + "#" + channelCode;
            }
            long fingerSearchStart = System.currentTimeMillis();
            log.info("Fingerprint 1-N search algorithm call starts,start:{}", fingerSearchStart);
            List<FingerSearchResult> searchResultList =
                bioFingerMicroService.fingerMiniSearch("", handleSeq, feature, realLibId, topN, thresholdScore);
            long fingerSeachEnd = System.currentTimeMillis();
            log.info("Fingerprint 1-N search algorithm call ends,start:{},end:{},usedTime:{}ms", fingerSearchStart, fingerSeachEnd,
                fingerSeachEnd - fingerSearchStart);
            if (CollectionUtils.isNotEmpty(searchResultList)) {
                searchResultList = searchResultList.stream()
                    .sorted((e1, e2) -> Double.valueOf(e2.getScore()).compareTo(e1.getScore())).map(it -> {
                        // 搜索出来的用户标识去除租户ID前缀信息
                        if (it.getUserId().indexOf("#") != -1) {
                            it.setUserId(it.getUserId().substring(it.getUserId().indexOf("#") + 1));
                        }
                        return it;
                    }).collect(Collectors.toList());
            }
            return searchResultList;
        } catch (TimeoutException e) {
            log.error("invoke [FOX_MINISEARCH] to search finger timeout", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.1n.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [FOX_MINISEARCH] the search finger error", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.1n.error",  msg));
        }
    }

    /**
     * 获取多指纹特征
     * 
     * @param sceneImage
     * @return
     */
    @Override
    public List<FeatureBean> getMultiPersonFingerFeature(String sceneImage) {
        FingerExtractResult fingerExtractResult = getFingerExtractResult(sceneImage);
        if (CollectionUtils.isEmpty(fingerExtractResult.getFeatures())) {
            return Collections.emptyList();
        }
        return fingerExtractResult.getFeatures().parallelStream().map(f -> {
            FeatureBean imgFeature = new FeatureBean();
            imgFeature.setFeature(f);
            imgFeature.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            return imgFeature;
        }).collect(Collectors.toList());
    }

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param fingerImgFile
     * @param srcfinger
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFinger execCheckAndUploadFinger(MultipartFile fingerImgFile, BasePersonFinger srcfinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        String imageBase64 = null == fingerImgFile ? null : PlatformFileUtils.getImageBase64(fingerImgFile);
        String fileName = null == fingerImgFile ? null : fingerImgFile.getOriginalFilename();
        return execCheckAndUploadFinger(imageBase64, fileName, srcfinger, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param fileModel
     * @param srcfinger
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFinger execCheckAndUploadFinger(FileModel fileModel, BasePersonFinger srcfinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        String imageBase64 =
            null == fileModel ? null : PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
        String fileName = null == fileModel ? null : fileModel.getFileName();
        return execCheckAndUploadFinger(imageBase64, fileName, srcfinger, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param imageBase64
     * @param fileName
     * @param srcfinger
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFinger execCheckAndUploadFinger(String imageBase64, String fileName, BasePersonFinger srcfinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        FingerExtractResult fingerExtractResult = null;
        String feature = null;
        Double qualityScore = null;
        if (StringUtils.isNotBlank(imageBase64)) {
            // 获取上传指纹图像特征
            fingerExtractResult = getFingerExtractResult(imageBase64);
            List<String> features = fingerExtractResult.getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                String msg = MessageUtils.message("person.finger.recog.check.no.finger", srcfinger.getFingerNo());
                throw new CustomException(msg);
            }
            if (features.size() > 1) {
                String msg = MessageUtils.message("person.finger.recog.check.multiple.fingers", srcfinger.getFingerNo());
                throw new CustomException(msg);
            }
            feature = features.get(0);
            // 图像质量检测
            qualityScore = qualityDetect(imageBase64, null, null);
        }
        // 进行1:1比对
        if (isUpdate && StringUtils.isNotBlank(stockImgFeature)) {
            List<FeatureBean> featureBeanList = new ArrayList<>();
            FeatureBean featureBean = new FeatureBean();
            featureBean.setFeature(feature);
            featureBean.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            FeatureBean stockImgFeatureBaan = new FeatureBean();
            stockImgFeatureBaan.setFeature(stockImgFeature);
            stockImgFeatureBaan.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
            featureBeanList.add(featureBean);
            featureBeanList.add(stockImgFeatureBaan);
            fingerOne2OneCompare(featureBeanList, getOne2OneCompareThreshold(), null);
        }
        isValidN = null == isValidN ? getFingerAddIsValidateN() : isValidN;
        // 进行1：N校验
        if (isValidN) {
            List<FingerSearchResult> searchNResult = fingerSearchN(feature, "", 1, null);
            String uniqueId = srcfinger.getUniqueId();
            if (CollectionUtils.isNotEmpty(searchNResult) && !searchNResult.get(0).getUserId().equals(uniqueId)) {
                String msg = MessageUtils.message("person.finger.recog.1n.check.failed", srcfinger.getFingerNo());
                throw new CustomException(msg);
            }
        }
        /********************************** 设置指纹信息属性 *********************************/
        BasePersonFinger basePersonFinger = new BasePersonFinger();
        BeanUtils.copyBeanProp(basePersonFinger, srcfinger);// 拷贝已有属性信息
        basePersonFinger.setAlgsVersion(null == fingerExtractResult ? null : fingerExtractResult.getAlgVersion());// 算法版本
        basePersonFinger.setFeature(feature);// 图像特征
        basePersonFinger.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        basePersonFinger.setQualityScore(qualityScore); // 图像质量得分
        // 默认加密
        if (StringUtils.isBlank(basePersonFinger.getEncrypted())) {
            basePersonFinger.setEncrypted(DictConstants.Encrypted.ENABLE);// 是否加密，默认加密
        }
        if (StringUtils.isBlank(basePersonFinger.getDatasource())) {
            basePersonFinger.setDatasource(DictConstants.DataSource.INTERFACE);// 数据源，默认是接口
        }
        if (StringUtils.isBlank(basePersonFinger.getStatus())) {
            basePersonFinger.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }
        if (StringUtils.isBlank(basePersonFinger.getCoercivePosition())) {
            basePersonFinger.setCoercivePosition("0");
        }
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(basePersonFinger.getId())) {
            basePersonFinger.setId(IdWorker.getNextStringId());
            basePersonFinger.setCreateTime(DateUtils.getNowDate());
            basePersonFinger.setCreateBy(loginName);
        } else {
            basePersonFinger.setUpdateTime(DateUtils.getNowDate());
            basePersonFinger.setUpdateBy(loginName);
        }
        /********************************** 设置指纹信息属性结束 *********************************/

        if (StringUtils.isBlank(imageBase64)) {
            basePersonFinger.setImageUrl(null);
            return basePersonFinger;
        }
        // 上传指纹图像
        boolean encrypted = StringUtils.isBlank(basePersonFinger.getEncrypted())
            || DictConstants.Encrypted.ENABLE.equals(basePersonFinger.getEncrypted());
        String filePathName = uploadFingerImg(encrypted, fileName, imageBase64, null);
        basePersonFinger.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
        basePersonFinger.setImageUrl(filePathName);// 设置图片路径
        return basePersonFinger;
    }

    /**
     * 1：1比对
     * 
     * @param featureBeanList
     * @param thresholdScore
     * @param notPassMsg
     * @return
     */
    private double fingerOne2OneCompare(List<FeatureBean> featureBeanList, double thresholdScore, String notPassMsg) {
        List<MatchBean> matchBeanList = fingerOne2OneCompare(featureBeanList);
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (thresholdScore > score) {
            String s = MessageUtils.message("person.finger.recog.one.match.one.check.failed", score,thresholdScore);
            log.info(s);
            String defaultMsg = s;
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 获取指纹1-N搜索阈值
     * 
     * @return
     */
    private double getSearchNThreshold() {
        String thresholdStr =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_SEARCH_N_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.finger.recog.1n.threshold.configure", SysConfigConstants.BASEDATA_FINGER_SEARCH_N_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            String msg = MessageUtils.message("person.finger.recog.1n.threshold.need.number", SysConfigConstants.BASEDATA_FINGER_SEARCH_N_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
    }

    /**
     * 获取平台是否开启了1-N功能
     * 
     * @return
     */
    private boolean getPlatformIsOpenSearchN() {
        String validConfig = configService.selectConfigByKey(SysConfigConstants.PLATFORM_SEARCH_N_FUNCTION_OPEN_KEY);
        // 不进行参数配置则默认开启了1-N功能
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }
}
