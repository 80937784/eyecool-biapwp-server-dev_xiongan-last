package cn.eyecool.scene.trade.service;

import java.util.List;
import java.util.Map;

import com.eyecool.abis.callmicroservice.common.FeatureBean;

import cn.eyecool.scene.trade.entity.PersonFaceRecog;
import cn.eyecool.scene.trade.entity.PersonFaceVerify;
import cn.eyecool.scene.trade.entity.PersonIdentityVerification;
import cn.eyecool.scene.trade.vo.PersonFaceRecogVO;
import cn.eyecool.scene.trade.vo.PersonFaceVerifyVO;

/**
 * 场景人脸业务HTTP服务层
 * 
 * @author admin
 * @date 2019年11月28日
 */
public interface IChannelBusiFaceHttpService {
    /**
     * 人脸1:1认证
     * 
     * @param faceVerify
     * @return
     */
    public PersonFaceVerifyVO verifyPersonFace(PersonFaceVerify faceVerify);

    /**
     * 人脸1:N识别
     * 
     * @param faceRecog
     */
    public List<PersonFaceRecogVO> recogPersonFace(PersonFaceRecog faceRecog);

    /**
     * 比对两张人脸图片
     * 
     * @param imageBase64_1
     * @param imageBase64_2
     * @param threshold
     * @param channelCode
     * @return
     */
    public Map<String, Object> compareTwoImage(String imageBase64_1, String imageBase64_2, Double threshold,
        String channelCode);

    /**
     * 获取人脸特征
     * 
     * @param sceneImage
     * @param channelCode
     * @return
     */
    public List<FeatureBean> getPersonFaceFeature(String sceneImage, String channelCode);

    /**
     * 人脸图片检活
     * 
     * @param sceneImage
     * @param threshold
     * @param channelCode
     * @return
     */
    public Map<String, Object> checklivePersonFaceImage(String sceneImage, Double threshold, String channelCode);

    /**
     * 人脸视频检活
     * 
     * @param sceneVideo
     * @param threshold
     * @param channelCode
     * @return
     */
    public Map<String, Object> checklivePersonFaceVideo(String sceneVideo, Double threshold, String channelCode);

    /**
     * 人脸图片质量检测
     * 
     * @param sceneImage
     * @param threshold
     * @param channelCode
     */
    public Map<String, Object> personFaceQualityDetect(String sceneImage, Double threshold, String channelCode);

    /**
     * 人脸视频检活和比对
     * 
     * @param sceneVideo
     * @param sceneImage
     * @param checkliveThreshold
     * @param channelCode
     * @return
     */
    Map<String, Object> checkliveFaceVideoAndCompare(String sceneVideo, String sceneImage, Double checkliveThreshold,
        String channelCode);

    /**
     * 联网身份核查
     * 
     * @param personIdVerification
     * @return
     */
    public Map<String, Object> personIdentityVerification(PersonIdentityVerification personIdVerification);

}
