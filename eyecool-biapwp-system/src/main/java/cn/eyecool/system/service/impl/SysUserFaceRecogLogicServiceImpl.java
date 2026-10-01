package cn.eyecool.system.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eyecool.abis.callmicroservice.IBioFaceMicroService;
import com.eyecool.abis.callmicroservice.IQualityDetectService;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.MatchBean;
import com.eyecool.abis.callmicroservice.common.QualityDetectReult;

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
import cn.eyecool.system.domain.SysUserFace;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysUserFaceRecogLogicService;
import io.grpc.StatusRuntimeException;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.service.impl
 * @Description: TODO
 * @date Date : 2021年01月19日 上午9:02
 */
@Service
public class SysUserFaceRecogLogicServiceImpl implements ISysUserFaceRecogLogicService {
    // 序列号原子对象
    private final AtomicLong requestSeqGen = new AtomicLong();

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioFaceMicroService bioFaceMicroService;
    @Autowired
    private IQualityDetectService qualityDetectService;
    private static final Logger LOGGER = LoggerFactory.getLogger(SysUserFaceRecogLogicServiceImpl.class);

    @Override
    public SysUserFace execCheckAndUploadFace(String loginName, String imageBase64, String fileName,
        SysUserFace srcFace, String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        FaceExtractResult faceExtractResult = null;
        String feature = null;
        Double qualityScore = null;

        if (StringUtils.isNotBlank(imageBase64)) {
            // 获取图像特征
            faceExtractResult = getFaceExtractResult(imageBase64);
            List<String> features = faceExtractResult.getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                LOGGER.error("No face detected in the picture, please upload a clear face picture");
                throw new CustomException(MessageUtils.message("person.face.recog.image.no.face"));
            }
            if (features.size() > 1) {
                LOGGER.error("The picture has detected multiple faces, please upload a single face picture");
                throw new CustomException(MessageUtils.message("person.face.recog.image.multiple.faces"));
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
            featureBean.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
            FeatureBean stockImgFeatureBaan = new FeatureBean();
            stockImgFeatureBaan.setFeature(stockImgFeature);
            stockImgFeatureBaan.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
            featureBeanList.add(featureBean);
            featureBeanList.add(stockImgFeatureBaan);
            faceOne2OneCompare(featureBeanList, getOne2OneCompareThreshold(), null);
        }
        // 设置人脸信息属性
        SysUserFace sysUserFace = new SysUserFace();
        BeanUtils.copyBeanProp(sysUserFace, srcFace);// 拷贝已有属性信息
        sysUserFace.setAlgsVersion(faceExtractResult == null ? null : faceExtractResult.getAlgVersion());// 算法版本
        sysUserFace.setFeature(feature);// 图像特征
        sysUserFace.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        sysUserFace.setQualityScore(qualityScore != null ? qualityScore.longValue() : 0); // 图像质量得分
        // 默认加密
        if (StringUtils.isBlank(sysUserFace.getStatus())) {
            sysUserFace.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }
        if (StringUtils.isBlank(sysUserFace.getId())) {
            sysUserFace.setId(IdWorker.getNextStringId());
            sysUserFace.setCreateTime(DateUtils.getNowDate());
            sysUserFace.setCreateBy(loginName);
        } else {
            sysUserFace.setUpdateTime(DateUtils.getNowDate());
            sysUserFace.setUpdateBy(loginName);
        }
        /**********************************
         * 设置人脸信息属性结束
         *********************************/
        if (StringUtils.isBlank(imageBase64)) {
            sysUserFace.setImageUrl(null);
            return sysUserFace;
        }
        // 上传人脸图片
        String filePathName = uploadFaceImg(true, fileName, imageBase64, null);
        sysUserFace.setImageUrl(filePathName);// 设置图片路径
        return sysUserFace;
    }

