package cn.eyecool.system.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.system.mapper.SysUserFingerMapper;
import cn.eyecool.system.domain.SysUserFinger;
import cn.eyecool.system.service.ISysUserFingerService;

/**
 * 用户指纹信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-16
 */
@Service
public class SysUserFingerServiceImpl implements ISysUserFingerService 
{
    @Autowired
    private SysUserFingerMapper sysUserFingerMapper;

    /**
     * 查询用户指纹信息
     * 
     * @param id 用户指纹信息ID
     * @return 用户指纹信息
     */
    @Override
    public SysUserFinger selectSysUserFingerById(String id)
    {
        return sysUserFingerMapper.selectSysUserFingerById(id);
    }

    /**
     * 查询用户指纹信息列表
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 用户指纹信息
     */
    @Override
    public List<SysUserFinger> selectSysUserFingerList(SysUserFinger sysUserFinger)
    {
        return sysUserFingerMapper.selectSysUserFingerList(sysUserFinger);
    }

    /**
     * 新增用户指纹信息
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 结果
     */
    @Override
    public int insertSysUserFinger(SysUserFinger sysUserFinger)
    {
        sysUserFinger.setCreateTime(DateUtils.getNowDate());
        return sysUserFingerMapper.insertSysUserFinger(sysUserFinger);
    }

    /**
     * 修改用户指纹信息
     * 
     * @param sysUserFinger 用户指纹信息
     * @return 结果
     */
    @Override
    public int updateSysUserFinger(SysUserFinger sysUserFinger)
    {
        sysUserFinger.setUpdateTime(DateUtils.getNowDate());
        return sysUserFingerMapper.updateSysUserFinger(sysUserFinger);
    }

    /**
     * 批量删除用户指纹信息
     * 
     * @param ids 需要删除的用户指纹信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserFingerByIds(String[] ids)
    {
        return sysUserFingerMapper.deleteSysUserFingerByIds(ids);
    }

    /**
     * 删除用户指纹信息信息
     * 
     * @param id 用户指纹信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserFingerById(String id)
    {
        return sysUserFingerMapper.deleteSysUserFingerById(id);
    }
}
