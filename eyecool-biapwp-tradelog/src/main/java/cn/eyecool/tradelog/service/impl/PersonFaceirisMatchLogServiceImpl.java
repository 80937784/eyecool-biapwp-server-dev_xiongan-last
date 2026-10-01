package cn.eyecool.tradelog.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.tradelog.domain.PersonFaceirisMatchLog;
import cn.eyecool.tradelog.mapper.PersonFaceirisMatchLogMapper;
import cn.eyecool.tradelog.service.IPersonFaceirisMatchLogService;

/**
 * 人脸虹膜多模态比对日志Service业务层处理
 * 
 * @author admin
 * @date 2021-12-09
 */
@Service
public class PersonFaceirisMatchLogServiceImpl implements IPersonFaceirisMatchLogService {
    @Autowired
    private PersonFaceirisMatchLogMapper personFaceirisMatchLogMapper;

    /**
     * 查询人脸虹膜多模态比对日志
     * 
     * @param id 人脸虹膜多模态比对日志ID
     * @return 人脸虹膜多模态比对日志
     */
    @Override
    public PersonFaceirisMatchLog selectPersonFaceirisMatchLogById(String id) {
        return personFaceirisMatchLogMapper.selectPersonFaceirisMatchLogById(id);
    }

    /**
     * 查询人脸虹膜多模态比对日志列表
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 人脸虹膜多模态比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceirisMatchLog>
        selectPersonFaceirisMatchLogList(PersonFaceirisMatchLog personFaceirisMatchLog) {
        return personFaceirisMatchLogMapper.selectPersonFaceirisMatchLogList(personFaceirisMatchLog);
    }

    /**
     * 查询人脸虹膜多模态比对日志最新比对列表
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 人脸虹膜多模态比对日志
     */
    @Override
    @DataScope(deptAlias = "log")
    public List<PersonFaceirisMatchLog>
        selectLastPersonFaceirisMatchLogList(PersonFaceirisMatchLog personFaceirisMatchLog) {
        return personFaceirisMatchLogMapper.selectLastPersonFaceirisMatchLogList(personFaceirisMatchLog);
    }

    /**
     * 新增人脸虹膜多模态比对日志
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 结果
     */
    @Override
    public int insertPersonFaceirisMatchLog(PersonFaceirisMatchLog personFaceirisMatchLog) {
        personFaceirisMatchLog.setCreateTime(DateUtils.getNowDate());
        return personFaceirisMatchLogMapper.insertPersonFaceirisMatchLog(personFaceirisMatchLog);
    }

    /**
     * 修改人脸虹膜多模态比对日志
     * 
     * @param personFaceirisMatchLog 人脸虹膜多模态比对日志
     * @return 结果
     */
    @Override
    public int updatePersonFaceirisMatchLog(PersonFaceirisMatchLog personFaceirisMatchLog) {
        return personFaceirisMatchLogMapper.updatePersonFaceirisMatchLog(personFaceirisMatchLog);
    }

    /**
     * 批量删除人脸虹膜多模态比对日志
     * 
     * @param ids 需要删除的人脸虹膜多模态比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceirisMatchLogByIds(String[] ids) {
        return personFaceirisMatchLogMapper.deletePersonFaceirisMatchLogByIds(ids);
    }

    /**
     * 删除人脸虹膜多模态比对日志信息
     * 
     * @param id 人脸虹膜多模态比对日志ID
     * @return 结果
     */
    @Override
    public int deletePersonFaceirisMatchLogById(String id) {
        return personFaceirisMatchLogMapper.deletePersonFaceirisMatchLogById(id);
    }
}
