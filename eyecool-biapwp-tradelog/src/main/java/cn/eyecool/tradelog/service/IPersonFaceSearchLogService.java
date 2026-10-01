package cn.eyecool.tradelog.service;

import java.util.List;

import cn.eyecool.tradelog.domain.PersonFaceSearchLog;
import cn.eyecool.tradelog.param.PersonFaceSearchBakLog;

/**
 * 人脸搜索日志Service接口
 * 
 * @author admin
 * @date 2021-04-29
 */
public interface IPersonFaceSearchLogService {
    /**
     * 查询人脸搜索日志
     * 
     * @param id 人脸搜索日志ID
     * @return 人脸搜索日志
     */
    public PersonFaceSearchLog selectPersonFaceSearchLogById(String id);

    /**
     * 查询人脸搜索日志列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志集合
     */
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 查询人脸搜索日志最新比对列表
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 人脸搜索日志集合
     */
    public List<PersonFaceSearchLog> selectLastPersonFaceSearchLogList(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 新增人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    public int insertPersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 修改人脸搜索日志
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 结果
     */
    public int updatePersonFaceSearchLog(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 批量删除人脸搜索日志
     * 
     * @param ids 需要删除的人脸搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceSearchLogByIds(String[] ids);

    /**
     * 删除人脸搜索日志信息
     * 
     * @param id 人脸搜索日志ID
     * @return 结果
     */
    public int deletePersonFaceSearchLogById(String id);

    /**
     * 保存回传日志
     * 
     * @param faceSearchBakLog
     */
    public void savePersonFaceSearchBakLog(PersonFaceSearchBakLog faceSearchBakLog);

    /**
     * 按照时间范围查询人脸识别日志
     * 
     * @param personFaceSearchLog
     * @return
     */
    public List<PersonFaceSearchLog> selectPersonFaceSearchLogByTimeRange(PersonFaceSearchLog personFaceSearchLog);

    /**
     * 查询人脸搜索日志数量
     * 
     * @param personFaceSearchLog 人脸搜索日志
     * @return 数量
     */
    public int selectPersonFaceSearchLogCount(PersonFaceSearchLog personFaceSearchLog);

    /**
     * execXACardPassSendMsg 往雄安一卡通推送数据
     * 
     * @param faceSearchLog
     * @return void
     * @author zfx
     * @since 2022/11/29 15:12
     */
    void execXACardPassSendMsg(PersonFaceSearchLog faceSearchLog);

    /**
     * sendToOneCardPass 往一卡通推送数据
     * 
     * @param body body明文
     * @param url 发送地址
     * @param type 类型 0刷卡记录 1权限状态回传
     * @return void
     * @author zfx
     * @since 2023/4/18 14:06
     */
    void sendToOneCardPass(String body, String url, String type);
}
