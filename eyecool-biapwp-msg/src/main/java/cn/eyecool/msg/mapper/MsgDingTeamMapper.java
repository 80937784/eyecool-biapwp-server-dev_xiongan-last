package cn.eyecool.msg.mapper;

import java.util.List;

import cn.eyecool.msg.domain.MsgDingTeam;

/**
 * 钉钉团队(企业)Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgDingTeamMapper {
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
     * 删除钉钉团队(企业)
     * 
     * @param id 钉钉团队(企业)ID
     * @return 结果
     */
    public int deleteMsgDingTeamById(String id);

    /**
     * 批量删除钉钉团队(企业)
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgDingTeamByIds(String[] ids);

    /**
     * 检验corpId是否唯一
     * 
     * @param corpId
     * @return
     */
    public MsgDingTeam checkCorpIdUnique(String corpId);
}
