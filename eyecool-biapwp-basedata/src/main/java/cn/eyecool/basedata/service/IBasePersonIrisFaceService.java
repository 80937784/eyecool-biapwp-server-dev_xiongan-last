package cn.eyecool.basedata.service;

import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.domain.IrisFaceRegister;

import java.util.List;

/**
 * 虹膜人脸多模态Service接口
 *
 * @author mawj
 * @date 2021-01-27
 */
public interface IBasePersonIrisFaceService {
    /**
     * 查询虹膜人脸多模态
     *
     * @param id 虹膜人脸多模态ID
     * @return 虹膜人脸多模态
     */
    public BasePersonIrisFace selectBasePersonIrisFaceById(String id);

    /**
     * 查询虹膜人脸多模态列表
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 虹膜人脸多模态集合
     */
    public List<BasePersonIrisFace> selectBasePersonIrisFaceList(BasePersonIrisFace basePersonIrisFace);

    /**
     * 新增虹膜人脸多模态
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    public int insertBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace);

    /**
     * 修改虹膜人脸多模态
     *
     * @param basePersonIrisFace 虹膜人脸多模态
     * @return 结果
     */
    public int updateBasePersonIrisFace(BasePersonIrisFace basePersonIrisFace);

    /**
     * 批量删除虹膜人脸多模态
     *
     * @param ids 需要删除的虹膜人脸多模态ID
     * @return 结果
     */
    public int deleteBasePersonIrisFaceByIds(String[] ids);

    /**
     * 删除虹膜人脸多模态信息
     *
     * @param id 虹膜人脸多模态ID
     * @return 结果
     */
    public int deleteBasePersonIrisFaceById(String id);

    /**
     * 虹膜人脸多模态信息注册
     *
     * @param irisFaceRegister
     * @param channelCode
     * @param primarySubCode
     */
    public void irisFaceRegister(IrisFaceRegister irisFaceRegister, String channelCode, String primarySubCode);

    /**
     * 校验人员是否有人脸虹膜多模态数据
     *
     * @param personId
     * @return
     */
    public boolean checkPersonHasFaceIris(String personId);


}
