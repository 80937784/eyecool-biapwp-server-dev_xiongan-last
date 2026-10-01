package cn.eyecool.system.service;

import java.util.List;
import cn.eyecool.system.domain.SysUserIris;

/**
 * 用户虹膜信息Service接口
 * 
 * @author admin
 * @date 2021-04-16
 */
public interface ISysUserIrisService 
{
    /**
     * 查询用户虹膜信息
     * 
     * @param id 用户虹膜信息ID
     * @return 用户虹膜信息
     */
    public SysUserIris selectSysUserIrisById(String id);

    /**
     * 查询用户虹膜信息列表
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 用户虹膜信息集合
     */
    public List<SysUserIris> selectSysUserIrisList(SysUserIris sysUserIris);

    /**
     * 新增用户虹膜信息
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 结果
     */
    public int insertSysUserIris(SysUserIris sysUserIris);

    /**
     * 修改用户虹膜信息
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 结果
     */
    public int updateSysUserIris(SysUserIris sysUserIris);

    /**
     * 批量删除用户虹膜信息
     * 
     * @param ids 需要删除的用户虹膜信息ID
     * @return 结果
     */
    public int deleteSysUserIrisByIds(String[] ids);

    /**
     * 删除用户虹膜信息信息
     * 
     * @param id 用户虹膜信息ID
     * @return 结果
     */
    public int deleteSysUserIrisById(String id);
}
