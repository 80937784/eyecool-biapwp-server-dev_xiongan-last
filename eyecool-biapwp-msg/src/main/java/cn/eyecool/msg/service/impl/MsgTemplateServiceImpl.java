package cn.eyecool.msg.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.msg.domain.MsgTemplate;
import cn.eyecool.msg.mapper.MsgTemplateMapper;
import cn.eyecool.msg.service.IMsgTemplateService;

/**
 * 消息模板Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgTemplateServiceImpl implements IMsgTemplateService {
    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    /**
     * 查询消息模板
     * 
     * @param id 消息模板ID
     * @return 消息模板
     */
    @Override
    public MsgTemplate selectMsgTemplateById(String id) {
        return msgTemplateMapper.selectMsgTemplateById(id);
    }

    /**
     * 查询消息模板列表
     * 
     * @param msgTemplate 消息模板
     * @return 消息模板
     */
    @Override
    public List<MsgTemplate> selectMsgTemplateList(MsgTemplate msgTemplate) {
        return msgTemplateMapper.selectMsgTemplateList(msgTemplate);
    }

    /**
     * 新增消息模板
     * 
     * @param msgTemplate 消息模板
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgTemplate(MsgTemplate msgTemplate) {
        try {
            msgTemplate.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgTemplate.setId(IdWorker.getNextStringId());
        msgTemplate.setCreateTime(DateUtils.getNowDate());
        return msgTemplateMapper.insertMsgTemplate(msgTemplate);
    }

    /**
     * 修改消息模板
     * 
     * @param msgTemplate 消息模板
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgTemplate(MsgTemplate msgTemplate) {
        try {
            msgTemplate.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgTemplate.setUpdateTime(DateUtils.getNowDate());
        return msgTemplateMapper.updateMsgTemplate(msgTemplate);
    }

    /**
     * 批量删除消息模板
     * 
     * @param ids 需要删除的消息模板ID
     * @return 结果
     */
    @Override
    public int deleteMsgTemplateByIds(String[] ids) {
        return msgTemplateMapper.deleteMsgTemplateByIds(ids);
    }

    /**
     * 删除消息模板信息
     * 
     * @param id 消息模板ID
     * @return 结果
     */
    @Override
    public int deleteMsgTemplateById(String id) {
        return msgTemplateMapper.deleteMsgTemplateById(id);
    }

    /**
     * 校验模板名称是否唯一
     */
    @Override
    public Boolean checkTemplateNameUnique(MsgTemplate msgTemplate) {
        String id = msgTemplate.getId();
        MsgTemplate info =
            msgTemplateMapper.checkTemplateNameUnique(msgTemplate.getTemplateName(), msgTemplate.getNoticeMethod());
        return null == info || info.getId().equals(id);
    }

    /**
     * 校验模板官方ID是否唯一
     */
    @Override
    public Boolean checkOfficalIdUnique(MsgTemplate msgTemplate) {
        String id = msgTemplate.getId();
        if (StringUtils.isBlank(msgTemplate.getOfficalId())) {
            return true;
        }
        MsgTemplate info =
            msgTemplateMapper.checkOfficalIdUnique(msgTemplate.getOfficalId(), msgTemplate.getNoticeMethod());
        return null == info || info.getId().equals(id);
    }
}
