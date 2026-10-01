package cn.eyecool.system.service;

import java.util.List;
import cn.eyecool.system.domain.SysUserFace;

/**
 * 用户人脸信息Service接口
 * 
 * @author admin
 * @date 2021-04-16
 */
public interface ISysUserFaceService 
{
    /**
     * 查询用户人脸信息
     * 
     * @param id 用户人脸信息ID
     * @return 用户人脸信息
     */
    public SysUserFace selectSysUserFaceById(String id);

    /**
     * 查询用户人脸信息列表
     * 
     * @param sysUserFace 用户人脸信息
     * @return 用户人脸信息集合
     */
    public List<SysUserFace> selectSysUserFaceList(SysUserFace sysUserFace);

    /**
     * 新增用户人脸信息
     * 
     * @param sysUserFace 用户人脸信息
     * @return 结果
     */
    public int insertSysUserFace(SysUserFace sysUserFace);

    /**
     * 修改用户人脸信息
     * 
     * @param sysUserFace 用户人脸信息
     * @return 结果
     */
    public int updateSysUserFace(SysUserFace sysUserFace);

    /**
     * 批量删除用户人脸信息
     * 
     * @param ids 需要删除的用户人脸信息ID
     * @return 结果
     */
    public int deleteSysUserFaceByIds(String[] ids);

    /**
     * 删除用户人脸信息信息
     * 
     * @param id 用户人脸信息ID
     * @return 结果
     */
    public int deleteSysUserFaceById(String id);
}
