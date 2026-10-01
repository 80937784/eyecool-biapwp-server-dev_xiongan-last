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

import com.eyecool.abis.callmicroservice.IBioIrisMicroService;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;
import com.eyecool.abis.callmicroservice.common.IrisSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
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
 * 虹膜识别、比对逻辑实现
 * 
 * @author admin
 * @date 2019年10月24日
 */
@Service
@Slf4j
public class PersonIrisRecogLogicServiceImpl implements IPersonIrisRecogLogicService {

    // 序列号原子对象
    private final AtomicLong requestSeqGen = new AtomicLong();

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioIrisMicroService bioIrisMicroService;

    /**
     * 获取虹膜特征
     * 
     * @param imageBase64
     * @return
     */
    @Override
    public IrisExtractResult getIrisExtractResult(String imageBase64) {
        String handleSeq = new UUID(System.currentTimeMillis(), requestSeqGen.incrementAndGet()).toString();// 生成流水号
        try {
            // 虹膜特征提取(参数：节点，平台流水[便于跟踪日志],图片的base64)
            long irsiExtractStart = System.currentTimeMillis();
            log.info("The iris feature extraction algorithm call starts,start:{}", irsiExtractStart);
            IrisExtractResult irisExtractResult = bioIrisMicroService.extractIrisFeature(null, handleSeq, imageBase64);
            long irisExtractEnd = System.currentTimeMillis();
            log.info("The iris feature extraction algorithm call ends,start:{},end:{},usedTime:{}ms", irsiExtractStart, irisExtractEnd,
                irisExtractEnd - irsiExtractStart);
            return irisExtractResult;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] to extract feature timeout", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.extract.feature.time"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] to extract feature error", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.extract.feature.error", msg));
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
     * @param emptyIrisMsg
     * @param multiIrisMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyIrisMsg, String multiIrisMsg) {
        IrisExtractResult irisExtractResult = getIrisExtractResult(imgBase64);
        return getFeatureBean(irisExtractResult, emptyIrisMsg, multiIrisMsg);
    }

    /**
     * 获取FeatureBean
     * 
     * @param irisExtractResult
     * @param emptyIrisMsg
     * @param multiIrisMsg
     * @return
     */
    @Override
    public FeatureBean getFeatureBean(IrisExtractResult irisExtractResult, String emptyIrisMsg, String multiIrisMsg) {
        List<String> features = irisExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            String defaultMsg = MessageUtils.message("person.iris.recog.image.no.iris");
            log.error(defaultMsg);
            String msg = StringUtils.isBlank(emptyIrisMsg) ? defaultMsg : emptyIrisMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            String defaultMsg = MessageUtils.message("person.iris.recog.image.multiple.iris");
            log.error(defaultMsg);
            String msg = StringUtils.isBlank(multiIrisMsg) ? defaultMsg : multiIrisMsg;
            throw new CustomException(msg);
        }
        FeatureBean imgFeature = new FeatureBean();
        imgFeature.setFeature(features.get(0));
        imgFeature.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
        return imgFeature;
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
    public double irisOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg) {
        Double thresholdScore = threshold;
        if (null == threshold) {
            thresholdScore = getOne2OneCompareThreshold();
        }
        List<FeatureBean> featureBeanList = new ArrayList<>();
        featureBeanList.add(getFeatureBean(imageBase64_1));
        featureBeanList.add(getFeatureBean(imageBase64_2));
        return irisOne2OneCompare(featureBeanList, thresholdScore, notPassMsg);
    }

    /**
     * 获取1:1比对阈值参数
     * 
     * @return
     */
    @Override
    public double getOne2OneCompareThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_IRIS_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.iris.recog.one.match.one.threshold.configure", SysConfigConstants.BASEDATA_IRIS_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.iris.recog.one.match.one.threshold.need.number", SysConfigConstants.BASEDATA_IRIS_COMPARE_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        return Double.valueOf(thresholdStr);
    }

    /**
     * 虹膜图片1:1认证
     * 
     * @param featureBeanList
     * @return
     */
    @Override
    public List<MatchBean> irisOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long irisMatchStart = System.currentTimeMillis();
            log.info("The iris image 1:1 comparison algorithm call starts,start:{}", irisMatchStart);
            List<MatchBean> matchBeanList =
                bioIrisMicroService.irisMatch(null, null, new ArrayList<>(), featureBeanList);
            long irisrMatchEnd = System.currentTimeMillis();
            log.info("The iris image 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", irisMatchStart, irisrMatchEnd,
                irisrMatchEnd - irisMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            log.error("invoke [ABIS_MATCH] to match iris timeout", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [ABIS_MATCH] to match iris error", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.match.error",  msg));
        }
    }

    /**
     * 查询虹膜入库是否进行1-N校验
     * 
     * @return
     */
    @Override
    public boolean getIrisAddIsValidateN() {
        // 平台没有配置1-N功能，则直接就返回不校验
        if (!getPlatformIsOpenSearchN()) {
            return false;
        }
        // 平配置了1-N功能，则再查询是否入库进行校验
        String validConfig = configService.selectConfigByKey(SysConfigConstants.BASEDATA_IRIS_ADD_ISVALIDN_KEY);
        // 不进行参数配置则默认进行1：N校验
        return null == validConfig ? true : DictConstants.YesOrNoState.YES.equalsIgnoreCase(validConfig);
    }

    /**
     * 上传入库虹膜图像
     * 
     * @param encrypted
     * @param fileName
     * @param imageBase64
     * @param baseDir
     * @return
     */
    @Override
    public String uploadIrisImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传虹膜图片, 拓展名的获取需要在加密之前获取
        String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(imageBase64);
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }

        if (StringUtils.isBlank(baseDir)) {
            baseDir = configService.selectConfigByKey(SysConfigConstants.BASEDATA_IRIS_DIR_KEY);
            if (StringUtils.isBlank(baseDir)) {
                String msg  =  MessageUtils.message("person.iris.recog.upload.iris.folder.need", SysConfigConstants.BASEDATA_IRIS_DIR_KEY );
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
            log.error("upload the iris image error", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.upload.error"));
        }
    }

    /**
     * 校验是否有重复虹膜
     * 
     * @param featureList
     * @param threshold
     * @return
     */
    @Override
    public boolean checkHasRepeatIris(List<String> featureList, Double threshold) {
        if (CollectionUtils.isEmpty(featureList) || featureList.size() <= 1) {
            return false;
        }
        Double thresholdScore = threshold;
        if (null == threshold) {
            String thresholdStr =
                configService.selectConfigByKey(SysConfigConstants.BASEDATA_IRIS_REPEAT_THRESHOLD_KEY);
            if (StringUtils.isBlank(thresholdStr)) {
                String msg = MessageUtils.message("person.iris.recog.repeat.threshold.configure", SysConfigConstants.BASEDATA_IRIS_REPEAT_THRESHOLD_KEY );
                throw new CustomException(msg);
            }
            try {
                thresholdScore = Double.valueOf(thresholdStr);
            } catch (Exception e) {
                String msg = MessageUtils.message("person.iris.recog.repeat.threshold.need.number", SysConfigConstants.BASEDATA_IRIS_REPEAT_THRESHOLD_KEY );
                throw new CustomException(msg);
            }
        }
        List<FeatureBean> featureBeanList = featureList.stream().map(it -> {
            FeatureBean imgFeature = new FeatureBean();
            imgFeature.setFeature(it);
            imgFeature.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
            return imgFeature;
        }).collect(Collectors.toList());
        List<MatchBean> matchBeanList = irisOne2OneCompare(featureBeanList);
        for (int i = 0; i < featureList.size(); i++) {
            for (int j = 0; j < featureList.size() - i - 1; j++) {
                double score = matchBeanList.get(i).getResults().get(j).getScore();
                if (thresholdScore < score) {
                    log.info("There are duplicate iris images");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 虹膜1-N搜索
     * 
     * @param feature
     * @param channelCode
     * @param topN
     * @param threshold
     * @return
     */
    @Override
    public List<IrisSearchResult> irisSearchN(String feature, String channelCode, Integer topN, Double threshold) {
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
            log.info("The iris 1-N search algorithm call starts,start:{}", fingerSearchStart);
            List<IrisSearchResult> searchResultList = bioIrisMicroService.irisMiniSearch("", handleSeq, feature,
                getEyeFlag(null), realLibId, topN, thresholdScore);
            long fingerSeachEnd = System.currentTimeMillis();
            log.info("The iris 1-N search algorithm call ends,start:{},end:{},usedTime:{}ms", fingerSearchStart, fingerSeachEnd,
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
            log.error("invoke [FOX_MINISEARCH] to search the iris timeout", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.1n.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            log.error("invoke [FOX_MINISEARCH] to search iris error", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.1n.error",msg));
        }
    }

    /**
     * 提取多虹膜特征
     * 
     * @param sceneImage
     * @return
     */
    @Override
    public List<FeatureBean> getMultiPersonIrisFeature(String sceneImage) {
        IrisExtractResult extractResult = getIrisExtractResult(sceneImage);
        if (CollectionUtils.isEmpty(extractResult.getFeatures())) {
            return Collections.emptyList();
        }
        return extractResult.getFeatures().parallelStream().map(f -> {
            FeatureBean imgFeature = new FeatureBean();
            imgFeature.setFeature(f);
            imgFeature.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
            return imgFeature;
        }).collect(Collectors.toList());
    }

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param irisImgFile
     * @param srcIris
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonIris execCheckAndUploadIris(MultipartFile irisImgFile, BasePersonIris srcIris,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        String imageBase64 = null == irisImgFile ? null : PlatformFileUtils.getImageBase64(irisImgFile);
        String fileName = null == irisImgFile ? null : irisImgFile.getOriginalFilename();
        return execCheckAndUploadIris(imageBase64, fileName, srcIris, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param fileModel
     * @param srcIris
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonIris execCheckAndUploadIris(FileModel fileModel, BasePersonIris srcIris, String stockImgFeature,
        Boolean isValidN, boolean isUpdate) {
        String imageBase64 =
            null == fileModel ? null : PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
        String fileName = null == fileModel ? null : fileModel.getFileName();
        return execCheckAndUploadIris(imageBase64, fileName, srcIris, stockImgFeature, isValidN, isUpdate);
    }

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性
     * 
     * @param imageBase64
     * @param fileName
     * @param srcIris
     * @param stockImgFeature
     * @param isValidN
     * @param isUpdate
     * @return
     */
    @Override
    public BasePersonIris execCheckAndUploadIris(String imageBase64, String fileName, BasePersonIris srcIris,
        String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        IrisExtractResult irisExtractResult = null;
        String feature = srcIris.getFeature();
        Double qualityScore = null;
        // 传入特征则不再提取
        if (StringUtils.isBlank(feature) && StringUtils.isNotBlank(imageBase64)) {
            // 获取上传虹膜图片特征
            irisExtractResult = getIrisExtractResult(imageBase64);
            List<String> features = irisExtractResult.getFeatures();
            if (CollectionUtils.isEmpty(features)) {
                throw new CustomException(MessageUtils.message("person.iris.recog.image.no.iris"));
            }
            if (features.size() > 1) {
                throw new CustomException(MessageUtils.message("person.iris.recog.image.multiple.iris"));
            }
            feature = features.get(0);
        }
        // 进行1:1比对
        if (isUpdate && StringUtils.isNotBlank(stockImgFeature)) {
            List<FeatureBean> featureBeanList = new ArrayList<>();
            FeatureBean featureBean = new FeatureBean();
            featureBean.setFeature(feature);
            featureBean.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
            FeatureBean stockImgFeatureBaan = new FeatureBean();
            stockImgFeatureBaan.setFeature(stockImgFeature);
            stockImgFeatureBaan.setType(String.valueOf(FeatureData.FeatureType.IrisFeatureUnknown));
            featureBeanList.add(featureBean);
            featureBeanList.add(stockImgFeatureBaan);
            irisOne2OneCompare(featureBeanList, getOne2OneCompareThreshold(), null);
        }
        isValidN = null == isValidN ? getIrisAddIsValidateN() : isValidN;
        // 进行1：N校验
        if (isValidN) {
            List<IrisSearchResult> searchNResult = irisSearchN(feature, StringUtils.EMPTY, 1, null);
            String uniqueId = srcIris.getUniqueId();
            if (CollectionUtils.isNotEmpty(searchNResult) && !searchNResult.get(0).getUserId().equals(uniqueId)) {
                throw new CustomException(MessageUtils.message("person.iris.recog.1n.check.failed"));
            }
        }
        /********************************** 设置虹膜信息属性 *********************************/
        BasePersonIris basePersonIris = new BasePersonIris();
        BeanUtils.copyBeanProp(basePersonIris, srcIris);// 拷贝已有属性信息
        basePersonIris.setAlgsVersion(null == irisExtractResult ? null : irisExtractResult.getAlgVersion());// 算法版本
        basePersonIris.setFeature(feature);// 图像特征
        basePersonIris.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        basePersonIris.setQualityScore(qualityScore); // 图像质量得分
        // 默认加密
        if (StringUtils.isBlank(basePersonIris.getEncrypted())) {
            basePersonIris.setEncrypted(DictConstants.Encrypted.ENABLE);// 是否加密，默认加密
        }
        if (StringUtils.isBlank(basePersonIris.getDatasource())) {
            basePersonIris.setDatasource(DictConstants.DataSource.INTERFACE);// 数据源，默认是接口
        }
        if (StringUtils.isBlank(basePersonIris.getStatus())) {
            basePersonIris.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(basePersonIris.getId())) {
            basePersonIris.setId(IdWorker.getNextStringId());
            basePersonIris.setCreateTime(DateUtils.getNowDate());
            basePersonIris.setCreateBy(loginName);
        } else {
            basePersonIris.setUpdateTime(DateUtils.getNowDate());
            basePersonIris.setUpdateBy(loginName);
        }
        /********************************** 设置虹膜信息属性结束 *********************************/

        if (StringUtils.isBlank(imageBase64)) {
            basePersonIris.setImageUrl(null);
            return basePersonIris;
        }
        // 上传虹膜图片
        boolean encrypted = StringUtils.isBlank(basePersonIris.getEncrypted())
            || DictConstants.Encrypted.ENABLE.equals(basePersonIris.getEncrypted());
        basePersonIris.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
        String filePathName = uploadIrisImg(encrypted, fileName, imageBase64, null);
        basePersonIris.setImageUrl(filePathName);
        return basePersonIris;
    }

    /**
     * 根据眼睛编码返回左右眼标记
     * 
     * @param eyeCode
     * @return
     */
    private String getEyeFlag(String eyeCode) {
        if (StringUtils.isBlank(eyeCode)) {
            return "0";
        }
        switch (eyeCode) {
            case DictConstants.EyeCode.RIGHT_EYE:
                return "1";
            case DictConstants.EyeCode.LEFT_EYE:
                return "2";
            default:
                return "0";
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
    private double irisOne2OneCompare(List<FeatureBean> featureBeanList, double thresholdScore, String notPassMsg) {
        List<MatchBean> matchBeanList = irisOne2OneCompare(featureBeanList);
        double score = matchBeanList.get(0).getResults().get(0).getScore();
        if (thresholdScore > score) {
            String defaultMsg =MessageUtils.message("person.iris.recog.one.match.one.check.failed",score,thresholdScore);
            log.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 获取虹膜1-N搜索阈值
     * 
     * @return
     */
    private double getSearchNThreshold() {
        String thresholdStr = configService.selectConfigByKey(SysConfigConstants.BASEDATA_IRIS_SEARCH_N_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            String msg = MessageUtils.message("person.iris.recog.1n.threshold.configure", SysConfigConstants.BASEDATA_IRIS_SEARCH_N_THRESHOLD_KEY);
            throw new CustomException(msg);
        }
        if (!DoubleValidator.getInstance().isValid(thresholdStr)) {
            String msg = MessageUtils.message("person.iris.recog.1n.threshold.need.number", SysConfigConstants.BASEDATA_IRIS_SEARCH_N_THRESHOLD_KEY);
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
