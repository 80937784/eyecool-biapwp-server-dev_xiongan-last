package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.system.domain.SysUserFinger;

/**
 * 用户指纹信息Mapper接口
 * 
 * @author admin
 * @date 2021-04-16
 */
public interface SysUserFingerMapper {
    /**
     * 查询用户指纹信息
     * 
     * @param id 用户指纹信息ID
     * @return 用户指纹信息
     */
    public SysUserFinger selectSysUserFingerById(String id);

    /**
     * 查询用户指纹信息列表
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 用户指纹信息集合
     */
    public List<SysUserFinger> selectSysUserFingerList(SysUserFinger sysUserFinger);

    /**
     * 新增用户指纹信息
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 结果
     */
    public int insertSysUserFinger(SysUserFinger sysUserFinger);

    /**
     * 修改用户指纹信息
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 结果
     */
    public int updateSysUserFinger(SysUserFinger sysUserFinger);

    /**
     * 删除用户指纹信息
     * 
     * @param id 用户指纹信息ID
     * @return 结果
     */
    public int deleteSysUserFingerById(String id);

    /**
     * 批量删除用户指纹信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysUserFingerByIds(String[] ids);

    /**
     * 根据用户ID删除指纹信息
     * 
     * @param userId
     * @return
     */
    public int deleteSysUserFingerByUserId(Long userId);
}
