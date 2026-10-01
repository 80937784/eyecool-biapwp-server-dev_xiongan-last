package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFingerMatchLog;

/**
 * 指纹比对日志Service接口
 * 
 * @author admin
 * @date 2021-05-06
 */
public interface IPersonFingerMatchLogService {
    /**
     * 查询指纹比对日志
     * 
     * @param id 指纹比对日志ID
     * @return 指纹比对日志
     */
    public PersonFingerMatchLog selectPersonFingerMatchLogById(String id);

    /**
     * 查询指纹比对日志列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志集合
     */
    public List<PersonFingerMatchLog> selectPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 查询指纹比对日志最新比对列表
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 指纹比对日志集合
     */
    public List<PersonFingerMatchLog> selectLastPersonFingerMatchLogList(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 新增指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    public int insertPersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 修改指纹比对日志
     * 
     * @param personFingerMatchLog 指纹比对日志
     * @return 结果
     */
    public int updatePersonFingerMatchLog(PersonFingerMatchLog personFingerMatchLog);

    /**
     * 批量删除指纹比对日志
     * 
     * @param ids 需要删除的指纹比对日志ID
     * @return 结果
     */
    public int deletePersonFingerMatchLogByIds(String[] ids);

    /**
     * 删除指纹比对日志信息
     * 
     * @param id 指纹比对日志ID
     * @return 结果
     */
    public int deletePersonFingerMatchLogById(String id);
}
