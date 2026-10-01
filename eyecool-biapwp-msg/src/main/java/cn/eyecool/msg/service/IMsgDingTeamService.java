package cn.eyecool.msg.service;

import java.util.List;

import cn.eyecool.msg.domain.MsgDingTeam;

/**
 * 钉钉团队(企业)Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgDingTeamService {
    /**
     * 查询钉钉团队(企业)
     * 
     * @param id 钉钉团队(企业)ID
     * @return 钉钉团队(企业)
     */
    public MsgDingTeam selectMsgDingTeamById(String id);

    /**
     * 查询钉钉团队(企业)列表
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 钉钉团队(企业)集合
     */
    public List<MsgDingTeam> selectMsgDingTeamList(MsgDingTeam msgDingTeam);

    /**
     * 新增钉钉团队(企业)
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 结果
     */
    public int insertMsgDingTeam(MsgDingTeam msgDingTeam);

    /**
     * 修改钉钉团队(企业)
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 结果
     */
    public int updateMsgDingTeam(MsgDingTeam msgDingTeam);

    /**
     * 批量删除钉钉团队(企业)
     * 
     * @param ids 需要删除的钉钉团队(企业)ID
     * @return 结果
     */
    public int deleteMsgDingTeamByIds(String[] ids);

    /**
     * 删除钉钉团队(企业)信息
     * 
     * @param id 钉钉团队(企业)ID
     * @return 结果
     */
    public int deleteMsgDingTeamById(String id);

    /**
     * 校验CorpId是否唯一
     * 
     * @param msgDingTeam
     * @return
     */
    public Boolean checkCorpIdUnique(MsgDingTeam msgDingTeam);
}
