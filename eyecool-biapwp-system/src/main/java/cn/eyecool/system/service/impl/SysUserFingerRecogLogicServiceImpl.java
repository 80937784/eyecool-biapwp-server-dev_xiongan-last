package cn.eyecool.system.service.impl;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.match.service.commons.FeatureData;
import cn.eyecool.system.constant.SystemConstants;
import cn.eyecool.system.domain.SysUserFinger;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysUserFingerRecogLogicService;

import com.eyecool.abis.callmicroservice.IBioFingerMicroService;
import com.eyecool.abis.callmicroservice.IQualityDetectService;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.eyecool.abis.callmicroservice.common.QualityDetectReult;
import com.google.common.collect.Lists;
import io.grpc.StatusRuntimeException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.service.impl
 * @Description: TODO
 * @date Date : 2021年01月19日 上午9:04
 */
@Service
public class SysUserFingerRecogLogicServiceImpl implements ISysUserFingerRecogLogicService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysUserFingerRecogLogicServiceImpl.class);
    private final AtomicLong requestSeqGen = new AtomicLong();

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioFingerMicroService bioFingerMicroService;
    @Autowired
    private IQualityDetectService qualityDetectService;

    @Override
    public SysUserFinger execCheckAndUploadFinger(String loginName, String imageBase64, String fileName,
            SysUserFinger srcFinger, String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        FingerExtractResult fingerExtractResult = null;
        String feature = null;
        Double qualityScore = null;
        if (StringUtils.isNotBlank(imageBase64)) {
            // 获取上传指纹图像特征
            fingerExtractResult = getFingerExtractResult(imageBase64);
            List<String> features = fingerExtractResult.getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                throw new CustomException(MessageUtils.message("person.finger.recog.image.no.finger"));
            }
            if (features.size() > 1) {
                throw new CustomException(MessageUtils.message("person.finger.recog.image.multiple.fingers"));
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
            fingerOne2OneCompare(srcFinger.getFingerNo(), featureBeanList, getOne2OneCompareThreshold(), null);
        }

        /********************************** 设置指纹信息属性 *********************************/
        SysUserFinger sysUserFinger = new SysUserFinger();
        BeanUtils.copyBeanProp(sysUserFinger, srcFinger);// 拷贝已有属性信息
        sysUserFinger.setAlgsVersion(null == fingerExtractResult ? null : fingerExtractResult.getAlgVersion());// 算法版本
        sysUserFinger.setFeature(feature);// 图像特征
        sysUserFinger.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        if (qualityScore != null) {
            sysUserFinger.setQualityScore(qualityScore.longValue()); // 图像质量得分
        }

        if (StringUtils.isBlank(sysUserFinger.getStatus())) {
            sysUserFinger.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }

        if (StringUtils.isBlank(sysUserFinger.getId())) {
            sysUserFinger.setId(IdWorker.getNextStringId());
            sysUserFinger.setCreateTime(DateUtils.getNowDate());
            sysUserFinger.setCreateBy(loginName);
        } else {
            sysUserFinger.setUpdateTime(DateUtils.getNowDate());
            sysUserFinger.setUpdateBy(loginName);
        }
        /**********************************
         * 设置指纹信息属性结束
         *********************************/

        if (StringUtils.isBlank(imageBase64)) {
            sysUserFinger.setImageUrl(null);
            return sysUserFinger;
        }
        String filePathName = uploadFingerImg(true, fileName, imageBase64, null);
        sysUserFinger.setImageUrl(filePathName);// 设置图片路径
        return sysUserFinger;
    }

    @Override
    public FingerExtractResult getFingerExtractResult(String imageBase64) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        try {
            // 指纹特征提取(参数：节点，平台流水[便于跟踪日志],图片的base64)
            long fingerExtractStart = System.currentTimeMillis();
            LOGGER.info("The fingerprint feature extraction algorithm call starts,start:{}", fingerExtractStart);
            FingerExtractResult fingerExtractResult = bioFingerMicroService.fingerExteaction(null, handleSeq,
                    imageBase64);
            long fingerExtractEnd = System.currentTimeMillis();
            LOGGER.info("The fingerprint feature extraction algorithm call ends,start:{},end:{},usedTime:{}ms", fingerExtractStart, fingerExtractEnd,
                    fingerExtractEnd - fingerExtractStart);
            return fingerExtractResult;
        } catch (TimeoutException e) {
            LOGGER.error("Timeout calling [ABIS_MATCH] to extract fingerprint features", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Call [ABIS_MATCH] to extract fingerprint feature exception", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.error", msg));
        }
    }

    @Override
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg) {
        ArrayList<Double> thresholdList = Lists.newArrayList(null == threshold ? getDetectThreshold() : threshold);
        QualityDetectReult imageFingerQuality = null;
        try {
            long qualityDetectStart = System.currentTimeMillis();
            LOGGER.info("The fingerprint image quality detection algorithm call starts,start:{}", qualityDetectStart);
            imageFingerQuality = qualityDetectService.imageFingerQuality(imageBase64, thresholdList.get(0));
            long qualityDetectEnd = System.currentTimeMillis();
            LOGGER.info("The fingerprint image quality detection algorithm call ends,start:{},end:{},usedTime:{}ms", qualityDetectStart, qualityDetectEnd,
                    qualityDetectEnd - qualityDetectStart);
        } catch (TimeoutException e) {
            LOGGER.error("Call [ABIS_DETECT] to detect fingerprint image quality timeout", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("The physical quality of the fingerprint image is abnormally detected", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.error",  msg));
        }
        Boolean result = imageFingerQuality.getResult();
        double score = imageFingerQuality.getScore();
        if (null == result || !result) {
            String defaultMsg =MessageUtils.message("person.finger.recog.detect.quality.check.failed",score,thresholdList.get(0));
            LOGGER.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    @Override
    public String uploadFingerImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传指纹图像
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }

        if (StringUtils.isBlank(baseDir)) {
            baseDir = SystemConstants.SYS_USER_BIO_IMAGE_DIR.FINGER_DIR;
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        try {
            if (StringUtils.isBlank(fileName)) {
                String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(imageBase64);
                fileName = String.valueOf(System.currentTimeMillis() + UUID.randomUUID().hashCode())
                        + imageFileExtendName;
            }
            String filePathName = PlatformFileUploadUtils.upload(baseDir, fileName, imageBase64);
            return filePathName;
        } catch (Exception e) {
            LOGGER.error("Fingerprint image upload exception", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.upload.error"));
        }
    }

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
        return fingerOne2OneCompare(null, featureBeanList, thresholdScore, notPassMsg);
    }

    /**
     * 1：1比对
     *
     * @param featureBeanList
     * @param thresholdScore
     * @param notPassMsg
     * @return
     */
    private double fingerOne2OneCompare(String fingerNo, List<FeatureBean> featureBeanList, double thresholdScore,
            String notPassMsg) {

        List<MatchBean> matchBeanList = fingerOne2OneCompare(featureBeanList);
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (thresholdScore > score) {
            String defaultMsg =MessageUtils.message("sysuser.finger.service.match.not.pass",fingerNo,score,thresholdScore);
            LOGGER.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    @Override
    public List<MatchBean> fingerOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long fingerMatchStart = System.currentTimeMillis();
            LOGGER.info("Fingerprint image 1:1 comparison algorithm call starts,start:{}", fingerMatchStart);
            List<MatchBean> matchBeanList = bioFingerMicroService.fingerMatch(null, null, new ArrayList<>(),
                    featureBeanList);
            long fingerMatchEnd = System.currentTimeMillis();
            LOGGER.info("The fingerprint image 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", fingerMatchStart, fingerMatchEnd,
                    fingerMatchEnd - fingerMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            LOGGER.error("Calling [ABIS_MATCH] to compare fingerprints timed out", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Calling [ABIS_MATCH] to compare fingerprints is abnormal", e);
            throw new CustomException(MessageUtils.message("person.finger.recog.finger.match.error",msg));
        }
    }

    @Override
    public double getOne2OneCompareThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.finger.recog.one.match.one.threshold.configure",SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.finger.recog.one.match.one.threshold.need.number",SysConfigConstants.BASEDATA_FINGER_COMPARE_THRESHOLD_KEY));
        }
    }

    @Override
    public FeatureBean getFeatureBean(String imgBase64) {
        return getFeatureBean(imgBase64, null, null);
    }

    /**
     * 获取FeatureBean
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyFingerMsg, String multiFingerMsg) {
        FingerExtractResult fingerExtractResult = getFingerExtractResult(imgBase64);
        return getFeatureBean(fingerExtractResult, emptyFingerMsg, multiFingerMsg);
    }

    /**
     * 获取FeatureBean
     */
    @Override
    public FeatureBean getFeatureBean(FingerExtractResult fingerExtractResult, String emptyFingerMsg,
            String multiFingerMsg) {
        List<String> features = fingerExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            String defaultMsg = MessageUtils.message("person.finger.recog.image.no.finger");
            LOGGER.error(defaultMsg);
            String msg = StringUtils.isBlank(emptyFingerMsg) ? defaultMsg : emptyFingerMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            String defaultMsg = MessageUtils.message("person.finger.recog.image.multiple.fingers");
            LOGGER.error(defaultMsg);
            String msg = StringUtils.isBlank(multiFingerMsg) ? defaultMsg : multiFingerMsg;
            throw new CustomException(msg);
        }
        FeatureBean imgFeature = new FeatureBean();
        imgFeature.setFeature(features.get(0));
        imgFeature.setType(String.valueOf(FeatureData.FeatureType.FingerFeatureUnknown));
        return imgFeature;
    }

    @Override
    public double getDetectThreshold() {
        String thresholdStr = configService
                .selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.threshold.configure",SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.finger.recog.detect.quality.threshold.need.number",SysConfigConstants.BASEDATA_FINGER_QUALITY_DETECT_THRESHOLD_KEY));
        }
    }

    /**
     * 校验是否有重复指纹
     */
    @Override
    public boolean checkHasRepeatFinger(List<String> featureList, Double threshold) {
        if (CollectionUtils.isEmpty(featureList) || featureList.size() <= 1) {
            return false;
        }
        Double thresholdScore = threshold;
        if (null == threshold) {
            String thresholdStr = configService
                    .selectConfigByKey(SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY);
            if (StringUtils.isBlank(thresholdStr)) {
                throw new CustomException(MessageUtils.message("person.finger.recog.repeat.threshold.configure", SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY ));
            }
            try {
                thresholdScore = Double.valueOf(thresholdStr);
            } catch (Exception e) {
                throw new CustomException(MessageUtils.message("person.finger.recog.repeat.threshold.need.number",SysConfigConstants.BASEDATA_FINGER_REPEAT_THRESHOLD_KEY));
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
                    LOGGER.info("There are duplicate fingerprint images");
                    return true;
                }
            }
        }
        return false;
    }
}
