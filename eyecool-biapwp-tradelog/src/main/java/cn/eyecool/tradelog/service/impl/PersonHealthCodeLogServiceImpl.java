package cn.eyecool.tradelog.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.tradelog.mapper.PersonHealthCodeLogMapper;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;
import cn.eyecool.tradelog.service.IPersonHealthCodeLogService;

/**
 * 健康码请求Service业务层处理
 * 
 * @author admin
 * @date 2021-05-13
 */
@Service
public class PersonHealthCodeLogServiceImpl implements IPersonHealthCodeLogService 
{
    @Autowired
    private PersonHealthCodeLogMapper personHealthCodeLogMapper;

    /**
     * 查询健康码请求
     * 
     * @param id 健康码请求ID
     * @return 健康码请求
     */
    @Override
    public PersonHealthCodeLog selectPersonHealthCodeLogById(String id)
    {
        return personHealthCodeLogMapper.selectPersonHealthCodeLogById(id);
    }

    /**
     * 查询健康码请求列表
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 健康码请求
     */
    @Override
    public List<PersonHealthCodeLog> selectPersonHealthCodeLogList(PersonHealthCodeLog personHealthCodeLog)
    {
        return personHealthCodeLogMapper.selectPersonHealthCodeLogList(personHealthCodeLog);
    }

    /**
     * 新增健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    @Override
    public int insertPersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog)
    {
        personHealthCodeLog.setCreateTime(DateUtils.getNowDate());
        return personHealthCodeLogMapper.insertPersonHealthCodeLog(personHealthCodeLog);
    }

    /**
     * 修改健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    @Override
    public int updatePersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog)
    {
        return personHealthCodeLogMapper.updatePersonHealthCodeLog(personHealthCodeLog);
    }

    /**
     * 批量删除健康码请求
     * 
     * @param ids 需要删除的健康码请求ID
     * @return 结果
     */
    @Override
    public int deletePersonHealthCodeLogByIds(String[] ids)
    {
        return personHealthCodeLogMapper.deletePersonHealthCodeLogByIds(ids);
    }

    /**
     * 删除健康码请求信息
     * 
     * @param id 健康码请求ID
     * @return 结果
     */
    @Override
    public int deletePersonHealthCodeLogById(String id)
    {
        return personHealthCodeLogMapper.deletePersonHealthCodeLogById(id);
    }
}
