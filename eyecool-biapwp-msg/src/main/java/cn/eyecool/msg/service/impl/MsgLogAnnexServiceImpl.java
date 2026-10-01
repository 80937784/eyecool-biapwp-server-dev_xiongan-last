package cn.eyecool.msg.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.msg.domain.MsgLogAnnex;
import cn.eyecool.msg.mapper.MsgLogAnnexMapper;
import cn.eyecool.msg.service.IMsgLogAnnexService;

/**
 * 消息日志附件Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgLogAnnexServiceImpl implements IMsgLogAnnexService {

    private static final Logger LOG = LoggerFactory.getLogger(MsgLogAnnexServiceImpl.class);

    @Autowired
    private MsgLogAnnexMapper msgLogAnnexMapper;

    /**
     * 查询消息日志附件
     * 
     * @param id 消息日志附件ID
     * @return 消息日志附件
     */
    @Override
    public MsgLogAnnex selectMsgLogAnnexById(String id) {
        return msgLogAnnexMapper.selectMsgLogAnnexById(id);
    }

    /**
     * 查询消息日志附件列表
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 消息日志附件
     */
    @Override
    public List<MsgLogAnnex> selectMsgLogAnnexList(MsgLogAnnex msgLogAnnex) {
        return msgLogAnnexMapper.selectMsgLogAnnexList(msgLogAnnex);
    }

    /**
     * 新增消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    @Override
    public int insertMsgLogAnnex(MsgLogAnnex msgLogAnnex) {
        msgLogAnnex.setId(IdWorker.getNextStringId());
        msgLogAnnex.setCreateTime(DateUtils.getNowDate());
        return msgLogAnnexMapper.insertMsgLogAnnex(msgLogAnnex);
    }

    /**
     * 修改消息日志附件
     * 
     * @param msgLogAnnex 消息日志附件
     * @return 结果
     */
    @Override
    public int updateMsgLogAnnex(MsgLogAnnex msgLogAnnex) {
        return msgLogAnnexMapper.updateMsgLogAnnex(msgLogAnnex);
    }

    /**
     * 批量删除消息日志附件
     * 
     * @param ids 需要删除的消息日志附件ID
     * @return 结果
     */
    @Override
    public int deleteMsgLogAnnexByIds(String[] ids) {
        return msgLogAnnexMapper.deleteMsgLogAnnexByIds(ids);
    }

    /**
     * 删除消息日志附件信息
     * 
     * @param id 消息日志附件ID
     * @return 结果
     */
    @Override
    public int deleteMsgLogAnnexById(String id) {
        return msgLogAnnexMapper.deleteMsgLogAnnexById(id);
    }

    /**
     * 下载附件
     * 
     * @param id
     * @param request
     * @param response
     */
    @Override
    public void downloadLogAnnex(String id, HttpServletRequest request, HttpServletResponse response) {
        MsgLogAnnex annex = msgLogAnnexMapper.selectMsgLogAnnexById(id);
        if (null == annex) {
            throw new CustomException(MessageUtils.message("msg.logannex.service.file.not.exists"));
        }
        String path = annex.getFilePath();
        String filename = annex.getFileName();
        response.setContentType("multipart/form-data");
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + FileUtils.setFileDownloadHeader(request, filename));
            FileUtils.writeBytes(path, response.getOutputStream());
        } catch (Exception e) {
            LOG.error("Message attachment download error", e);
            throw new CustomException(e.getMessage());
        }

    }
}
