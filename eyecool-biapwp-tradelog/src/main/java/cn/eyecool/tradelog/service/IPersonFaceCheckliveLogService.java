package cn.eyecool.tradelog.service;

import java.util.List;
import cn.eyecool.tradelog.domain.PersonFaceCheckliveLog;

/**
 * 人员人脸检活日志Service接口
 * 
 * @author admin
 * @date 2021-09-02
 */
public interface IPersonFaceCheckliveLogService 
{
    /**
     * 查询人员人脸检活日志
     * 
     * @param id 人员人脸检活日志ID
     * @return 人员人脸检活日志
     */
    public PersonFaceCheckliveLog selectPersonFaceCheckliveLogById(String id);

    /**
     * 查询人员人脸检活日志列表
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 人员人脸检活日志集合
     */
    public List<PersonFaceCheckliveLog> selectPersonFaceCheckliveLogList(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 新增人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    public int insertPersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 修改人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    public int updatePersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog);

    /**
     * 批量删除人员人脸检活日志
     * 
     * @param ids 需要删除的人员人脸检活日志ID
     * @return 结果
     */
    public int deletePersonFaceCheckliveLogByIds(String[] ids);

    /**
     * 删除人员人脸检活日志信息
     * 
     * @param id 人员人脸检活日志ID
     * @return 结果
     */
    public int deletePersonFaceCheckliveLogById(String id);
}
