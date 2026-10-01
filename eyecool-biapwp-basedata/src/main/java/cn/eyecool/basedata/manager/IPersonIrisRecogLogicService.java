package cn.eyecool.basedata.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;
import com.eyecool.abis.callmicroservice.common.IrisSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.common.utils.file.FileModel;

/**
 * 虹膜识别、比对逻辑接口
 * 
 * @author admin
 * @date 2019年10月24日
 */
public interface IPersonIrisRecogLogicService {

    /**
     * 根据虹膜图片base64进行特征提取
     * 
     * @param imageBase64 图片
     * @return IrisExtractResult
     */
    public IrisExtractResult getIrisExtractResult(String imageBase64);

    /**
     * 根据虹膜base64获取特征
     * 
     * @param imgBase64 图片
     * @return FeatureBean
     */
    public FeatureBean getFeatureBean(String imgBase64);

    /**
     * 根据虹膜base64获取特征
     * 
     * @param imgBase64 imgBase64 图片
     * @param emptyIrisMsg 检测不到虹膜提示信息
     * @param multiIrisMsg 检测到多虹膜提示信息
     * @return
     */
    public FeatureBean getFeatureBean(String imgBase64, String emptyIrisMsg, String multiIrisMsg);

    /**
     * 根据IrisExtractResult获取特征
     * 
     * @param irisExtractResult
     * @param emptyIrisMsg 检测不到人脸提示信息
     * @param multiIrisMsg 检测到多人脸提示信息
     * @return
     */
    public FeatureBean getFeatureBean(IrisExtractResult irisExtractResult, String emptyIrisMsg, String multiIrisMsg);

    /**
     * 虹膜图片1:1比对
     * 
     * @param imageBase64_1 图片Base64
     * @param imageBase64_2 图片Base64
     * @param threshold 比对阈值,可以为空
     * @param notPassMsg 比对不通过提示信息,可以为空
     * @return 比对分值
     */
    public double irisOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    /**
     * 虹膜图片1:1认证
     * 
     * @param featureBeanList
     * @return
     */
    public List<MatchBean> irisOne2OneCompare(List<FeatureBean> featureBeanList);

    /**
     * 上传入库虹膜图像
     * 
     * @param encrypted 是否加密
     * @param fileName 文件名称
     * @param imageBase64 文件base64
     * @param baseDir
     * @return
     */
    public String uploadIrisImg(boolean encrypted, String fileName, String imageBase64, String baseDir);

    /**
     * 校验是否有重复虹膜
     * 
     * @param featureList
     * @param threshold
     * @return
     */
    public boolean checkHasRepeatIris(List<String> featureList, Double threshold);

    /**
     * 1-N搜索
     * 
     * @param feature 虹膜特征
     * @param channelCode 搜索库
     * @param topN 返回数据条数
     * @param threshold 搜索阈值
     * @return
     */
    public List<IrisSearchResult> irisSearchN(String feature, String channelCode, Integer topN, Double threshold);

    /**
     * 查询虹膜入库是否进行1-N校验
     * 
     * @return
     */
    public boolean getIrisAddIsValidateN();

    /**
     * 提取多虹膜特征
     * 
     * @param sceneImage
     * @return
     */
    public List<FeatureBean> getMultiPersonIrisFeature(String sceneImage);

    /**
     * 查询系统虹膜1:1比对阈值
     * 
     * @return
     */
    public double getOne2OneCompareThreshold();

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param irisImgFile 虹膜图片
     * @param srcIris 虹膜信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的虹膜信息
     * @date 2019年10月24日
     *
     */
    public BasePersonIris execCheckAndUploadIris(MultipartFile irisImgFile, BasePersonIris srcIris,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param fileModel 虹膜图片信息对象
     * @param srcfinger 虹膜信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的虹膜信息
     * @date 2019年10月24日
     *
     */
    public BasePersonIris execCheckAndUploadIris(FileModel fileModel, BasePersonIris srcIris, String stockImgFeature,
        Boolean isValidN, boolean isUpdate);

    /**
     * 执行虹膜特征获取、质量检测和图片上传，返回虹膜信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @param imageBase64 虹膜图片Base64
     * @param fileName 图片名称
     * @param srcfinger 虹膜信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的虹膜信息
     */
    public BasePersonIris execCheckAndUploadIris(String imageBase64, String fileName, BasePersonIris srcIris,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

}
