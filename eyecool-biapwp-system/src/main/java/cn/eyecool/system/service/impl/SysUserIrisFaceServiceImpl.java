package cn.eyecool.system.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.system.mapper.SysUserIrisFaceMapper;
import cn.eyecool.system.domain.SysUserIrisFace;
import cn.eyecool.system.service.ISysUserIrisFaceService;

/**
 * 用户虹膜人脸多模态信息Service业务层处理
 * 
 * @author admin
 * @date 2021-05-31
 */
@Service
public class SysUserIrisFaceServiceImpl implements ISysUserIrisFaceService 
{
    @Autowired
    private SysUserIrisFaceMapper sysUserIrisFaceMapper;

    /**
     * 查询用户虹膜人脸多模态信息
     * 
     * @param id 用户虹膜人脸多模态信息ID
     * @return 用户虹膜人脸多模态信息
     */
    @Override
    public SysUserIrisFace selectSysUserIrisFaceById(String id)
    {
        return sysUserIrisFaceMapper.selectSysUserIrisFaceById(id);
    }

    /**
     * 查询用户虹膜人脸多模态信息列表
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 用户虹膜人脸多模态信息
     */
    @Override
    public List<SysUserIrisFace> selectSysUserIrisFaceList(SysUserIrisFace sysUserIrisFace)
    {
        return sysUserIrisFaceMapper.selectSysUserIrisFaceList(sysUserIrisFace);
    }

    /**
     * 新增用户虹膜人脸多模态信息
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 结果
     */
    @Override
    public int insertSysUserIrisFace(SysUserIrisFace sysUserIrisFace)
    {
        sysUserIrisFace.setCreateTime(DateUtils.getNowDate());
        return sysUserIrisFaceMapper.insertSysUserIrisFace(sysUserIrisFace);
    }

    /**
     * 修改用户虹膜人脸多模态信息
     * 
     * @param sysUserIrisFace 用户虹膜人脸多模态信息
     * @return 结果
     */
    @Override
    public int updateSysUserIrisFace(SysUserIrisFace sysUserIrisFace)
    {
        sysUserIrisFace.setUpdateTime(DateUtils.getNowDate());
        return sysUserIrisFaceMapper.updateSysUserIrisFace(sysUserIrisFace);
    }

    /**
     * 批量删除用户虹膜人脸多模态信息
     * 
     * @param ids 需要删除的用户虹膜人脸多模态信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserIrisFaceByIds(String[] ids)
    {
        return sysUserIrisFaceMapper.deleteSysUserIrisFaceByIds(ids);
    }

    /**
     * 删除用户虹膜人脸多模态信息信息
     * 
     * @param id 用户虹膜人脸多模态信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserIrisFaceById(String id)
    {
        return sysUserIrisFaceMapper.deleteSysUserIrisFaceById(id);
    }
}
