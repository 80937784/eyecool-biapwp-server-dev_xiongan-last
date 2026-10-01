package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFingerSearchLog;

/**
 * 指纹搜索日志Service接口
 * 
 * @author admin
 * @date 2021-05-06
 */
public interface IPersonFingerSearchLogService {
    /**
     * 查询指纹搜索日志
     * 
     * @param id 指纹搜索日志ID
     * @return 指纹搜索日志
     */
    public PersonFingerSearchLog selectPersonFingerSearchLogById(String id);

    /**
     * 查询指纹搜索日志列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志集合
     */
    public List<PersonFingerSearchLog> selectPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 查询指纹搜索日志最新比对列表
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 指纹搜索日志集合
     */
    public List<PersonFingerSearchLog> selectLastPersonFingerSearchLogList(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 新增指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    public int insertPersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 修改指纹搜索日志
     * 
     * @param personFingerSearchLog 指纹搜索日志
     * @return 结果
     */
    public int updatePersonFingerSearchLog(PersonFingerSearchLog personFingerSearchLog);

    /**
     * 批量删除指纹搜索日志
     * 
     * @param ids 需要删除的指纹搜索日志ID
     * @return 结果
     */
    public int deletePersonFingerSearchLogByIds(String[] ids);

    /**
     * 删除指纹搜索日志信息
     * 
     * @param id 指纹搜索日志ID
     * @return 结果
     */
    public int deletePersonFingerSearchLogById(String id);
}
