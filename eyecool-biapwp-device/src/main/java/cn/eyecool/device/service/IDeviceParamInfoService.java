package cn.eyecool.device.service;

import java.util.List;
import cn.eyecool.device.domain.DeviceParamInfo;

/**
 * 设备参数信息Service接口
 * 
 * @author admin
 * @date 2021-03-30
 */
public interface IDeviceParamInfoService 
{
    /**
     * 查询设备参数信息
     * 
     * @param id 设备参数信息ID
     * @return 设备参数信息
     */
    public DeviceParamInfo selectDeviceParamInfoById(String id);

    /**
     * 查询设备参数信息列表
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 设备参数信息集合
     */
    public List<DeviceParamInfo> selectDeviceParamInfoList(DeviceParamInfo deviceParamInfo);

    /**
     * 新增设备参数信息
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 结果
     */
    public int insertDeviceParamInfo(DeviceParamInfo deviceParamInfo);

    /**
     * 修改设备参数信息
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 结果
     */
    public int updateDeviceParamInfo(DeviceParamInfo deviceParamInfo);

    /**
     * 批量删除设备参数信息
     * 
     * @param ids 需要删除的设备参数信息ID
     * @return 结果
     */
    public int deleteDeviceParamInfoByIds(String[] ids);

    /**
     * 删除设备参数信息信息
     * 
     * @param id 设备参数信息ID
     * @return 结果
     */
    public int deleteDeviceParamInfoById(String id);
}
