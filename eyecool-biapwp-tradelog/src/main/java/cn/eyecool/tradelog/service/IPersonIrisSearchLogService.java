package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonIrisSearchLog;

/**
 * 虹膜搜索日志Service接口
 * 
 * @author admin
 * @date 2021-05-07
 */
public interface IPersonIrisSearchLogService {
    /**
     * 查询虹膜搜索日志
     * 
     * @param id 虹膜搜索日志ID
     * @return 虹膜搜索日志
     */
    public PersonIrisSearchLog selectPersonIrisSearchLogById(String id);

    /**
     * 查询虹膜搜索日志列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志集合
     */
    public List<PersonIrisSearchLog> selectPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 查询虹膜搜索日志最新列表
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 虹膜搜索日志集合
     */
    public List<PersonIrisSearchLog> selectLastPersonIrisSearchLogList(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 新增虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    public int insertPersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 修改虹膜搜索日志
     * 
     * @param personIrisSearchLog 虹膜搜索日志
     * @return 结果
     */
    public int updatePersonIrisSearchLog(PersonIrisSearchLog personIrisSearchLog);

    /**
     * 批量删除虹膜搜索日志
     * 
     * @param ids 需要删除的虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonIrisSearchLogByIds(String[] ids);

    /**
     * 删除虹膜搜索日志信息
     * 
     * @param id 虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonIrisSearchLogById(String id);
}
