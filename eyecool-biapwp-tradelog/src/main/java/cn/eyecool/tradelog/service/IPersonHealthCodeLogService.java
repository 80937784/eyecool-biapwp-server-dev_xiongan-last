package cn.eyecool.tradelog.service;

import java.util.List;
import cn.eyecool.tradelog.domain.PersonHealthCodeLog;

/**
 * 健康码请求Service接口
 * 
 * @author admin
 * @date 2021-05-13
 */
public interface IPersonHealthCodeLogService 
{
    /**
     * 查询健康码请求
     * 
     * @param id 健康码请求ID
     * @return 健康码请求
     */
    public PersonHealthCodeLog selectPersonHealthCodeLogById(String id);

    /**
     * 查询健康码请求列表
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 健康码请求集合
     */
    public List<PersonHealthCodeLog> selectPersonHealthCodeLogList(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 新增健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    public int insertPersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 修改健康码请求
     * 
     * @param personHealthCodeLog 健康码请求
     * @return 结果
     */
    public int updatePersonHealthCodeLog(PersonHealthCodeLog personHealthCodeLog);

    /**
     * 批量删除健康码请求
     * 
     * @param ids 需要删除的健康码请求ID
     * @return 结果
     */
    public int deletePersonHealthCodeLogByIds(String[] ids);

    /**
     * 删除健康码请求信息
     * 
     * @param id 健康码请求ID
     * @return 结果
     */
    public int deletePersonHealthCodeLogById(String id);
}
