package cn.eyecool.tradelog.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.tradelog.domain.PersonIrisMatchLog;
import cn.eyecool.tradelog.mapper.PersonIrisMatchLogMapper;
import cn.eyecool.tradelog.service.IPersonIrisMatchLogService;

/**
 * 虹膜比对日志Service业务层处理
 * 
 * @author admin
 * @date 2021-05-07
 */
@Service
public class PersonIrisMatchLogServiceImpl implements IPersonIrisMatchLogService {
    @Autowired
    private PersonIrisMatchLogMapper personIrisMatchLogMapper;

    /**
     * 查询虹膜比对日志
     * 
     * @param id 虹膜比对日志ID
     * @return 虹膜比对日志
     */
    @Override
    public PersonIrisMatchLog selectPersonIrisMatchLogById(String id) {
        return personIrisMatchLogMapper.selectPersonIrisMatchLogById(id);
    }

    /**
     * 查询虹膜比对日志列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonIrisMatchLog> selectPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog) {
        return personIrisMatchLogMapper.selectPersonIrisMatchLogList(personIrisMatchLog);
    }

    /**
     * 查询虹膜比对日志最新比对列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonIrisMatchLog> selectLastPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog) {
        return personIrisMatchLogMapper.selectLastPersonIrisMatchLogList(personIrisMatchLog);
    }

    /**
     * 新增虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    @Override
    public int insertPersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog) {
        personIrisMatchLog.setCreateTime(DateUtils.getNowDate());
        return personIrisMatchLogMapper.insertPersonIrisMatchLog(personIrisMatchLog);
    }

    /**
     * 修改虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    @Override
    public int updatePersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog) {
        return personIrisMatchLogMapper.updatePersonIrisMatchLog(personIrisMatchLog);
    }

    /**
     * 批量删除虹膜比对日志
     * 
     * @param ids 需要删除的虹膜比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonIrisMatchLogByIds(String[] ids) {
        return personIrisMatchLogMapper.deletePersonIrisMatchLogByIds(ids);
    }

    /**
     * 删除虹膜比对日志信息
     * 
     * @param id 虹膜比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonIrisMatchLogById(String id) {
        return personIrisMatchLogMapper.deletePersonIrisMatchLogById(id);
    }
}
