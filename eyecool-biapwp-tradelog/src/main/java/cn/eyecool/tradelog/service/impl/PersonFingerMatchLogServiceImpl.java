package cn.eyecool.tradelog.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.tradelog.domain.PersonFingerMatchLog;
import cn.eyecool.tradelog.mapper.PersonFingerMatchLogMapper;
import cn.eyecool.tradelog.service.IPersonFingerMatchLogService;

/**
 * 指纹比对日志Service业务层处理
 * 
 * @author admin
 * @date 2021-05-06
 */
@Service
public class PersonFingerMatchLogServiceImpl implements IPersonFingerMatchLogService {
    @Autowired
    private PersonFingerMatchLogMapper personFingerMatchLogMapper;

    /**
     * 查询指纹比对日志
     * 
     * @param id 指纹比对日志ID
     * @return 指纹比对日志
     */
    @Override
    public PersonFingerMatchLog selectPersonFingerMatchLogById(String id) {
        return personFingerMatchLogMapper.selectPersonFingerMatchLogById(id);
    }

    /**
     * 查询指纹比对日志列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFingerMatchLog> selectPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog) {
        return personFingerMatchLogMapper.selectPersonFingerMatchLogList(personFingerMatchLog);
    }

    /**
     * 查询指纹比对日志最新比对列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFingerMatchLog> selectLastPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog) {
        return personFingerMatchLogMapper.selectLastPersonFingerMatchLogList(personFingerMatchLog);
    }

    /**
     * 新增指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    @Override
    public int insertPersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog) {
        personFingerMatchLog.setCreateTime(DateUtils.getNowDate());
        return personFingerMatchLogMapper.insertPersonFingerMatchLog(personFingerMatchLog);
    }

    /**
     * 修改指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    @Override
    public int updatePersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog) {
        return personFingerMatchLogMapper.updatePersonFingerMatchLog(personFingerMatchLog);
    }

    /**
     * 批量删除指纹比对日志
     * 
     * @param ids 需要删除的指纹比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFingerMatchLogByIds(String[] ids) {
        return personFingerMatchLogMapper.deletePersonFingerMatchLogByIds(ids);
    }

    /**
     * 删除指纹比对日志信息
     * 
     * @param id 指纹比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFingerMatchLogById(String id) {
        return personFingerMatchLogMapper.deletePersonFingerMatchLogById(id);
    }
}
