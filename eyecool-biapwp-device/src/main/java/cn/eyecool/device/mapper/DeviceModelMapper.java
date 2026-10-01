package cn.eyecool.device.mapper;

import java.util.List;
import cn.eyecool.device.domain.DeviceModel;

/**
 * 设备型号信息Mapper接口
 * 
 * @author admin
 * @date 2021-03-29
 */
public interface DeviceModelMapper 
{
    /**
     * 查询设备型号信息
     * 
     * @param id 设备型号信息ID
     * @return 设备型号信息
     */
    public DeviceModel selectDeviceModelById(String id);

    /**
     * 查询设备型号信息列表
     * 
     * @param deviceModel 设备型号信息
     * @return 设备型号信息集合
     */
    public List<DeviceModel> selectDeviceModelList(DeviceModel deviceModel);

    /**
     * 新增设备型号信息
     * 
     * @param deviceModel 设备型号信息
     * @return 结果
     */
    public int insertDeviceModel(DeviceModel deviceModel);

    /**
     * 修改设备型号信息
     * 
     * @param deviceModel 设备型号信息
     * @return 结果
     */
    public int updateDeviceModel(DeviceModel deviceModel);

    /**
     * 删除设备型号信息
     * 
     * @param id 设备型号信息ID
     * @return 结果
     */
    public int deleteDeviceModelById(String id);

    /**
     * 批量删除设备型号信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceModelByIds(String[] ids);
}
