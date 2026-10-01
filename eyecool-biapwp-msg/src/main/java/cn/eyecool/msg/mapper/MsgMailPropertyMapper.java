package cn.eyecool.msg.mapper;

import java.util.List;

import cn.eyecool.msg.domain.MsgMailProperty;

/**
 * 邮箱配置Mapper接口
 * 
 * @author admin
 * @date 2021-04-15
 */
public interface MsgMailPropertyMapper {
    /**
     * 查询邮箱配置
     * 
     * @param id 邮箱配置ID
     * @return 邮箱配置
     */
    public MsgMailProperty selectMsgMailPropertyById(String id);

    /**
     * 查询邮箱配置列表
     * 
     * @param msgMailProperty 邮箱配置
     * @return 邮箱配置集合
     */
    public List<MsgMailProperty> selectMsgMailPropertyList(MsgMailProperty msgMailProperty);

    /**
     * 新增邮箱配置
     * 
     * @param msgMailProperty 邮箱配置
     * @return 结果
     */
    public int insertMsgMailProperty(MsgMailProperty msgMailProperty);

    /**
     * 修改邮箱配置
     * 
     * @param msgMailProperty 邮箱配置
     * @return 结果
     */
    public int updateMsgMailProperty(MsgMailProperty msgMailProperty);

    /**
     * 删除邮箱配置
     * 
     * @param id 邮箱配置ID
     * @return 结果
     */
    public int deleteMsgMailPropertyById(String id);

    /**
     * 批量删除邮箱配置
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMsgMailPropertyByIds(String[] ids);

    /**
     * 校验发件邮箱是否唯一
     * 
     * @param emailAddr
     * @return
     */
    public MsgMailProperty checkFromMailUnique(String emailAddr);
}
