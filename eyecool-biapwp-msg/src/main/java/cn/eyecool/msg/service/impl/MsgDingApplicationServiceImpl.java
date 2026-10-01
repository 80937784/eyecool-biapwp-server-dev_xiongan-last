package cn.eyecool.msg.service.impl;

import java.util.List;

import org.aspectj.bridge.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.msg.configure.ding.DingTalkInstanceCache;
import cn.eyecool.msg.domain.MsgDingApplication;
import cn.eyecool.msg.mapper.MsgDingApplicationMapper;
import cn.eyecool.msg.service.IMsgDingApplicationService;

/**
 * 钉钉微应用Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgDingApplicationServiceImpl implements IMsgDingApplicationService {
    @Autowired
    private MsgDingApplicationMapper msgDingApplicationMapper;

    /**
     * 查询钉钉微应用
     * 
     * @param id 钉钉微应用ID
     * @return 钉钉微应用
     */
    @Override
    public MsgDingApplication selectMsgDingApplicationById(String id) {
        return msgDingApplicationMapper.selectMsgDingApplicationById(id);
    }

    /**
     * 查询钉钉微应用列表
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 钉钉微应用
     */
    @Override
    public List<MsgDingApplication> selectMsgDingApplicationList(MsgDingApplication msgDingApplication) {
        return msgDingApplicationMapper.selectMsgDingApplicationList(msgDingApplication);
    }

    /**
     * 新增钉钉微应用
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgDingApplication(MsgDingApplication msgDingApplication) {
        if (!this.checkAgentIdUnique(msgDingApplication)) {
            throw new CustomException(MessageUtils.message("msg.ding.service.agentid.exists"));
        }
        if (!this.checkAppKeyUnique(msgDingApplication)) {
            throw new CustomException(MessageUtils.message("msg.ding.service.appkey.exists"));
        }
        try {
            msgDingApplication.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgDingApplication.setId(IdWorker.getNextStringId());
        msgDingApplication.setCreateTime(DateUtils.getNowDate());
        int result = msgDingApplicationMapper.insertMsgDingApplication(msgDingApplication);
        DingTalkInstanceCache.refresh(Long.valueOf(msgDingApplication.getAgentId()), msgDingApplication.getAppKey(),
            msgDingApplication.getAppSecrect());
        return result;
    }

    /**
     * 修改钉钉微应用
     * 
     * @param msgDingApplication 钉钉微应用
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgDingApplication(MsgDingApplication msgDingApplication) {
        if (!this.checkAgentIdUnique(msgDingApplication)) {
            throw new CustomException(MessageUtils.message("msg.ding.service.agentid.exists"));
        }
        if (!this.checkAppKeyUnique(msgDingApplication)) {
            throw new CustomException(MessageUtils.message("msg.ding.service.appkey.exists"));
        }
        MsgDingApplication application =
            msgDingApplicationMapper.selectMsgDingApplicationById(msgDingApplication.getId());
        try {
            msgDingApplication.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgDingApplication.setUpdateTime(DateUtils.getNowDate());
        int result = msgDingApplicationMapper.updateMsgDingApplication(msgDingApplication);
        if (application.getAppKey().equals(msgDingApplication.getAppKey())) {
            DingTalkInstanceCache.refresh(Long.valueOf(msgDingApplication.getAgentId()), msgDingApplication.getAppKey(),
                msgDingApplication.getAppSecrect());
        } else {
            DingTalkInstanceCache.remove(application.getAppKey());
        }
        return result;
    }

    /**
     * 批量删除钉钉微应用
     * 
     * @param ids 需要删除的钉钉微应用ID
     * @return 结果
     */
    @Override
    public int deleteMsgDingApplicationByIds(String[] ids) {
        return msgDingApplicationMapper.deleteMsgDingApplicationByIds(ids);
    }

    /**
     * 删除钉钉微应用信息
     * 
     * @param id 钉钉微应用ID
     * @return 结果
     */
    @Override
    public int deleteMsgDingApplicationById(String id) {
        return msgDingApplicationMapper.deleteMsgDingApplicationById(id);
    }

    /**
     * 校验AgentId是否唯一
     */
    @Override
    public Boolean checkAgentIdUnique(MsgDingApplication msgDingApplication) {
        String id = msgDingApplication.getId();
        MsgDingApplication info = msgDingApplicationMapper.checkAgentIdUnique(msgDingApplication.getAgentId(),
            msgDingApplication.getCorpId());
        return null == info || info.getId().equals(id);
    }

    /**
     * 校验AppKey是否唯一
     */
    @Override
    public Boolean checkAppKeyUnique(MsgDingApplication msgDingApplication) {
        String id = msgDingApplication.getId();
        MsgDingApplication info =
            msgDingApplicationMapper.checkAppKeyUnique(msgDingApplication.getAppKey(), msgDingApplication.getCorpId());
        return null == info || info.getId().equals(id);
    }
}
