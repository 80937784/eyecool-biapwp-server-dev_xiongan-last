package cn.eyecool.tradelog.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.tradelog.domain.PersonIrisSearchLog;
import cn.eyecool.tradelog.mapper.PersonIrisSearchLogMapper;
import cn.eyecool.tradelog.service.IPersonIrisSearchLogService;

/**
 * 虹膜搜索日志Service业务层处理
 * 
 * @author admin
 * @date 2021-05-07
 */
@Service
public class PersonIrisSearchLogServiceImpl implements IPersonIrisSearchLogService {
    @Autowired
    private PersonIrisSearchLogMapper personIrisSearchLogMapper;

    /**
     * 查询虹膜搜索日志
     * 
     * @param id 虹膜搜索日志ID
     * @return 虹膜搜索日志
     */
    @Override
    public PersonIrisSearchLog selectPersonIrisSearchLogById(String id) {
        return personIrisSearchLogMapper.selectPersonIrisSearchLogById(id);
    }

    /**
     * 查询虹膜搜索日志列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonIrisSearchLog> selectPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog) {
        return personIrisSearchLogMapper.selectPersonIrisSearchLogList(personIrisSearchLog);
    }

    /**
     * 查询虹膜搜索日志最新比对列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonIrisSearchLog> selectLastPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog) {
        return personIrisSearchLogMapper.selectLastPersonIrisSearchLogList(personIrisSearchLog);
    }

    /**
     * 新增虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    @Override
    public int insertPersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog) {
        personIrisSearchLog.setCreateTime(DateUtils.getNowDate());
        return personIrisSearchLogMapper.insertPersonIrisSearchLog(personIrisSearchLog);
    }

    /**
     * 修改虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    @Override
    public int updatePersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog) {
        return personIrisSearchLogMapper.updatePersonIrisSearchLog(personIrisSearchLog);
    }

    /**
     * 批量删除虹膜搜索日志
     * 
     * @param ids 需要删除的虹膜搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonIrisSearchLogByIds(String[] ids) {
        return personIrisSearchLogMapper.deletePersonIrisSearchLogByIds(ids);
    }

    /**
     * 删除虹膜搜索日志信息
     * 
     * @param id 虹膜搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonIrisSearchLogById(String id) {
        return personIrisSearchLogMapper.deletePersonIrisSearchLogById(id);
    }
}
