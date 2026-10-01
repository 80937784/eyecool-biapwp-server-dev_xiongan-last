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
import cn.eyecool.msg.domain.MsgMailProperty;
import cn.eyecool.msg.mapper.MsgMailPropertyMapper;
import cn.eyecool.msg.service.IMsgMailPropertyService;

/**
 * 邮箱配置Service业务层处理
 * 
 * @author admin
 * @date 2021-04-15
 */
@Service
public class MsgMailPropertyServiceImpl implements IMsgMailPropertyService {
    @Autowired
    private MsgMailPropertyMapper msgMailPropertyMapper;

    /**
     * 查询邮箱配置
     * 
     * @param id 邮箱配置ID
     * @return 邮箱配置
     */
    @Override
    public MsgMailProperty selectMsgMailPropertyById(String id) {
        return msgMailPropertyMapper.selectMsgMailPropertyById(id);
    }

    /**
     * 查询邮箱配置列表
     * 
     * @param msgMailProperty 邮箱配置
     * @return 邮箱配置
     */
    @Override
    public List<MsgMailProperty> selectMsgMailPropertyList(MsgMailProperty msgMailProperty) {
        return msgMailPropertyMapper.selectMsgMailPropertyList(msgMailProperty);
    }

    /**
     * 新增邮箱配置
     * 
     * @param msgMailProperty 邮箱配置
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMsgMailProperty(MsgMailProperty msgMailProperty) {
        if (!checkFromMailUnique(msgMailProperty)) {
            throw new CustomException(MessageUtils.message("msg.mail.service.mail.exists"));
        }
        try {
            msgMailProperty.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgMailProperty.setId(IdWorker.getNextStringId());
        msgMailProperty.setCreateTime(DateUtils.getNowDate());
        return msgMailPropertyMapper.insertMsgMailProperty(msgMailProperty);
    }

    /**
     * 修改邮箱配置
     * 
     * @param msgMailProperty 邮箱配置
     * @return 结果
     */
    @Override
    public int updateMsgMailProperty(MsgMailProperty msgMailProperty) {
        try {
            msgMailProperty.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        msgMailProperty.setUpdateTime(DateUtils.getNowDate());
        return msgMailPropertyMapper.updateMsgMailProperty(msgMailProperty);
    }

    /**
     * 批量删除邮箱配置
     * 
     * @param ids 需要删除的邮箱配置ID
     * @return 结果
     */
    @Override
    public int deleteMsgMailPropertyByIds(String[] ids) {
        return msgMailPropertyMapper.deleteMsgMailPropertyByIds(ids);
    }

    /**
     * 删除邮箱配置信息
     * 
     * @param id 邮箱配置ID
     * @return 结果
     */
    @Override
    public int deleteMsgMailPropertyById(String id) {
        return msgMailPropertyMapper.deleteMsgMailPropertyById(id);
    }

    /**
     * 校验发件邮箱是否唯一
     * 
     * @param msgMailProperty
     * @return
     */
    @Override
    public Boolean checkFromMailUnique(MsgMailProperty msgMailProperty) {
        String id = msgMailProperty.getId();
        MsgMailProperty info = msgMailPropertyMapper.checkFromMailUnique(msgMailProperty.getEmailAddr());
        return null == info || info.getId().equals(id);
    }
}
