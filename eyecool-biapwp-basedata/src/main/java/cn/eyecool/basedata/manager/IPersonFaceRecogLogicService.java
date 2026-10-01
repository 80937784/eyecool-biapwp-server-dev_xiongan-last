package cn.eyecool.basedata.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.eyecool.abis.callmicroservice.common.FaceSearchResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.common.utils.file.FileModel;

/**
 * 人脸识别、比对逻辑服务接口
 * 
 * @author admin
 * @date 2019年10月24日
 */
public interface IPersonFaceRecogLogicService {

    /**
     * 根据人脸图片base64进行特征提取
     * 
     * @param imageBase64 图片
     * @return FaceExtractResult
     */
    public FaceExtractResult getFaceExtractResult(String imageBase64);

    /**
     * 根据人脸base64获取特征
     * 
     * @param imgBase64 图片
     * @return FeatureBean
     */
    public FeatureBean getFeatureBean(String imgBase64);

    /**
     * 根据人脸base64获取特征
     * 
     * @param imgBase64 imgBase64 图片
     * @param emptyFaceMsg 检测不到人脸提示信息
     * @param multiFaceMsg 检测到多人脸提示信息
     * @return FeatureBean
     */
    public FeatureBean getFeatureBean(String imgBase64, String emptyFaceMsg, String multiFaceMsg);

    /**
     * 根据FaceExtractResult获取特征
     * 
     * @param faceExtractResult
     * @param emptyFaceMsg 检测不到人脸提示信息
     * @param multiFaceMsg 检测到多人脸提示信息
     * @return FeatureBean
     */
    public FeatureBean getFeatureBean(FaceExtractResult faceExtractResult, String emptyFaceMsg, String multiFaceMsg);

    /**
     * 进行人脸图片质量检测
     * 
     * @param imageBase64 图片
     * @param threshold 阈值
     * @return 分值
     */
    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg);

    /**
     * 人脸图片1:1比对
     * 
     * @param imageFeature1 人脸图片1特征
     * @param imageFeature2 人脸图片2特征
     * @param threshold 比对阈值
     * @param notPassMsg 比对未通过提示信息
     * @return 比对分值
     */
    public double faceOne2OneCompare(FeatureBean imageFeature1, FeatureBean imageFeature2, Double threshold,
        String notPassMsg);

    /**
     * 人脸图片1:1比对
     * 
     * @param imageBase64_1 图片1Base64
     * @param imageBase64_2 图片2Base64
     * @param threshold 比对阈值,可以为空
     * @param notPassMsg 比对不通过提示信息,可以为空
     * @return 比对分值
     */
    public double faceOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    /**
     * 人脸图片1:1认证
     * 
     * @param featureBeanList 图片特征列表
     * @return MatchBean
     */
    public List<MatchBean> faceOne2OneCompare(List<FeatureBean> featureBeanList);

    /**
     * 上传入库人脸图像
     * 
     * @param encrypted 是否加密
     * @param fileName 文件名称
     * @param imageBase64 图片base64
     * @param baseDir 保存基础目录
     * @return 图片保存相对路径
     */
    public String uploadFaceImg(boolean encrypted, String fileName, String imageBase64, String baseDir);

    /**
     * 图片检活操作
     * 
     * @param imageBase64 图片base64
     * @param threshold 检活阈值
     * @return 检活结果
     */
    public CheckLiveResponse checkLive(String imageBase64, Double threshold);

    /**
     * 视频检活
     * 
     * @param videoBase64 视频base64
     * @param threshold 检活阈值
     * @return 检活结果
     */
    public CheckLiveResponse videoCheckLive(String videoBase64, Double threshold);

    /**
     * 1-N搜索
     * 
     * @param feature 人脸特征
     * @param channelCode 搜索库
     * @param topN 返回数据条数
     * @param threshold 搜索阈值
     * @return 搜索结果
     */
    public List<FaceSearchResult> faceSearchN(String feature, String channelCode, Integer topN, Double threshold);

    /**
     * 获取多人脸特征
     * 
     * @param imageBase64 图片base64
     * @return 特征列表
     */
    public List<FeatureBean> getMultiPersonFaceFeature(String imageBase64);

    /**
     * 查询人脸入库是否进行1：N校验
     * 
     * @return
     */
    public boolean getFaceAddIsValidateN();

    /**
     * 查询人脸入库是否进行活体检测
     * 
     * @return
     */
    public boolean getFaceAddIsCheckLive();

    /**
     * 查询系统人脸1:1比对阈值
     * 
     * @return
     */
    public double getOne2OneCompareThreshold();

    /**
     * 查询系统人脸质量检测阈值
     * 
     * @return
     */
    public double getDetectThreshold();

    /**
     * 获取人脸检活阈值
     * 
     * @param isVideo
     * @return
     */
    public double getCheckLiveThreshold(boolean isVideo);

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param faceImgFile 人脸图片
     * @param srcface 人脸信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的人脸信息
     * @date 2019年10月24日
     *
     */
    public BasePersonFace execCheckAndUploadFace(MultipartFile faceImgFile, BasePersonFace srcface,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @author mawenjun
     * @param fileModel 人脸图片信息对象
     * @param srcface 人脸信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的人脸信息
     * @date 2019年10月24日
     *
     */
    public BasePersonFace execCheckAndUploadFace(FileModel fileModel, BasePersonFace srcface, String stockImgFeature,
        Boolean isValidN, boolean isUpdate);

    /**
     * 执行人脸特征获取、质量检测和图片上传，返回人脸信息 如果是用作更新，源对象需要传入ID属性 传入底库照片特征则进行1:1比对，不传入则不比对
     * 
     * @param imageBase64 人脸图片Base64
     * @param fileName 图片名称
     * @param srcFace 人脸信息入参
     * @param stockImgFeature 底库照片特征
     * @param isValidN 是否进行1-N校验
     * @param isUpdate 是否是用于更新 true更新 false新增
     * @return 设置好属性的人脸信息
     */
    public BasePersonFace execCheckAndUploadFace(String imageBase64, String fileName, BasePersonFace srcFace,
        String stockImgFeature, Boolean isValidN, boolean isUpdate);
}
