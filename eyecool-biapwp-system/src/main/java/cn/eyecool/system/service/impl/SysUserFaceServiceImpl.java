package cn.eyecool.system.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.system.mapper.SysUserFaceMapper;
import cn.eyecool.system.domain.SysUserFace;
import cn.eyecool.system.service.ISysUserFaceService;

/**
 * 用户人脸信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-16
 */
@Service
public class SysUserFaceServiceImpl implements ISysUserFaceService 
{
    @Autowired
    private SysUserFaceMapper sysUserFaceMapper;

    /**
     * 查询用户人脸信息
     * 
     * @param id 用户人脸信息ID
     * @return 用户人脸信息
     */
    @Override
    public SysUserFace selectSysUserFaceById(String id)
    {
        return sysUserFaceMapper.selectSysUserFaceById(id);
    }

    /**
     * 查询用户人脸信息列表
     * 
     * @param sysUserFace 用户人脸信息
     * @return 用户人脸信息
     */
    @Override
    public List<SysUserFace> selectSysUserFaceList(SysUserFace sysUserFace)
    {
        return sysUserFaceMapper.selectSysUserFaceList(sysUserFace);
    }

    /**
     * 新增用户人脸信息
     * 
     * @param sysUserFace 用户人脸信息
     * @return 结果
     */
    @Override
    public int insertSysUserFace(SysUserFace sysUserFace)
    {
        sysUserFace.setCreateTime(DateUtils.getNowDate());
        return sysUserFaceMapper.insertSysUserFace(sysUserFace);
    }

    /**
     * 修改用户人脸信息
     * 
     * @param sysUserFace 用户人脸信息
     * @return 结果
     */
    @Override
    public int updateSysUserFace(SysUserFace sysUserFace)
    {
        sysUserFace.setUpdateTime(DateUtils.getNowDate());
        return sysUserFaceMapper.updateSysUserFace(sysUserFace);
    }

    /**
     * 批量删除用户人脸信息
     * 
     * @param ids 需要删除的用户人脸信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserFaceByIds(String[] ids)
    {
        return sysUserFaceMapper.deleteSysUserFaceByIds(ids);
    }

    /**
     * 删除用户人脸信息信息
     * 
     * @param id 用户人脸信息ID
     * @return 结果
     */
    @Override
    public int deleteSysUserFaceById(String id)
    {
        return sysUserFaceMapper.deleteSysUserFaceById(id);
    }
}
