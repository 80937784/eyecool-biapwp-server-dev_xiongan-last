package cn.eyecool.scene.trade.service;

import cn.eyecool.scene.trade.entity.PersonIrisFaceVerify;
import cn.eyecool.scene.trade.vo.PersonIrisFaceVerifyVO;

/**
 * 场景虹膜人脸多模态业务HTTP服务层
 * 
 * @author mawj
 * @date 2021/12/06
 */
public interface IChannelBusiIrisFaceHttpService {

    /**
     * 虹膜人脸多模态1:1认证
     * 
     * @param irisFaceVerify
     * @return
     */
    public PersonIrisFaceVerifyVO verifyPersonIrisFace(PersonIrisFaceVerify irisFaceVerify);

}
