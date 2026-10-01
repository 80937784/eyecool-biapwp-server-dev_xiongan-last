package cn.eyecool.scene.trade.service;

import java.util.List;
import java.util.Map;

import com.eyecool.abis.callmicroservice.common.FeatureBean;

import cn.eyecool.scene.trade.entity.PersonIrisRecog;
import cn.eyecool.scene.trade.entity.PersonIrisVerify;
import cn.eyecool.scene.trade.vo.PersonIrisRecogVO;
import cn.eyecool.scene.trade.vo.PersonIrisVerifyVO;

/**
 * 场景虹膜业务HTTP服务层
 * 
 * @author admin
 * @date 2019年11月28日
 */
public interface IChannelBusiIrisHttpService {

    /**
     * 虹膜1:1认证
     * 
     * @param irisVerify
     * @return
     */
    public PersonIrisVerifyVO verifyPersonIris(PersonIrisVerify irisVerify);

    /**
     * 虹膜1:N识别
     * 
     * @param irisRecog
     * @return
     */
    public List<PersonIrisRecogVO> recogPersonIris(PersonIrisRecog irisRecog);

    /**
     * 比对两张虹膜图片
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
     * 提取虹膜特征
     * 
     * @param sceneImage
     * @param channelCode
     * @return
     */
    public List<FeatureBean> getPersonIrisFeature(String sceneImage, String channelCode);
}
