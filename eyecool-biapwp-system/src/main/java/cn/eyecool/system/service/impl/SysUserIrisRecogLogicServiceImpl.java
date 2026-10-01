package cn.eyecool.system.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eyecool.abis.callmicroservice.IBioIrisMicroService;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

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
import cn.eyecool.system.domain.SysUserIris;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysUserIrisRecogLogicService;
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
public class SysUserIrisRecogLogicServiceImpl implements ISysUserIrisRecogLogicService {
    private final AtomicLong requestSeqGen = new AtomicLong();
    private static final Logger LOGGER = LoggerFactory.getLogger(SysUserIrisRecogLogicServiceImpl.class);
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IBioIrisMicroService bioIrisMicroService;

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性
     */
    @Override
    public SysUserIris execCheckAndUploadIris(String loginName, String imageBase64, String fileName,
        SysUserIris srcIris, String stockImgFeature, Boolean isValidN, boolean isUpdate) {
        IrisExtractResult irisExtractResult = null;
        String feature = srcIris.getFeature();
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
        /********************************** 设置虹膜信息属性 *********************************/
        SysUserIris sysUserIris = new SysUserIris();
        BeanUtils.copyBeanProp(sysUserIris, srcIris);// 拷贝已有属性信息
        sysUserIris.setAlgsVersion(null == irisExtractResult ? null : irisExtractResult.getAlgVersion());// 算法版本
        sysUserIris.setFeature(feature);// 图像特征
        sysUserIris.setFeatureMd5(StringUtils.isBlank(feature) ? null : Md5Utils.hash(feature));
        if (StringUtils.isBlank(sysUserIris.getStatus())) {
            sysUserIris.setStatus(DictConstants.Status.ENABLE);// 状态，默认正常
        }

        if (StringUtils.isBlank(sysUserIris.getId())) {
            sysUserIris.setId(IdWorker.getNextStringId());
            sysUserIris.setCreateTime(DateUtils.getNowDate());
            sysUserIris.setCreateBy(loginName);
        } else {
            sysUserIris.setUpdateTime(DateUtils.getNowDate());
            sysUserIris.setUpdateBy(loginName);
        }
        /********************************** 设置虹膜信息属性结束 *********************************/

        if (StringUtils.isBlank(imageBase64)) {
            sysUserIris.setImageUrl(null);
            return sysUserIris;
        }
        // 上传虹膜图片
        String filePathName = uploadIrisImg(true, fileName, imageBase64, null);
        sysUserIris.setImageUrl(filePathName);
        return sysUserIris;
    }

    /**
     * 上传虹膜图像
     */
    @Override
    public String uploadIrisImg(boolean encrypted, String fileName, String imageBase64, String baseDir) {
        // 上传虹膜图片
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }

        if (StringUtils.isBlank(baseDir)) {
            baseDir = SystemConstants.SYS_USER_BIO_IMAGE_DIR.IRIS_DIR;
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
            LOGGER.error("Iris picture upload abnormally", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.upload.error"));
        }
    }

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
            LOGGER.info("The iris feature extraction algorithm call starts,start:{}", irsiExtractStart);
            IrisExtractResult irisExtractResult = bioIrisMicroService.extractIrisFeature(null, handleSeq, imageBase64);
            long irisExtractEnd = System.currentTimeMillis();
            LOGGER.info("The iris feature extraction algorithm call ends,start:{},end:{},usedTime:{}ms", irsiExtractStart, irisExtractEnd,
                irisExtractEnd - irsiExtractStart);
            return irisExtractResult;
        } catch (TimeoutException e) {
            LOGGER.error("Timeout calling [ABIS_MATCH] to extract iris features", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.extract.feature.time"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Call [ABIS_MATCH] to extract abnormal iris features", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.extract.feature.error",msg));
        }
    }

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
                throw new CustomException(MessageUtils.message("person.iris.recog.repeat.threshold.configure",SysConfigConstants.BASEDATA_IRIS_REPEAT_THRESHOLD_KEY));
            }
            try {
                thresholdScore = Double.valueOf(thresholdStr);
            } catch (Exception e) {
                throw new CustomException(MessageUtils.message("person.iris.recog.repeat.threshold.need.number",SysConfigConstants.BASEDATA_IRIS_REPEAT_THRESHOLD_KEY));
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
                    LOGGER.info("There are duplicate iris images");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取FeatureBean
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64) {
        return getFeatureBean(imgBase64, null, null);
    }

    /**
     * 获取FeatureBean
     */
    @Override
    public FeatureBean getFeatureBean(String imgBase64, String emptyIrisMsg, String multiIrisMsg) {
        IrisExtractResult irisExtractResult = getIrisExtractResult(imgBase64);
        return getFeatureBean(irisExtractResult, emptyIrisMsg, multiIrisMsg);
    }

    /**
     * 获取FeatureBean
     */
    @Override
    public FeatureBean getFeatureBean(IrisExtractResult irisExtractResult, String emptyIrisMsg, String multiIrisMsg) {
        List<String> features = irisExtractResult.getFeatures();
        if (CollectionUtils.isEmpty(features)) {
            String defaultMsg =MessageUtils.message("person.iris.recog.image.no.iris");
            LOGGER.error(defaultMsg);
            String msg = StringUtils.isBlank(emptyIrisMsg) ? defaultMsg : emptyIrisMsg;
            throw new CustomException(msg);
        }
        if (features.size() > 1) {
            String defaultMsg =MessageUtils.message("person.iris.recog.image.multiple.iris");
            LOGGER.error(defaultMsg);
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
            throw new CustomException(MessageUtils.message("person.iris.recog.one.match.one.threshold.configure",SysConfigConstants.BASEDATA_IRIS_COMPARE_THRESHOLD_KEY));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("person.iris.recog.one.match.one.threshold.need.number",SysConfigConstants.BASEDATA_IRIS_COMPARE_THRESHOLD_KEY));
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
            String defaultMsg =MessageUtils.message("person.iris.recog.one.match.one.check.failed", score, thresholdScore);
            LOGGER.info(defaultMsg);
            String msg = StringUtils.isBlank(notPassMsg) ? defaultMsg : notPassMsg;
            throw new CustomException(msg);
        }
        return score;
    }

    /**
     * 虹膜图片1:1认证
     */
    @Override
    public List<MatchBean> irisOne2OneCompare(List<FeatureBean> featureBeanList) {
        try {
            long irisMatchStart = System.currentTimeMillis();
            LOGGER.info("The iris picture 1:1 comparison algorithm call starts,start:{}", irisMatchStart);
            List<MatchBean> matchBeanList =
                bioIrisMicroService.irisMatch(null, null, new ArrayList<>(), featureBeanList);
            long irisrMatchEnd = System.currentTimeMillis();
            LOGGER.info("The iris image 1:1 comparison algorithm call ends,start:{},end:{},usedTime:{}ms", irisMatchStart, irisrMatchEnd,
                irisrMatchEnd - irisMatchStart);
            return matchBeanList;
        } catch (TimeoutException e) {
            LOGGER.error("Call [ABIS_MATCH] to match iris timed out", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.match.timeout"));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() instanceof StatusRuntimeException) {
                msg = e.getCause().getMessage();
            }
            LOGGER.error("Call [ABIS_MATCH] to compare iris exception", e);
            throw new CustomException(MessageUtils.message("person.iris.recog.match.error", msg));
        }
    }
}
