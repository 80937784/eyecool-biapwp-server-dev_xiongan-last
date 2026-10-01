package cn.eyecool.basedata.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.FingerSearchResult;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.common.utils.file.FileModel;

/**
 * 指纹识别、比对逻辑接口
 * 
 * @author admin
 * @date 2019年10月24日
 */
public interface IPersonFingerRecogLogicService {

    /**
     * 根据指纹图片base64进行特征提取
     * 
     * @param imageBase64 图片
     * @return FingerExtractResult
     */
    public FingerExtractResult getFingerExtractResult(String imageBase64);

    /**
     * 根据指纹base64获取特征
     * 
     * @param imgBase64 图片
     * @return FeatureBean
     */
    public FeatureBean getFeatureBean(String imgBase64);

    /**
     * 根据指纹base64获取特征
     * 
     * @param imgBase64 imgBase64 图片
     * @param emptyFingerMsg 检测不到指纹提示信息
     * @param multiFingerMsg 检测到多指纹提示信息
     * @return
     */
    public FeatureBean getFeatureBean(String imgBase64, String emptyFingerMsg, String multiFingerMsg);

    /**
     * 根据FingerExtractResult获取特征
     * 
     * @param fingerExtractResult
     * @param emptyFingerMsg 检测不到指纹提示信息
     * @param multiFingerMsg 检测到多指纹提示信息
     * @return
     */
    public FeatureBean getFeatureBean(FingerExtractResult fingerExtractResult, String emptyFingerMsg,
        String multiFingerMsg);

    /**
     * 进行指纹图片质量检测
     * 
     * @param imageBase64 图片
     * @param threshold 阈值
     * @return 分值
     */
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg);

    /**
     * 指纹图片1:1比对
     * 
     * @param imageBase64_1 图片Base64
     * @param imageBase64_2 图片Base64
     * @param threshold 比对阈值,可以为空
     * @param notPassMsg 比对不通过提示信息,可以为空
     * @return 比对分值
     */
    public double fingerOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    /**
     * 指纹图片1:1认证
     * 
     * @param featureBeanList
     * @return
     */
    public List<MatchBean> fingerOne2OneCompare(List<FeatureBean> featureBeanList);

    /**
     * 上传入库指纹图像
     * 
     * @param encrypted 是否加密
     * @param fileName 文件名称
     * @param imageBase64 文件base64
     * @param baseDir
     * @return
     */
    public String uploadFingerImg(boolean encrypted, String fileName, String imageBase64, String baseDir);

    /**
     * 校验是否有重复手指
     * 
     * @param featureList
     * @param threshold
     * @return
     */
    public boolean checkHasRepeatFinger(List<String> featureList, Double threshold);

    /**
     * 1-N搜索
     * 
     * @param feature 指纹特征
     * @param channelCode 搜索库
     * @param topN 返回数据条数
     * @param threshold 搜索阈值
     * @return
     */
    public List<FingerSearchResult> fingerSearchN(String feature, String channelCode, Integer topN, Double threshold);

    /**
     * 查询指纹入库是否进行1-N校验
     * 
     * @return
     */
    public boolean getFingerAddIsValidateN();

    /**
     * 提取多指纹特征
     * 
     * @param sceneImage
     * @return
     */
    public List<FeatureBean> getMultiPersonFingerFeature(String sceneImage);

    /**
     * 获取系统指纹1:1比对阈值
     * 
     * @return
     */
    public double getOne2OneCompareThreshold();

    /**
     * 获取系统质量检测阈值
     * 
     * @return
     */
    public double getDetectThreshold();

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param fingerImgFile 指纹图片
     * @param srcfinger 指纹信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的指纹信息
     * @date 2019年10月24日
     *
     */
    public BasePersonFinger execCheckAndUploadFinger(MultipartFile fingerImgFile, BasePersonFinger srcFinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param fileModel 指纹图片信息对象
     * @param srcfinger 指纹信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的指纹信息
     * @date 2019年10月24日
     *
     */
    public BasePersonFinger execCheckAndUploadFinger(FileModel fileModel, BasePersonFinger srcFinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 执行指纹特征获取、质量检测和图片上传，返回指纹信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @param imageBase64 指纹图片Base64
     * @param fileName 图片名称
     * @param srcfinger 指纹信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的指纹信息
     */
    public BasePersonFinger execCheckAndUploadFinger(String imageBase64, String fileName, BasePersonFinger srcFinger,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

}
