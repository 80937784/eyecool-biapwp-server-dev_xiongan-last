package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonIrisMatchLog;

/**
 * 虹膜比对日志Service接口
 * 
 * @author admin
 * @date 2021-05-07
 */
public interface IPersonIrisMatchLogService {
    /**
     * 查询虹膜比对日志
     * 
     * @param id 虹膜比对日志ID
     * @return 虹膜比对日志
     */
    public PersonIrisMatchLog selectPersonIrisMatchLogById(String id);

    /**
     * 查询虹膜比对日志列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志集合
     */
    public List<PersonIrisMatchLog> selectPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 查询虹膜比对日志最新比对列表
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 虹膜比对日志集合
     */
    public List<PersonIrisMatchLog> selectLastPersonIrisMatchLogList(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 新增虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    public int insertPersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 修改虹膜比对日志
     * 
     * @param personIrisMatchLog 虹膜比对日志
     * @return 结果
     */
    public int updatePersonIrisMatchLog(PersonIrisMatchLog personIrisMatchLog);

    /**
     * 批量删除虹膜比对日志
     * 
     * @param ids 需要删除的虹膜比对日志ID
     * @return 结果
     */
    public int deletePersonIrisMatchLogByIds(String[] ids);

    /**
     * 删除虹膜比对日志信息
     * 
     * @param id 虹膜比对日志ID
     * @return 结果
     */
    public int deletePersonIrisMatchLogById(String id);
}
