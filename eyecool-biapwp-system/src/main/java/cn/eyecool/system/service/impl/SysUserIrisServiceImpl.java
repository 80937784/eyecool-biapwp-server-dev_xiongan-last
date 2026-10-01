package cn.eyecool.system.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.system.mapper.SysUserIrisMapper;
import cn.eyecool.system.domain.SysUserIris;
import cn.eyecool.system.service.ISysUserIrisService;

/**
 * 用户虹膜信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-16
 */
@Service
public class SysUserIrisServiceImpl implements ISysUserIrisService 
{
    @Autowired
    private SysUserIrisMapper sysUserIrisMapper;

    /**
     * 查询用户虹膜信息
     * 
     * @param id 用户虹膜信息ID
     * @return 用户虹膜信息
     */
    @Override
    public SysUserIris selectSysUserIrisById(String id)
    {
        return sysUserIrisMapper.selectSysUserIrisById(id);
    }

    /**
     * 查询用户虹膜信息列表
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 用户虹膜信息
     */
    @Override
    public List<SysUserIris> selectSysUserIrisList(SysUserIris sysUserIris)
    {
        return sysUserIrisMapper.selectSysUserIrisList(sysUserIris);
    }

    /**
     * 新增用户虹膜信息
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 结果
     */
    @Override
    public int insertSysUserIris(SysUserIris sysUserIris)
    {
        sysUserIris.setCreateTime(DateUtils.getNowDate());
        return sysUserIrisMapper.insertSysUserIris(sysUserIris);
    }

    /**
     * 修改用户虹膜信息
     * 
     * @param sysUserIris 用户虹膜信息
     * @return 结果
     */
    @Override
    public int updateSysUserIris(SysUserIris sysUserIris)
    {
        sysUserIris.setUpdateTime(DateUtils.getNowDate());
        return sysUserIrisMapper.updateSysUserIris(sysUserIris);
    }

    /**
     * 批量删除用户虹膜信息
     * 
     * @param ids 需要删除的用户虹膜信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserIrisByIds(String[] ids)
    {
        return sysUserIrisMapper.deleteSysUserIrisByIds(ids);
    }

    /**
     * 删除用户虹膜信息信息
     * 
     * @param id 用户虹膜信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserIrisById(String id)
    {
        return sysUserIrisMapper.deleteSysUserIrisById(id);
    }
}
