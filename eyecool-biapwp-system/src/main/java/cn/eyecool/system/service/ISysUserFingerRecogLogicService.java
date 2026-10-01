package cn.eyecool.system.service;

import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.system.domain.SysUserFinger;

import java.util.List;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.service
 * @Description: TODO
 * @date Date : 2021年01月19日 上午9:01
 */
public interface ISysUserFingerRecogLogicService {
    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息
     * 如果是用作更新，源对象需要传入ID属性
     * 传入底库照片特征则进行1:1比对，不传入则不比对
     *
     * @param imageBase64     指纹图片Base64
     * @param fileName        图片名称
     * @param srcFinger       指纹信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN        是否进行1-N校验
     * @param isUpdate        是否是用于更新 true更新 false新增
     * @return 设置好属性的指纹信息
     */
    public SysUserFinger execCheckAndUploadFinger(String loginName, String imageBase64, String fileName, SysUserFinger srcFinger, String stockImgFeature, Boolean isValidN, boolean isUpdate);

    public FingerExtractResult getFingerExtractResult(String imageBase64);

    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg);

    public String uploadFingerImg(boolean encrypted, String fileName, String imageBase64, String baseDir);

    public double fingerOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    public List<MatchBean> fingerOne2OneCompare(List<FeatureBean> featureBeanList);

    public double getOne2OneCompareThreshold();

    public FeatureBean getFeatureBean(String imgBase64);

    public FeatureBean getFeatureBean(String imgBase64, String emptyFingerMsg, String multiFingerMsg);

    public FeatureBean getFeatureBean(FingerExtractResult fingerExtractResult, String emptyFingerMsg, String multiFingerMsg);

    public double getDetectThreshold();

    public boolean checkHasRepeatFinger(List<String> featureList, Double threshold);

}
