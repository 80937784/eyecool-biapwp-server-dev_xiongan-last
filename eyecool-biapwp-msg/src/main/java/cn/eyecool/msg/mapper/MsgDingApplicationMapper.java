package cn.eyecool.msg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.msg.domain.MsgDingApplication;

/**
 * 钉钉微应用Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgDingApplicationMapper {
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
     * 删除钉钉微应用
     * 
     * @param id 钉钉微应用ID
     * @return 结果
     */
    public int deleteMsgDingApplicationById(String id);

    /**
     * 批量删除钉钉微应用
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgDingApplicationByIds(String[] ids);

    /**
     * 校验AgentId是否唯一
     * 
     * @param agentId
     * @param corpId
     * @return
     */
    public MsgDingApplication checkAgentIdUnique(@Param("agentId") String agentId, @Param("corpId") String corpId);

    /**
     * 校验appKey是否唯一
     * 
     * @param appKey
     * @param corpId
     * @return
     */
    public MsgDingApplication checkAppKeyUnique(@Param("appKey") String appKey, @Param("corpId") String corpId);
}
