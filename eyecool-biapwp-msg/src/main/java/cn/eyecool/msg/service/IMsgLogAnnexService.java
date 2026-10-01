package cn.eyecool.msg.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.eyecool.msg.domain.MsgLogAnnex;

/**
 * 消息日志附件Service接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface IMsgLogAnnexService {
    /**
     * 查询消息日志附件
     * 
     * @param id 消息日志附件ID
     * @return 消息日志附件
     */
    public MsgLogAnnex selectMsgLogAnnexById(String id);

    /**
     * 查询消息日志附件列表
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 消息日志附件集合
     */
    public List<MsgLogAnnex> selectMsgLogAnnexList(MsgLogAnnex msgLogAnnex);

    /**
     * 新增消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    public int insertMsgLogAnnex(MsgLogAnnex msgLogAnnex);

    /**
     * 修改消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    public int updateMsgLogAnnex(MsgLogAnnex msgLogAnnex);

    /**
     * 批量删除消息日志附件
     * 
     * @param ids 需要删除的消息日志附件ID
     * @return 结果
     */
    public int deleteMsgLogAnnexByIds(String[] ids);

    /**
     * 删除消息日志附件信息
     * 
     * @param id 消息日志附件ID
     * @return 结果
     */
    public int deleteMsgLogAnnexById(String id);

    /**
     * 下载附件
     * 
     * @param id
     * @param request
     * @param response
     */
    public void downloadLogAnnex(String id, HttpServletRequest request, HttpServletResponse response);
}
