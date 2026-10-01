package cn.eyecool.msg.service;

import java.util.List;

import cn.eyecool.msg.domain.MsgTemplate;

/**
 * 消息模板Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgTemplateService {
    /**
     * 查询消息模板
     * 
     * @param id 消息模板ID
     * @return 消息模板
     */
    public MsgTemplate selectMsgTemplateById(String id);

    /**
     * 查询消息模板列表
     * 
     * @param msgTemplate 消息模板
     * @return 消息模板集合
     */
    public List<MsgTemplate> selectMsgTemplateList(MsgTemplate msgTemplate);

    /**
     * 新增消息模板
     * 
     * @param msgTemplate 消息模板
     * @return 结果
     */
    public int insertMsgTemplate(MsgTemplate msgTemplate);

    /**
     * 修改消息模板
     * 
     * @param msgTemplate 消息模板
     * @return 结果
     */
    public int updateMsgTemplate(MsgTemplate msgTemplate);

    /**
     * 批量删除消息模板
     * 
     * @param ids 需要删除的消息模板ID
     * @return 结果
     */
    public int deleteMsgTemplateByIds(String[] ids);

    /**
     * 删除消息模板信息
     * 
     * @param id 消息模板ID
     * @return 结果
     */
    public int deleteMsgTemplateById(String id);

    /**
     * 校验模板名称是否唯一
     * 
     * @param msgTemplate
     * @return
     */
    public Boolean checkTemplateNameUnique(MsgTemplate msgTemplate);

    /**
     * 校验模板官方ID是否唯一
     * 
     * @param msgTemplate
     * @return
     */
    Boolean checkOfficalIdUnique(MsgTemplate msgTemplate);
}