    @Override
    public boolean getFaceAddIsCheckLive() {
        // 再查询是否入库进行活体检测
        String validConfig = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_ADD_CHECKLIVE_KEY);
        // 不进行参数配置则默认进行活体校验
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    @Override
    public CheckLiveResponse checkLive(String imageBase64, Double threshold) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        Double thresholdScore = null == threshold ? getCheckLiveThreshold(false) : threshold;
        try {
            long faceDetectStart = System.currentTimeMillis();
            LOGGER.info("The face detection algorithm call starts,start:{}", faceDetectStart);
            CheckLiveResponse response =
                bioFaceMicroService.faceImageLivenessDetect(handleSeq, imageBase64, thresholdScore);
            long faceDetectEnd = System.currentTimeMillis();
            LOGGER.info("The face detection algorithm call ends,start:{},end:{},usedTime:{}ms,score:{},threshold:{},result:{}", faceDetectStart,
                faceDetectEnd, faceDetectEnd - faceDetectStart, response.getScore(), thresholdScore,
                response.getResult());
            return response;
        } catch (TimeoutException e) {
            LOGGER.error("Call [ABIS_CHECKLIVE] face check live timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Call [ABIS_CHECKLIVE] face check live exception", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.error",msg));
        }
    }

    /**
     * 获取人脸检活阈值参数
     *
     * @return
     */
    @Override
    public double getCheckLiveThreshold(boolean isVideo) {
        String key = isVideo ? SysConfigConstants.BASEDATA_FACE_VIDEO_CHECKLIVE_THRESHOLD_KEY
            : SysConfigConstants.BASEDATA_FACE_CHECKLIVE_THRESHOLD_KEY;
        String thresholdStr = configService.selectConfigByKey(key);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.threshold.configure", key ));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.threshold.need.number", key ));
        }
    }

    /**
     * 获取人脸特征
     *
     * @param imageBase64
     * @return
     */
    @Override
    public FaceExtractResult getFaceExtractResult(String imageBase64) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        try {
            // 指纹特征提取(参数：节点，平台流水[便于跟踪日志],图片的base64)
            long faceExtractStart = System.currentTimeMillis();
            LOGGER.info("The face feature extraction algorithm call starts,start:{}", faceExtractStart);
            FaceExtractResult faceExtractResult = bioFaceMicroService.faceExteaction(null, handleSeq, imageBase64);
            long faceExtractEnd = System.currentTimeMillis();
            LOGGER.info("The face feature extraction algorithm call ends,start:{},end:{},usedTime:{}ms", faceExtractStart, faceExtractEnd,
                faceExtractEnd - faceExtractStart);
            return faceExtractResult;
        } catch (TimeoutException e) {
            LOGGER.error("Timeout calling [ABIS_MATCH] to extract face features", e);
            throw new CustomException(MessageUtils.message("person.face.recog.extract.feature.timeou"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Calling [ABIS_MATCH] to extract facial features is abnormal", e);
            throw new CustomException(MessageUtils.message("person.face.recog.extract.feature.error",msg));
        }
    }

    /**
     * 图片质量检测
     */
    @Override
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg) {
        Double thresholdScore = null == threshold ? getDetectThreshold() : threshold;
        QualityDetectReult imageQuality = null;
        try {
            long qualityDetectStart = System.currentTimeMillis();
            LOGGER.info("The face image quality detection algorithm call starts,start:{}", qualityDetectStart);
            imageQuality = qualityDetectService.imageQuality(imageBase64, thresholdScore);
            long qualityDetectEnd = System.currentTimeMillis();
            LOGGER.info("The face image quality detection algorithm call ends,start:{},end:{},usedTime:{}ms", qualityDetectStart, qualityDetectEnd,
                qualityDetectEnd - qualityDetectStart);
        } catch (TimeoutException e) {
            LOGGER.error("Timeout when calling [ABIS_DETECT] to detect the quality of the face image", e);
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Abnormal physical quality detection of face pictures", e);
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.error", msg));
        }
        Boolean result = imageQuality.getResult();
        double score = imageQuality.getScore();
        if (null == result || !result) {
            String defaultMsg = MessageUtils.message("person.face.recog.detect.quality.check.failed", score,thresholdScore);
            LOGGER.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 获取人脸质量检测阈值参数
     *
     * @return
     */
    @Override
    public double getDetectThreshold() {
        String thresholdStr =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.threshold.configure",SysConfigConstants.BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.threshold.need.number",SysConfigConstants.BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY));
        }
    }

    /**
     * 人脸图像上传
     */
    @Override
    public String uploadFaceImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传人脸图片
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }
        if (StringUtils.isBlank(baseDir)) {
            baseDir = SystemConstants.SYS_USER_BIO_IMAGE_DIR.FACE_DIR;
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        try {
            if (StringUtils.isBlank(fileName)) {
                String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(imageBase64);
                fileName =
                    String.valueOf(System.currentTimeMillis() + UUID.randomUUID().hashCode()) + imageFileExtendName;
            }
            String filePathName = PlatformFileUploadUtils.upload(baseDir, fileName, imageBase64);
            return filePathName;
        } catch (Exception e) {
            LOGGER.error("Abnormal upload of face pictures", e);
            throw new CustomException(MessageUtils.message("person.face.recog.upload.face.error"));
        }
    }

    /**
     * 进行1:1图片比对
     */
    @Override
    public double faceOne2OneCompare(FeatureBean imageFeature1, FeatureBean imageFeature2, Double threshold,
        String notPassMsg) {
        Double thresholdScore = threshold;
        if (null == threshold) {
            thresholdScore = getOne2OneCompareThreshold();
        }
        List<FeatureBean> featureBeanList = new ArrayList<>();
        featureBeanList.add(imageFeature1);
        featureBeanList.add(imageFeature2);
        return faceOne2OneCompare(featureBeanList, thresholdScore, notPassMsg);
    }

    /**
     * 进行1:1图片比对
     */
    @Override
    public double faceOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg) {
        Double thresholdScore = threshold;
        if (null == threshold) {
            thresholdScore = getOne2OneCompareThreshold();
        }
        List<FeatureBean> featureBeanList = new ArrayList<>();
        featureBeanList.add(getFeatureBean(imageBase64_1));
        featureBeanList.add(getFeatureBean(imageBase64_2));
        return faceOne2OneCompare(featureBeanList, thresholdScore, notPassMsg);
    }

    /**
     * 获取1:1比对阈值参数
     *
     * @return
     */
    @Override
    public double getOne2OneCompareThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.1n.threshold.configure",SysConfigConstants.BASEDATA_FACE_COMPARE_THRESHOLD_KEY));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.1n.threshold.need.number",SysConfigConstants.BASEDATA_FACE_COMPARE_THRESHOLD_KEY));
        }
    }

    /**
     * 1：1比对
     *
     * @param featureBeanList
     * @param thresholdScore
     * @param notPassMsg
     * @return
     */
    private double faceOne2OneCompare(List<FeatureBean> featureBeanList, double thresholdScore, String notPassMsg) {
        List<MatchBean> matchBeanList = faceOne2OneCompare(featureBeanList);
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (thresholdScore > score) {
            String defaultMsg = MessageUtils.message("person.face.recog.one.match.one.check.failed",score,thresholdScore);
            LOGGER.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 人脸1:1认证
     */
    @Override
    public List<MatchBean> faceOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long faceMatchStart = System.currentTimeMillis();
            LOGGER.info("The face image 1:1 comparison algorithm call starts,start:{}", faceMatchStart);
            List<MatchBean> matchBeanList =
                bioFaceMicroService.faceMatch(null, null, new ArrayList<>(), featureBeanList);
            long faceMatchEnd = System.currentTimeMillis();
            LOGGER.info("The face image 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", faceMatchStart, faceMatchEnd,
                faceMatchEnd - faceMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            LOGGER.error("Timeout when calling [ABIS_MATCH] to compare faces", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Calling [ABIS_MATCH] to compare faces is abnormal", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.match.error", msg));
        }
    }

    @Override
    public FeatureBean getFeatureBean(String imgBase64) {
        return getFeatureBean(imgBase64, null, null);
    }

    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyFaceMsg, String multiFaceMsg) {
        FaceExtractResult faceExtractResult = getFaceExtractResult(imgBase64);
        return getFeatureBean(faceExtractResult, emptyFaceMsg, multiFaceMsg);
    }

    @Override
    public FeatureBean getFeatureBean(FaceExtractResult faceExtractResult, String emptyFaceMsg, String multiFaceMsg) {
        List<String> features = faceExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            String defaultMsg =  MessageUtils.message("person.face.recog.image.no.face");
            LOGGER.error(defaultMsg);
            String msg = StringUtils.isBlank(emptyFaceMsg) ? defaultMsg : emptyFaceMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            String defaultMsg = MessageUtils.message("person.face.recog.image.multiple.faces");
            LOGGER.error(defaultMsg);
            String msg = StringUtils.isBlank(multiFaceMsg) ? defaultMsg : multiFaceMsg;
            throw new CustomException(msg);
        }
        FeatureBean imgFeature = new FeatureBean();
        imgFeature.setFeature(features.get(0));
        imgFeature.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
        return imgFeature;
    }

}
