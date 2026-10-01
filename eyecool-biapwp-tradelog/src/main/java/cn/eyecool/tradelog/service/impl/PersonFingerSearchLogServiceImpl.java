package cn.eyecool.tradelog.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.tradelog.domain.PersonFingerSearchLog;
import cn.eyecool.tradelog.mapper.PersonFingerSearchLogMapper;
import cn.eyecool.tradelog.service.IPersonFingerSearchLogService;

/**
 * 指纹搜索日志Service业务层处理
 * 
 * @author admin
 * @date 2021-05-06
 */
@Service
public class PersonFingerSearchLogServiceImpl implements IPersonFingerSearchLogService {
    @Autowired
    private PersonFingerSearchLogMapper personFingerSearchLogMapper;

    /**
     * 查询指纹搜索日志
     * 
     * @param id 指纹搜索日志ID
     * @return 指纹搜索日志
     */
    @Override
    public PersonFingerSearchLog selectPersonFingerSearchLogById(String id) {
        return personFingerSearchLogMapper.selectPersonFingerSearchLogById(id);
    }

    /**
     * 查询指纹搜索日志列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFingerSearchLog> selectPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog) {
        return personFingerSearchLogMapper.selectPersonFingerSearchLogList(personFingerSearchLog);
    }

    /**
     * 查询指纹搜索日志最新比对列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFingerSearchLog>
        selectLastPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog) {
        return personFingerSearchLogMapper.selectLastPersonFingerSearchLogList(personFingerSearchLog);
    }

    /**
     * 新增指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    @Override
    public int insertPersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog) {
        personFingerSearchLog.setCreateTime(DateUtils.getNowDate());
        return personFingerSearchLogMapper.insertPersonFingerSearchLog(personFingerSearchLog);
    }

    /**
     * 修改指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    @Override
    public int updatePersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog) {
        return personFingerSearchLogMapper.updatePersonFingerSearchLog(personFingerSearchLog);
    }

    /**
     * 批量删除指纹搜索日志
     * 
     * @param ids 需要删除的指纹搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFingerSearchLogByIds(String[] ids) {
        return personFingerSearchLogMapper.deletePersonFingerSearchLogByIds(ids);
    }

    /**
     * 删除指纹搜索日志信息
     * 
     * @param id 指纹搜索日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFingerSearchLogById(String id) {
        return personFingerSearchLogMapper.deletePersonFingerSearchLogById(id);
    }
}
