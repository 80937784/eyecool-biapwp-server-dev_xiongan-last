package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.system.domain.SysUserIrisFace;

/**
 * 用户虹膜人脸多模态信息Mapper接口
 * 
 * @author admin
 * @date 2021-05-31
 */
public interface SysUserIrisFaceMapper {
    /**
     * 查询用户虹膜人脸多模态信息
     * 
     * @param id 用户虹膜人脸多模态信息ID
     * @return 用户虹膜人脸多模态信息
     */
    public SysUserIrisFace selectSysUserIrisFaceById(String id);

    /**
     * 查询用户虹膜人脸多模态信息列表
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 用户虹膜人脸多模态信息集合
     */
    public List<SysUserIrisFace> selectSysUserIrisFaceList(SysUserIrisFace sysUserIrisFace);

    /**
     * 新增用户虹膜人脸多模态信息
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 结果
     */
    public int insertSysUserIrisFace(SysUserIrisFace sysUserIrisFace);

    /**
     * 修改用户虹膜人脸多模态信息
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 结果
     */
    public int updateSysUserIrisFace(SysUserIrisFace sysUserIrisFace);

    /**
     * 删除用户虹膜人脸多模态信息
     * 
     * @param id 用户虹膜人脸多模态信息ID
     * @return 结果
     */
    public int deleteSysUserIrisFaceById(String id);

    /**
     * 批量删除用户虹膜人脸多模态信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysUserIrisFaceByIds(String[] ids);

    /**
     * 根据用户ID删除多模态信息
     * 
     * @param userId
     * @return
     */
    public void deleteSysUserIrisFaceByUserId(Long userId);
}
