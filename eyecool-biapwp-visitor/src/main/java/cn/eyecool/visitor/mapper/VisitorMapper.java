package cn.eyecool.visitor.mapper;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.visitor.dto.UserSubSceneDTO;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/10/21 18:57
 */
public interface VisitorMapper {

    /**
     * 查询人员基础信息列表
     *
     * @param basePersonInfo 人员基础信息
     * @return 人员基础信息集合
     */
    List<BasePersonInfo> selectBasePersonInfoList(BasePersonInfo basePersonInfo);

    /**
     * 查询邀请人所在子场景集合
     *
     * @param personInfoId 人员基础信息主键id
     * @return 邀请人所在子场景集合
     */
    List<UserSubSceneDTO> getPersonSubScene(String personInfoId);

    /**
     * 查询是否存在相同的访客人脸
     *
     * @param basePersonFace 访客人脸查询条件
     * @return 访客人脸集合
     */
    List<BasePersonFace> findRepeatFaceImage(BasePersonFace basePersonFace);

}
