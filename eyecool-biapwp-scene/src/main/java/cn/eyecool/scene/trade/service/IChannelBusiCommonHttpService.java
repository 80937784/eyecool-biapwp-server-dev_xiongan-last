package cn.eyecool.scene.trade.service;

import java.util.List;
import java.util.Map;

import cn.eyecool.scene.domain.BasePersonLiveUpdateInfo;
import cn.eyecool.scene.trade.entity.PersonBusiOpen;
import cn.eyecool.scene.trade.entity.SubtreasuryBusiOperateParam;

/**
 * 场景公共业务HTTP服务层
 * 
 * @author admin
 * @date 2019年11月13日
 */
public interface IChannelBusiCommonHttpService {

    /**
     * 开通场景人员(人脸、指纹、虹膜、指静脉)
     * 
     * @param personBusiOpen
     * @param bioAttestType 生物特征类型[DictConstants.BioAttestType]
     */
    public void openPersonChannelBusi(PersonBusiOpen personBusiOpen, String bioAttestType);

    /**
     * 关闭场景人员(人脸、指纹、虹膜、指静脉)
     * 
     * @param personBusiOpen
     * @param bioAttestType 生物特征类型[DictConstants.BioAttestType]
     */
    public void closePersonChannelBusi(PersonBusiOpen personBusiOpen, String bioAttestType);

    /**
     * 操作子场景
     * 
     * @param channelSubtreasuryBusiSaveParam
     * @return
     */
    public Map<String, Object> operatePersonSubtreasury(SubtreasuryBusiOperateParam channelSubtreasuryBusiSaveParam);

    /**
     * 查询场景库人员是否存在
     * 
     * @param channelCode
     * @param uniqueId
     * @return
     */
    public boolean queryChannelPersonExists(String channelCode, String uniqueId);

    /**
     * 删除场景库人员
     * 
     * @param channelCode
     * @param uniqueId
     */
    public void deleteChannelPerson(String channelCode, String uniqueId);

    /**
     * 查询实时更新人员列表
     * 
     * @param map 人员信息
     * @return 人员管理集合
     */
    public List<BasePersonLiveUpdateInfo> selectLiveUpdateBasePersonInfoList(Map<String, Object> map);

}
