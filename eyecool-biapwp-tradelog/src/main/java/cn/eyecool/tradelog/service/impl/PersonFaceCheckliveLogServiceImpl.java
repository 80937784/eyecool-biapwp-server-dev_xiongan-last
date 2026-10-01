package cn.eyecool.tradelog.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.tradelog.mapper.PersonFaceCheckliveLogMapper;
import cn.eyecool.tradelog.domain.PersonFaceCheckliveLog;
import cn.eyecool.tradelog.service.IPersonFaceCheckliveLogService;

/**
 * 人员人脸检活日志Service业务层处理
 * 
 * @author admin
 * @date 2021-09-02
 */
@Service
public class PersonFaceCheckliveLogServiceImpl implements IPersonFaceCheckliveLogService 
{
    @Autowired
    private PersonFaceCheckliveLogMapper personFaceCheckliveLogMapper;

    /**
     * 查询人员人脸检活日志
     * 
     * @param id 人员人脸检活日志ID
     * @return 人员人脸检活日志
     */
    @Override
    public PersonFaceCheckliveLog selectPersonFaceCheckliveLogById(String id)
    {
        return personFaceCheckliveLogMapper.selectPersonFaceCheckliveLogById(id);
    }

    /**
     * 查询人员人脸检活日志列表
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 人员人脸检活日志
     */
    @Override
    public List<PersonFaceCheckliveLog> selectPersonFaceCheckliveLogList(PersonFaceCheckliveLog personFaceCheckliveLog)
    {
        return personFaceCheckliveLogMapper.selectPersonFaceCheckliveLogList(personFaceCheckliveLog);
    }

    /**
     * 新增人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    @Override
    public int insertPersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog)
    {
        personFaceCheckliveLog.setCreateTime(DateUtils.getNowDate());
        return personFaceCheckliveLogMapper.insertPersonFaceCheckliveLog(personFaceCheckliveLog);
    }

    /**
     * 修改人员人脸检活日志
     * 
     * @param personFaceCheckliveLog 人员人脸检活日志
     * @return 结果
     */
    @Override
    public int updatePersonFaceCheckliveLog(PersonFaceCheckliveLog personFaceCheckliveLog)
    {
        return personFaceCheckliveLogMapper.updatePersonFaceCheckliveLog(personFaceCheckliveLog);
    }

    /**
     * 批量删除人员人脸检活日志
     * 
     * @param ids 需要删除的人员人脸检活日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceCheckliveLogByIds(String[] ids)
    {
        return personFaceCheckliveLogMapper.deletePersonFaceCheckliveLogByIds(ids);
    }

    /**
     * 删除人员人脸检活日志信息
     * 
     * @param id 人员人脸检活日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceCheckliveLogById(String id)
    {
        return personFaceCheckliveLogMapper.deletePersonFaceCheckliveLogById(id);
    }
}
