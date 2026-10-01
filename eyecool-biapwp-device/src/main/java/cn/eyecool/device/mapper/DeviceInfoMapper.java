package cn.eyecool.device.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.device.domain.DeviceInfo;

/**
 * 设备信息Mapper接口
 * 
 * @author admin
 * @date 2021-04-01
 */
@SuppressWarnings("deprecation")
public interface DeviceInfoMapper {
    /**
     * 查询设备信息
     * 
     * @param id 设备信息ID
     * @return 设备信息
     */
    public DeviceInfo selectDeviceInfoById(String id);

    /**
     * 查询设备信息列表
     * 
     * @param deviceInfo 设备信息
     * @return 设备信息集合
     */
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo);

    /**
     * 新增设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    public int insertDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 修改设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    public int updateDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 删除设备信息
     * 
     * @param id 设备信息ID
     * @return 结果
     */
    public int deleteDeviceInfoById(String id);

    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceInfoByIds(String[] ids);

    /**
     * 校验设备编码是否唯一
     * 
     * @param deviceNo
     * @return
     */
    @SqlParser(filter = true)
    public DeviceInfo checkDeviceNoUnique(String deviceNo);

    /**
     * 根据设备型号查询设备数量
     * 
     * @param deviceModelCode
     * @return
     */
    @SqlParser(filter = true)
    public int selectDeviceCountByModelCode(String deviceModelCode);

    /**
     * 更新设备mqtt认证信息
     * 
     * @param info
     * @return
     */
    public int updateMqttAuthInfo(DeviceInfo info);

    /**
     * 按照批次号删除设备
     * 
     * @param importBatchNums
     * @return
     */
    @SqlParser(filter = true)
    public int deleteDeviceByImportBatchNums(String[] importBatchNums);

    /**
     * 查询待升级的设备列表
     * 
     * @param versionId
     * @param deviceInfo
     * @return
     */
    public List<DeviceInfo> listUpgradeDevice(@Param("versionId") String versionId,
        @Param("deviceInfo") DeviceInfo deviceInfo);

    /**
     * 根据子场景编码设置设备全量拉取数据标志
     * 
     * @param subCode
     */
    public void resetPullAllDataFlagBySubCode(String subCode);

    /**
     * 根据场景编码设置设备全量拉取数据标志
     * 
     * @param channelCode
     */
    public void resetPullAllDataFlagByChannelCode(String channelCode);

    /**
     * 根据设备编码取消设备全量拉取数据标志
     * 
     * @param deviceNo
     */
    public void cancelPullAllDataFlagByDeviceNo(String deviceNo);
}
