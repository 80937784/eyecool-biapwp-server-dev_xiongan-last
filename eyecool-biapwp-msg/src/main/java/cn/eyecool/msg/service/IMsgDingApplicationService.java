package cn.eyecool.msg.service;

import java.util.List;

import cn.eyecool.msg.domain.MsgDingApplication;

/**
 * 钉钉微应用Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgDingApplicationService {
    /**
     * 查询钉钉微应用
     * 
     * @param id 钉钉微应用ID
     * @return 钉钉微应用
     */
    public MsgDingApplication selectMsgDingApplicationById(String id);

    /**
     * 查询钉钉微应用列表
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 钉钉微应用集合
     */
    public List<MsgDingApplication> selectMsgDingApplicationList(MsgDingApplication msgDingApplication);

    /**
     * 新增钉钉微应用
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 结果
     */
    public int insertMsgDingApplication(MsgDingApplication msgDingApplication);

    /**
     * 修改钉钉微应用
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 结果
     */
    public int updateMsgDingApplication(MsgDingApplication msgDingApplication);

    /**
     * 批量删除钉钉微应用
     * 
     * @param ids 需要删除的钉钉微应用ID
     * @return 结果
     */
    public int deleteMsgDingApplicationByIds(String[] ids);

    /**
     * 删除钉钉微应用信息
     * 
     * @param id 钉钉微应用ID
     * @return 结果
     */
    public int deleteMsgDingApplicationById(String id);

    /**
     * 校验AgentId是否唯一
     * 
     * @param msgDingApplication
     * @return
     */
    public Boolean checkAgentIdUnique(MsgDingApplication msgDingApplication);

    /**
     * 校验AppKey是否唯一
     * 
     * @param msgDingApplication
     * @return
     */
    public Boolean checkAppKeyUnique(MsgDingApplication msgDingApplication);
}
