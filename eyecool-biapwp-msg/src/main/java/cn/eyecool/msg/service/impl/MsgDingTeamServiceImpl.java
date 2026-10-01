package cn.eyecool.msg.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.msg.domain.MsgDingTeam;
import cn.eyecool.msg.mapper.MsgDingTeamMapper;
import cn.eyecool.msg.service.IMsgDingTeamService;

/**
 * 钉钉团队(企业)Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgDingTeamServiceImpl implements IMsgDingTeamService {
    @Autowired
    private MsgDingTeamMapper msgDingTeamMapper;

    /**
     * 查询钉钉团队(企业)
     * 
     * @param id 钉钉团队(企业)ID
     * @return 钉钉团队(企业)
     */
    @Override
    public MsgDingTeam selectMsgDingTeamById(String id) {
        return msgDingTeamMapper.selectMsgDingTeamById(id);
    }

    /**
     * 查询钉钉团队(企业)列表
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 钉钉团队(企业)
     */
    @Override
    public List<MsgDingTeam> selectMsgDingTeamList(MsgDingTeam msgDingTeam) {
        return msgDingTeamMapper.selectMsgDingTeamList(msgDingTeam);
    }

    /**
     * 新增钉钉团队(企业)
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgDingTeam(MsgDingTeam msgDingTeam) {
        if (!checkCorpIdUnique(msgDingTeam)) {
            throw new CustomException(cn.eyecool.common.utils.MessageUtils.message("msg.ding.team.service.corpid.exists"));
        }
        try {
            msgDingTeam.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgDingTeam.setId(IdWorker.getNextStringId());
        msgDingTeam.setCreateTime(DateUtils.getNowDate());
        return msgDingTeamMapper.insertMsgDingTeam(msgDingTeam);
    }

    /**
     * 修改钉钉团队(企业)
     * 
     * @param msgDingTeam 钉钉团队(企业)
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgDingTeam(MsgDingTeam msgDingTeam) {
        if (!checkCorpIdUnique(msgDingTeam)) {
            throw new CustomException(MessageUtils.message("msg.ding.team.service.corpid.exists"));
        }
        try {
            msgDingTeam.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgDingTeam.setUpdateTime(DateUtils.getNowDate());
        return msgDingTeamMapper.updateMsgDingTeam(msgDingTeam);
    }

    /**
     * 批量删除钉钉团队(企业)
     * 
     * @param ids 需要删除的钉钉团队(企业)ID
     * @return 结果
     */
    @Override
    public int deleteMsgDingTeamByIds(String[] ids) {
        return msgDingTeamMapper.deleteMsgDingTeamByIds(ids);
    }

    /**
     * 删除钉钉团队(企业)信息
     * 
     * @param id 钉钉团队(企业)ID
     * @return 结果
     */
    @Override
    public int deleteMsgDingTeamById(String id) {
        return msgDingTeamMapper.deleteMsgDingTeamById(id);
    }

    /**
     * 校验CorpId是否唯一
     * 
     * @param msgDingTeam
     * @return
     */
    @Override
    public Boolean checkCorpIdUnique(MsgDingTeam msgDingTeam) {
        String id = msgDingTeam.getId();
        MsgDingTeam info = msgDingTeamMapper.checkCorpIdUnique(msgDingTeam.getCorpId());
        return null == info || info.getId().equals(id);
    }
}
