package cn.eyecool.statistic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.statistic.mapper.SysUserStatisticMapper;
import cn.eyecool.statistic.service.ISysUserStatisticService;

/**
 * 用户统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class SysUserStatisticServiceImpl implements ISysUserStatisticService {

    @Autowired
    private SysUserStatisticMapper sysUserStatisticMapper;

    /**
     * 查询用户数量
     * 
     * @return
     */
    @Override
    public int countSysUser() {
        return sysUserStatisticMapper.countUser();
    }

}
