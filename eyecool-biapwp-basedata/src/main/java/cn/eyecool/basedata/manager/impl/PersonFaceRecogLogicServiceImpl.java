package cn.eyecool.basedata.manager.impl;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.*;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.FileModel;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.match.service.commons.FeatureData;
import cn.eyecool.system.service.ISysConfigService;
import com.eyecool.abis.callmicroservice.IBioFaceMicroService;
import com.eyecool.abis.callmicroservice.IQualityDetectService;
import com.eyecool.abis.callmicroservice.common.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 人脸识别、比对逻辑实现
 * 
 * @author admin
 * @date 2019年10月24日
 */
@Service
@Slf4j
public class PersonFaceRecogLogicServiceImpl implements IPersonFaceRecogLogicService {

    /** 序列号原子对象 */
    private final AtomicLong requestSeqGen = new AtomicLong();

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioFaceMicroService bioFaceMicroService;
    @Autowired
    private IQualityDetectService qualityDetectService;

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
            log.info("the facial feature extraction algorithm call starts,start:{}", faceExtractStart);
            FaceExtractResult faceExtractResult = bioFaceMicroService.faceExteaction(null, handleSeq, imageBase64);
            long faceExtractEnd = System.currentTimeMillis();
            log.info("the facial feature extraction algorithm call ends，start:{},end:{},usedTime:{}ms", faceExtractStart, faceExtractEnd,
                faceExtractEnd - faceExtractStart);
            return faceExtractResult;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] extract the face timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.extract.feature.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] extract the face error", e);
            throw new CustomException(MessageUtils.message("person.face.recog.extract.feature.error",msg));
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
     * @param emptyFaceMsg
     * @param multiFaceMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyFaceMsg, String multiFaceMsg) {
        FaceExtractResult faceExtractResult = getFaceExtractResult(imgBase64);
        return getFeatureBean(faceExtractResult, emptyFaceMsg, multiFaceMsg);
    }

    /**
     * 获取FeatureBean
     * 
     * @param faceExtractResult
     * @param emptyFaceMsg
     * @param multiFaceMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(FaceExtractResult faceExtractResult, String emptyFaceMsg, String multiFaceMsg) {
        List<String> features = faceExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            log.error("the image is not detected face.");
            String defaultMsg = MessageUtils.message("person.face.recog.image.no.face");
            String msg = StringUtils.isBlank(emptyFaceMsg) ? defaultMsg : emptyFaceMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            log.error("the image is detected multiple faces.");
            String defaultMsg = MessageUtils.message("person.face.recog.image.multiple.faces");
            String msg = StringUtils.isBlank(multiFaceMsg) ? defaultMsg : multiFaceMsg;
            throw new CustomException(msg);
        }
        FeatureBean imgFeature = new FeatureBean();
        imgFeature.setFeature(features.get(0));
        imgFeature.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
        return imgFeature;
    }

    /**
     * 图片质量检测
     * 
     * @param imageBase64
     * @param threshold
     * @param notPassMsg
     * @return
     */
    @Override
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg) {
        Double thresholdScore = null == threshold ? getDetectThreshold() : threshold;
        QualityDetectReult imageQuality = null;
        try {
            long qualityDetectStart = System.currentTimeMillis();
            log.info("face image quality detection algorithm call starts,start:{}", qualityDetectStart);
            imageQuality = qualityDetectService.imageQuality(imageBase64, thresholdScore);
            long qualityDetectEnd = System.currentTimeMillis();
            log.info("face image quality detection algorithm call ends,start:{},end:{},usedTime:{}ms", qualityDetectStart, qualityDetectEnd,
                qualityDetectEnd - qualityDetectStart);
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_DETECT] to detect the quality timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("abnormal physical quality detection of face pictures", e);
            throw new CustomException(MessageUtils.message("person.face.recog.detect.quality.error", msg));
        }
        Boolean result = imageQuality.getResult();
        double score = imageQuality.getScore();
        if (null == result || !result) {
            log.info("the face image quality check failed,score:{}, threshold:{}", score, thresholdScore);
            String defaultMsg =  MessageUtils.message("person.face.recog.detect.quality.check.failed", score,thresholdScore);
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
            String msg  =  MessageUtils.message("person.face.recog.detect.quality.threshold.configure", SysConfigConstants.BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.face.recog.detect.quality.threshold.need.number", SysConfigConstants.BASEDATA_FACE_QUALITY_DETECT_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 1:1图片比对
     * 
     * @param imageFeature1
     * @param imageFeature2
     * @param threshold
     * @param notPassMsg
     * @return
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
     * 1:1图片比对
     * 
     * @param imageBase64_1
     * @param imageBase64_2
     * @param threshold
     * @param notPassMsg
     * @return
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
            String msg = MessageUtils.message("person.face.recog.one.match.one.threshold.configure", SysConfigConstants.BASEDATA_FACE_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.face.recog.one.match.one.threshold.need.number", SysConfigConstants.BASEDATA_FACE_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 人脸1:1认证
     * 
     * @param featureBeanList
     * @return
     */
    @Override
    public List<MatchBean> faceOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long faceMatchStart = System.currentTimeMillis();
            log.info("Face picture 1:1 comparison algorithm call starts:{}", faceMatchStart);
            List<MatchBean> matchBeanList =
                bioFaceMicroService.faceMatch(null, null, new ArrayList<>(), featureBeanList);
            long faceMatchEnd = System.currentTimeMillis();
            log.info("Face picture 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", faceMatchStart, faceMatchEnd,
                faceMatchEnd - faceMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] to compare faces timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] to match face error", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.match.error", msg));
        }
    }

    /**
     * 查询人脸入库是否进行1-N校验
     * 
     * @return
     */
    @Override
    public boolean getFaceAddIsValidateN() {
        // 平台没有配置1-N功能，则直接就返回不校验
        if (!getPlatformIsOpenSearchN()) {
            return false;
        }
        // 平配置了1-N功能，则再查询是否入库进行校验
        String validConfig = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_ADD_ISVALIDN_KEY);
        // 不进行参数配置则默认进行1：N校验
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    /**
     * 人脸入库图像上传
     * 
     * @param encrypted
     * @param fileName
     * @param imageBase64
     * @param baseDir
     * @return
     */
    @Override
    public String uploadFaceImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传人脸图片, 拓展名的获取需要在加密之前获取
        String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(imageBase64);
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }
        if (StringUtils.isBlank(baseDir)) {
            baseDir = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_DIR_KEY);
            if (StringUtils.isBlank(baseDir)) {
                String msg = MessageUtils.message("person.face.recog.upload.face.folder.need", SysConfigConstants.BASEDATA_FACE_DIR_KEY);
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
            log.error("the face image upload failed", e);
            throw new CustomException(MessageUtils.message("person.face.recog.upload.face.error"));
        }
    }

    /**
     * 人脸图片检活
     * 
     * @param imageBase64
     * @param threshold
     * @return
     */
    @Override
    public CheckLiveResponse checkLive(String imageBase64, Double threshold) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        Double thresholdScore = null == threshold ? getCheckLiveThreshold(false) : threshold;
        try {
            long faceDetectStart = System.currentTimeMillis();
            log.info("Face check live algorithm call starts:{}", faceDetectStart);
            CheckLiveResponse response =
                bioFaceMicroService.faceImageLivenessDetect(handleSeq, imageBase64, thresholdScore);
            long faceDetectEnd = System.currentTimeMillis();
            log.info("Face check live algorithm call ends,start:{},end:{},usedTime:{}ms,score:{},threshold:{},result:{}", faceDetectStart,
                faceDetectEnd, faceDetectEnd - faceDetectStart, response.getScore(), thresholdScore,
                response.getResult());
            return response;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_CHECKLIVE] timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_CHECKLIVE] error", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.error", msg));
        }
    }

    /**
     * 人脸视频检活
     * 
     * @param videoBase64
     * @param threshold
     * @return
     */
    @Override
    public CheckLiveResponse videoCheckLive(String videoBase64, Double threshold) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        Double thresholdScore = null == threshold ? getCheckLiveThreshold(true) : threshold;
        try {
            long faceDetectStart = System.currentTimeMillis();
            log.info("Face video detection algorithm call starts,start:{}", faceDetectStart);
            CheckLiveResponse response =
                bioFaceMicroService.faceVideoLivenessDetection(handleSeq, videoBase64, thresholdScore);
            long faceDetectEnd = System.currentTimeMillis();
            log.info("Face video detection algorithm call ends,start:{},end:{},usedTime:{}ms,score:{},threshold:{},result:{}", faceDetectStart,
                faceDetectEnd, faceDetectEnd - faceDetectStart, response.getScore(), thresholdScore,
                response.getResult());
            return response;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_CHECKLIVE] timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.video.checklive.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_CHECKLIVE] error", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.video.checklive.error", msg));
        }
    }

    /**
     * 获取人脸检活阈值参数
     * 
     * @param isVideo
     * @return
     */
    @Override
    public double getCheckLiveThreshold(boolean isVideo) {
        String key = isVideo ? SysConfigConstants.BASEDATA_FACE_VIDEO_CHECKLIVE_THRESHOLD_KEY
            : SysConfigConstants.BASEDATA_FACE_CHECKLIVE_THRESHOLD_KEY;
        String thresholdStr = configService.selectConfigByKey(key);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.threshold.configure", key));
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            throw new CustomException(MessageUtils.message("person.face.recog.face.checklive.threshold.need.number", key));
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 1-N搜索
     * 
     * @param feature
     * @param channelCode
     * @param topN
     * @param threshold
     * @return
     */
    @Override
    public List<FaceSearchResult> faceSearchN(String feature, String channelCode, Integer topN, Double threshold) {
        // 平台没有开启1-N功能，直接返回空列表
        if (!getPlatformIsOpenSearchN()) {
            return Collections.emptyList();
        }
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString(); // 生成流水号
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
            long faceSearchStart = System.currentTimeMillis();
            log.info("Face 1-N search algorithm call starts,handleSeq:[{}], start:[{}],tenantId:[{}], libId:[{}], threshold:[{}]", handleSeq,
                faceSearchStart, tenantId, realLibId, threshold);
            List<FaceSearchResult> searchResultList =
                bioFaceMicroService.faceMiniSearch("", handleSeq, feature, realLibId, topN, thresholdScore);
            long faceSeachEnd = System.currentTimeMillis();
            log.info("Face 1-N search algorithm call ends,handleSeq:[{}], start:[{}],end:[{}],usedTime:[{}]ms", handleSeq, faceSearchStart,
                faceSeachEnd, faceSeachEnd - faceSearchStart);
            if (CollectionUtils.isNotEmpty(searchResultList)) {
                searchResultList = searchResultList.stream()
                    .sorted((e1, e2) -> Double.valueOf(e2.getScore()).compareTo(e1.getScore())).map(it -> {
                        log.info("Face 1-N search algorithm calls search results,handleSeq:[{}], tenantId:[{}], uniqueId:[{}]", handleSeq, tenantId,
                            it.getUserId());
                        // 搜索出来的用户标识去除租户信息
                        if (it.getUserId().indexOf("#") != -1) {
                            it.setUserId(it.getUserId().substring(it.getUserId().indexOf("#") + 1));
                        }
                        return it;
                    }).collect(Collectors.toList());
            }
            return searchResultList;
        } catch (TimeoutException e) {
            log.error("invoke [FOX_MINISEARCH] to search timeout", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.1n.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [FOX_MINISEARCH] to search error,", e);
            throw new CustomException(MessageUtils.message("person.face.recog.face.1n.error", msg));
        }
    }

    /**
     * 获取多人脸特征
     * 
     * @param imageBase64
     * @return
     */
    @Override
    public List<FeatureBean> getMultiPersonFaceFeature(String imageBase64) {
        FaceExtractResult faceExtractResult = getFaceExtractResult(imageBase64);
        if (CollectionUtils.isEmpty(faceExtractResult.getFeatures())) {
            return Collections.emptyList();
        }
        return faceExtractResult.getFeatures().parallelStream().map(f -> {
            FeatureBean imgFeature = new FeatureBean();
            imgFeature.setFeature(f);
            imgFeature.setType(String.valueOf(FeatureData.FeatureType.FaceFeature));
            return imgFeature;
        }).collect(Collectors.toList());
    }

    /**
     * 人脸入库是否进行活体检测
     */
    @Override
    public boolean getFaceAddIsCheckLive() {
        // 再查询是否入库进行活体检测
        String validConfig = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_ADD_CHECKLIVE_KEY);
        // 不进行参数配置则默认进行活体校验
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param faceImgFile
     * @param srcFace
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFace execCheckAndUploadFace(MultipartFile faceImgFile, BasePersonFace srcFace,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        String imageBase64 = null == faceImgFile ? null : PlatformFileUtils.getImageBase64(faceImgFile);
        String fileName = null == faceImgFile ? null : faceImgFile.getOriginalFilename();
        return execCheckAndUploadFace(imageBase64, fileName, srcFace, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param fileModel
     * @param srcFace
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFace execCheckAndUploadFace(FileModel fileModel, BasePersonFace srcFace, String stockImgFeature,
        Boolean isValidN, boolean isUpdate) {
        String imageBase64 =
            null == fileModel ? null : PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
        String fileName = null == fileModel ? null : fileModel.getFileName();
        return execCheckAndUploadFace(imageBase64, fileName, srcFace, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param imageBase64
     * @param fileName
     * @param srcFace
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonFace execCheckAndUploadFace(String imageBase64, String fileName, BasePersonFace srcFace,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        FaceExtractResult faceExtractResult = null;
        String feature = srcFace.getFeature();
        Double qualityScore = null;
        if (StringUtils.isNotBlank(imageBase64)) {
            // 获取图像特征
            faceExtractResult = getFaceExtractResult(imageBase64);
            List<String> features = faceExtractResult.getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                log.error("No face detected in the picture, please upload a clear face picture");
                throw new CustomException(MessageUtils.message("person.face.recog.image.no.face"));
            }
            if (features.size() > 1) {
                log.error("The picture has detected multiple faces, please upload a single face picture");
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
        isValidN = null == isValidN ? getFaceAddIsValidateN() : isValidN;
        // 进行1：N校验
        if (isValidN) {
            List<FaceSearchResult> searchNResult = faceSearchN(feature, "", 1, null);
            String uniqueId = srcFace.getUniqueId();
            if (CollectionUtils.isNotEmpty(searchNResult) && !searchNResult.get(0).getUserId().equals(uniqueId)) {
                log.error("The face storage 1-N verification failed, it is not my face! uniqueId:{}", searchNResult.get(0).getUserId());
                throw new CustomException(MessageUtils.message("person.face.recog.face.1n.check.failed"));
            }
        }
        /********************************** 设置人脸信息属性 *********************************/
        BasePersonFace basePersonFace = new BasePersonFace();
        BeanUtils.copyBeanProp(basePersonFace, srcFace);// 拷贝已有属性信息
        basePersonFace.setAlgsVersion(faceExtractResult == null ? null : faceExtractResult.getAlgVersion());// 算法版本
        basePersonFace.setFeature(feature);// 图像特征
        basePersonFace.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        basePersonFace.setQualityScore(qualityScore); // 图像质量得分
        // 默认加密
        if (StringUtils.isBlank(basePersonFace.getEncrypted())) {
            basePersonFace.setEncrypted(DictConstants.Encrypted.ENABLE);// 是否加密，默认加密
        }
        if (StringUtils.isBlank(basePersonFace.getDatasource())) {
            basePersonFace.setDatasource(DictConstants.DataSource.INTERFACE);// 数据源，默认是接口
        }
        if (StringUtils.isBlank(basePersonFace.getStatus())) {
            basePersonFace.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(basePersonFace.getId())) {
            basePersonFace.setId(IdWorker.getNextStringId());
            basePersonFace.setCreateTime(DateUtils.getNowDate());
            basePersonFace.setCreateBy(loginName);
        } else {
            basePersonFace.setUpdateTime(DateUtils.getNowDate());
            basePersonFace.setUpdateBy(loginName);
        }
        /********************************** 设置人脸信息属性结束 *********************************/

        if (StringUtils.isBlank(imageBase64)) {
            basePersonFace.setImageUrl(null);
            return basePersonFace;
        }
        // 上传人脸图片
        boolean encrypted = StringUtils.isBlank(basePersonFace.getEncrypted())
            || DictConstants.Encrypted.ENABLE.equals(basePersonFace.getEncrypted());
        String filePathName = uploadFaceImg(encrypted, fileName, imageBase64, null);
        basePersonFace.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
        basePersonFace.setImageUrl(filePathName);// 设置图片路径
        return basePersonFace;
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
            String s = MessageUtils.message("person.face.recog.one.match.one.check.failed", score,thresholdScore);
            log.info(s);
            String defaultMsg =s;
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 获取人脸1-N搜索阈值
     * 
     * @return
     */
    private double getSearchNThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.face.recog.face.1n.threshold.configure", SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.face.recog.face.1n.threshold.need.number", SysConfigConstants.BASEDATA_FACE_SEARCH_N_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
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
