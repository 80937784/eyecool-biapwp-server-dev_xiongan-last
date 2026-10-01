package cn.eyecool.scene.trade.service;

import java.util.List;
import java.util.Map;

import com.eyecool.abis.callmicroservice.common.FeatureBean;

import cn.eyecool.scene.trade.entity.PersonFingerRecog;
import cn.eyecool.scene.trade.entity.PersonFingerVerify;
import cn.eyecool.scene.trade.vo.PersonFingerRecogVO;
import cn.eyecool.scene.trade.vo.PersonFingerVerifyVO;

/**
 * 场景指纹业务HTTP服务层
 * 
 * @author admin
 * @date 2019年11月28日
 */
public interface IChannelBusiFingerHttpService {

    /**
     * 指纹1:1认证
     * 
     * @param fingerVerify
     * @return
     */
    public PersonFingerVerifyVO verifyPersonFinger(PersonFingerVerify fingerVerify);

    /**
     * 指纹1:N识别
     * 
     * @param fingerRecog
     * @return
     */
    public List<PersonFingerRecogVO> recogPersonFinger(PersonFingerRecog fingerRecog);

    /**
     * 比对两张指纹图片
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
     * 提取指纹特征信息
     * 
     * @param sceneImage
     * @param channelCode
     * @return
     */
    public List<FeatureBean> getPersonFingerFeature(String sceneImage, String channelCode);

    /**
     * 指纹图片质量检测
     * 
     * @param sceneImage
     * @param threshold
     * @param channelCode
     * @return
     */
    public Map<String, Object> personFingerQualityDetect(String sceneImage, Double threshold, String channelCode);

}
