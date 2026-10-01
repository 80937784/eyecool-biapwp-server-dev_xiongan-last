package cn.eyecool.msg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.eyecool.msg.domain.MsgTemplate;

/**
 * 消息模板Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgTemplateMapper {
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
     * 删除消息模板
     * 
     * @param id 消息模板ID
     * @return 结果
     */
    public int deleteMsgTemplateById(String id);

    /**
     * 批量删除消息模板
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgTemplateByIds(String[] ids);

    /**
     * 根据公众号主键删除模板
     * 
     * @param officalAccountId
     */
    public void deleteTemplateByAccountId(String officalAccountId);

    /**
     * 校验模板名称是否唯一
     * 
     * @param templateName
     * @param noticeMethod
     * @return
     */
    public MsgTemplate checkTemplateNameUnique(@Param("templateName") String templateName,
        @Param("noticeMethod") String noticeMethod);

    /**
     * 校验模板官方ID是否唯一
     * 
     * @param officalId
     * @param noticeMethod
     * @return
     */
    public MsgTemplate checkOfficalIdUnique(@Param("officalId") String officalId,
        @Param("noticeMethod") String noticeMethod);

}
