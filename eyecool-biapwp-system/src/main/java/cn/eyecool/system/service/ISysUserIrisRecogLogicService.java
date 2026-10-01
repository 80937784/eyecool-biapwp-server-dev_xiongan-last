package cn.eyecool.system.service;

import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.system.domain.SysUserIris;

import java.util.List;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.service
 * @Description: TODO
 * @date Date : 2021年01月19日 上午9:02
 */
public interface ISysUserIrisRecogLogicService {


    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息
     * 如果是用作更新，源对象需要传入ID属性
     * 传入底库照片特征则进行1:1比对，不传入则不比对
     *
     * @param imageBase64     虹膜图片Base64
     * @param fileName        图片名称
     * @param srcIris         虹膜信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN        是否进行1-N校验
     * @param isUpdate        是否是用于更新 true更新 false新增
     * @return 设置好属性的虹膜信息
     */
    public SysUserIris execCheckAndUploadIris(String loginName, String imageBase64, String fileName, SysUserIris srcIris, String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 根据虹膜图片base64进行特征提取
     *
     * @param imageBase64 图片
     * @return IrisExtractResult
     */
    public IrisExtractResult getIrisExtractResult(String imageBase64);

    /**
     * 校验是否有重复虹膜
     *
     * @param featureList
     * @param threshold
     * @return
     */
    public boolean checkHasRepeatIris(List<String> featureList, Double threshold);

    public FeatureBean getFeatureBean(String imgBase64);

    public FeatureBean getFeatureBean(String imgBase64, String emptyIrisMsg, String multiIrisMsg);

    public FeatureBean getFeatureBean(IrisExtractResult irisExtractResult, String emptyIrisMsg, String multiIrisMsg);

    public double irisOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    public double getOne2OneCompareThreshold();

    public List<MatchBean> irisOne2OneCompare(List<FeatureBean> featureBeanList);

    public String uploadIrisImg(boolean encrypted, String fileName, String imageBase64, String baseDir);
}
