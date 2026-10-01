package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFaceirisMatchLog;

/**
 * 人脸虹膜多模态比对日志Service接口
 * 
 * @author admin
 * @date 2021-12-09
 */
public interface IPersonFaceirisMatchLogService {
    /**
     * 查询人脸虹膜多模态比对日志
     * 
     * @param id 人脸虹膜多模态比对日志ID
     * @return 人脸虹膜多模态比对日志
     */
    public PersonFaceirisMatchLog selectPersonFaceirisMatchLogById(String id);

    /**
     * 查询人脸虹膜多模态比对日志列表
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 人脸虹膜多模态比对日志集合
     */
    public List<PersonFaceirisMatchLog> selectPersonFaceirisMatchLogList(PersonFaceirisMatchLog personFaceirisMatchLog);

    /**
     * 查询人脸虹膜多模态比对日志最新比对列表
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 人脸虹膜多模态比对日志
     */
    List<PersonFaceirisMatchLog> selectLastPersonFaceirisMatchLogList(PersonFaceirisMatchLog personFaceirisMatchLog);

    /**
     * 新增人脸虹膜多模态比对日志
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 结果
     */
    public int insertPersonFaceirisMatchLog(PersonFaceirisMatchLog personFaceirisMatchLog);

    /**
     * 修改人脸虹膜多模态比对日志
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 结果
     */
    public int updatePersonFaceirisMatchLog(PersonFaceirisMatchLog personFaceirisMatchLog);

    /**
     * 批量删除人脸虹膜多模态比对日志
     * 
     * @param ids 需要删除的人脸虹膜多模态比对日志ID
     * @return 结果
     */
    public int deletePersonFaceirisMatchLogByIds(String[] ids);

    /**
     * 删除人脸虹膜多模态比对日志信息
     * 
     * @param id 人脸虹膜多模态比对日志ID
     * @return 结果
     */
    public int deletePersonFaceirisMatchLogById(String id);

}
