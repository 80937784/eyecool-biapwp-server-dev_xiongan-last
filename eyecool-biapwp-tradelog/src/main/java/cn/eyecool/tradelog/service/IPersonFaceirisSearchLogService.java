package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFaceirisSearchLog;
import cn.eyecool.tradelog.param.PersonFaceIrisMultiBakLog;

/**
 * 人脸虹膜搜索日志Service接口
 * 
 * @author admin
 * @date 2021-05-08
 */
public interface IPersonFaceirisSearchLogService {
    /**
     * 查询人脸虹膜搜索日志
     * 
     * @param id 人脸虹膜搜索日志ID
     * @return 人脸虹膜搜索日志
     */
    public PersonFaceirisSearchLog selectPersonFaceirisSearchLogById(String id);

    /**
     * 查询人脸虹膜搜索日志列表
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志集合
     */
    public List<PersonFaceirisSearchLog>
        selectPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 查询人脸虹膜搜索日志最新比对列表
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 人脸虹膜搜索日志集合
     */
    public List<PersonFaceirisSearchLog>
        selectLastPersonFaceirisSearchLogList(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 新增人脸虹膜搜索日志
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    public int insertPersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 修改人脸虹膜搜索日志
     * 
     * @param personFaceirisSearchLog 人脸虹膜搜索日志
     * @return 结果
     */
    public int updatePersonFaceirisSearchLog(PersonFaceirisSearchLog personFaceirisSearchLog);

    /**
     * 批量删除人脸虹膜搜索日志
     * 
     * @param ids 需要删除的人脸虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceirisSearchLogByIds(String[] ids);

    /**
     * 删除人脸虹膜搜索日志信息
     * 
     * @param id 人脸虹膜搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceirisSearchLogById(String id);

    /**
     * 保存人脸虹膜多模态回传日志
     * 
     * @param multiBakLog
     */
    public void savePersonFaceIrisMultiBakLog(PersonFaceIrisMultiBakLog multiBakLog);

    /**
     * 根据时间范围查询人脸虹膜多模态识别日志
     * 
     * @param personFaceirisSearchLog
     * @return
     */
    public List<PersonFaceirisSearchLog>
        selectPersonFaceIrisSearchLogByTimeRange(PersonFaceirisSearchLog personFaceirisSearchLog);
}
