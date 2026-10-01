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
import cn.eyecool.msg.domain.MsgSmsCloudAccount;
import cn.eyecool.msg.mapper.MsgSmsCloudAccountMapper;
import cn.eyecool.msg.mapper.MsgTemplateMapper;
import cn.eyecool.msg.service.IMsgSmsCloudAccountService;

/**
 * 短信云账户Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgSmsCloudAccountServiceImpl implements IMsgSmsCloudAccountService {
    @Autowired
    private MsgSmsCloudAccountMapper msgSmsCloudAccountMapper;
    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    /**
     * 查询短信云账户
     * 
     * @param id 短信云账户ID
     * @return 短信云账户
     */
    @Override
    public MsgSmsCloudAccount selectMsgSmsCloudAccountById(String id) {
        return msgSmsCloudAccountMapper.selectMsgSmsCloudAccountById(id);
    }

    /**
     * 查询短信云账户列表
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 短信云账户
     */
    @Override
    public List<MsgSmsCloudAccount> selectMsgSmsCloudAccountList(MsgSmsCloudAccount msgSmsCloudAccount) {
        return msgSmsCloudAccountMapper.selectMsgSmsCloudAccountList(msgSmsCloudAccount);
    }

    /**
     * 新增短信云账户
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 结果
     */
    @Override
    public int insertMsgSmsCloudAccount(MsgSmsCloudAccount msgSmsCloudAccount) {
        if (!checkAppIdUnique(msgSmsCloudAccount)) {
            throw new CustomException(MessageUtils.message("msg.sms.service.appid.exists"));
        }
        try {
            msgSmsCloudAccount.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgSmsCloudAccount.setId(IdWorker.getNextStringId());
        msgSmsCloudAccount.setCreateTime(DateUtils.getNowDate());
        return msgSmsCloudAccountMapper.insertMsgSmsCloudAccount(msgSmsCloudAccount);
    }

    /**
     * 修改短信云账户
     * 
     * @param msgSmsCloudAccount 短信云账户
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMsgSmsCloudAccount(MsgSmsCloudAccount msgSmsCloudAccount) {
        try {
            msgSmsCloudAccount.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgSmsCloudAccount.setUpdateTime(DateUtils.getNowDate());
        return msgSmsCloudAccountMapper.updateMsgSmsCloudAccount(msgSmsCloudAccount);
    }

    /**
     * 批量删除短信云账户
     * 
     * @param ids 需要删除的短信云账户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgSmsCloudAccountByIds(String[] ids) {
        int count = 0;
        for (String id : ids) {
            count += deleteMsgSmsCloudAccountById(id);
        }
        return count;
    }

    /**
     * 删除短信云账户信息
     * 
     * @param id 短信云账户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMsgSmsCloudAccountById(String id) {
        // 删除关联的模板
        msgTemplateMapper.deleteTemplateByAccountId(id);
        return msgSmsCloudAccountMapper.deleteMsgSmsCloudAccountById(id);
    }

    /**
     * 校验云账户名字是否唯一
     * 
     * @param msgSmsCloudAccount
     * @return
     */
    @Override
    public Boolean checkAppNameUnique(MsgSmsCloudAccount msgSmsCloudAccount) {
        String id = msgSmsCloudAccount.getId();
        MsgSmsCloudAccount info = msgSmsCloudAccountMapper.checkAppNameUnique(msgSmsCloudAccount.getAppName());
        return null == info || info.getId().equals(id);
    }

    /**
     * 校验云账户AppId是否唯一
     * 
     * @param msgSmsCloudAccount
     * @return
     */
    @Override
    public Boolean checkAppIdUnique(MsgSmsCloudAccount msgSmsCloudAccount) {
        String id = msgSmsCloudAccount.getId();
        MsgSmsCloudAccount info = msgSmsCloudAccountMapper.checkAppIdUnique(msgSmsCloudAccount.getAppId());
        return null == info || info.getId().equals(id);
    }
}
