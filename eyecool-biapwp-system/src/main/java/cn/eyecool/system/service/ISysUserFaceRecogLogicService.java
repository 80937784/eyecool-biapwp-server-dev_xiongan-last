package cn.eyecool.system.service;

import java.util.List;

import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.MatchBean;

import cn.eyecool.system.domain.SysUserFace;

/**
 * @author : sunhuayu
 * @version V1.0
 * @Project: eyecool-biapwp
 * @Package cn.eyecool.system.service
 * @Description: TODO
 * @date Date : 2021年01月19日 上午9:01
 */
public interface ISysUserFaceRecogLogicService {

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
    public SysUserFace execCheckAndUploadFace(String loginName, String imageBase64, String fileName,
        SysUserFace srcFace, String stockImgFeature, Boolean isValidN, boolean isUpdate);

    /**
     * 查询人脸入库是否进行活体检测
     *
     * @return
     */
    public boolean getFaceAddIsCheckLive();

    /**
     * 检活操作
     *
     * @param imageBase64
     * @param threshold
     * @return
     */
    public CheckLiveResponse checkLive(String imageBase64, Double threshold);

    public double getDetectThreshold();

    public double qualityDetect(String imageBase64, Double threshold, String notPassMsg);

    public FaceExtractResult getFaceExtractResult(String imageBase64);

    /**
     * 上传人脸图像
     *
     * @param encrypted
     * @param fileName
     * @param imageBase64
     * @param baseDir
     * @return
     */
    public String uploadFaceImg(boolean encrypted, String fileName, String imageBase64, String baseDir);

    /**
     * 人脸图片1:1比对
     *
     * @param imageFeature1
     * @param imageFeature2
     * @param threshold
     * @param notPassMsg
     * @return
     */
    public double faceOne2OneCompare(FeatureBean imageFeature1, FeatureBean imageFeature2, Double threshold,
        String notPassMsg);

    /**
     * 人脸图片1:1比对
     *
     * @param imageBase64_1 图片Base64
     * @param imageBase64_2 图片Base64
     * @param threshold 比对阈值,可以为空
     * @param notPassMsg 比对不通过提示信息,可以为空
     * @return 比对分值
     */
    public double faceOne2OneCompare(String imageBase64_1, String imageBase64_2, Double threshold, String notPassMsg);

    /**
     * 人脸图片1:1认证
     *
     * @param featureBeanList
     * @return
     */
    public List<MatchBean> faceOne2OneCompare(List<FeatureBean> featureBeanList);

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
     * @return
     */
    public FeatureBean getFeatureBean(String imgBase64, String emptyFaceMsg, String multiFaceMsg);

    /**
     * 根据FaceExtractResult获取特征
     *
     * @param faceExtractResult
     * @param emptyFaceMsg 检测不到人脸提示信息
     * @param multiFaceMsg 检测到多人脸提示信息
     * @return
     */
    public FeatureBean getFeatureBean(FaceExtractResult faceExtractResult, String emptyFaceMsg, String multiFaceMsg);

    /**
     * 查询系统人脸1:1比对阈值
     *
     * @return
     */
    public double getOne2OneCompareThreshold();

    public double getCheckLiveThreshold(boolean isVideo);
}
