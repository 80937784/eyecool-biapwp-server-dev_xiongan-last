package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFaceMatchLog;
import cn.eyecool.tradelog.param.PersonFaceMatchBakLog;

/**
 * 人脸比对日志Service接口
 * 
 * @author admin
 * @date 2021-04-29
 */
public interface IPersonFaceMatchLogService {
    /**
     * 查询人脸比对日志
     * 
     * @param id 人脸比对日志ID
     * @return 人脸比对日志
     */
    public PersonFaceMatchLog selectPersonFaceMatchLogById(String id);

    /**
     * 查询人脸比对日志列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志集合
     */
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 查询人脸比对日志最新比对列表
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 人脸比对日志集合
     */
    public List<PersonFaceMatchLog> selectLastPersonFaceMatchLogList(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 新增人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    public int insertPersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 修改人脸比对日志
     * 
     * @param personFaceMatchLog 人脸比对日志
     * @return 结果
     */
    public int updatePersonFaceMatchLog(PersonFaceMatchLog personFaceMatchLog);

    /**
     * 批量删除人脸比对日志
     * 
     * @param ids 需要删除的人脸比对日志ID
     * @return 结果
     */
    public int deletePersonFaceMatchLogByIds(String[] ids);

    /**
     * 删除人脸比对日志信息
     * 
     * @param id 人脸比对日志ID
     * @return 结果
     */
    public int deletePersonFaceMatchLogById(String id);

    /**
     * 保存1v1回传日志
     * 
     * @param faceMatchBakLog
     */
    public void savePersonFaceMatchBakLog(PersonFaceMatchBakLog faceMatchBakLog);

    /**
     * 根据时间范围查询日志
     * 
     * @param personFaceMatchLog
     * @return
     */
    public List<PersonFaceMatchLog> selectPersonFaceMatchLogByTimeRange(PersonFaceMatchLog personFaceMatchLog);
}
