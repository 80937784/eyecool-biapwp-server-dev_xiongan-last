package cn.eyecool.device.mapper;

import java.util.List;
import cn.eyecool.device.domain.DeviceAccessAdapter;

/**
 * 203设备接入Mapper接口
 * 
 * @author admin
 * @date 2021-04-25
 */
public interface DeviceAccessAdapterMapper 
{
    /**
     * 查询203设备接入
     * 
     * @param id 203设备接入ID
     * @return 203设备接入
     */
    public DeviceAccessAdapter selectDeviceAccessAdapterById(String id);

    /**
     * 查询203设备接入列表
     * 
     * @param deviceAccessAdapter 203设备接入
     * @return 203设备接入集合
     */
    public List<DeviceAccessAdapter> selectDeviceAccessAdapterList(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 新增203设备接入
     * 
     * @param deviceAccessAdapter 203设备接入
     * @return 结果
     */
    public int insertDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 修改203设备接入
     * 
     * @param deviceAccessAdapter 203设备接入
     * @return 结果
     */
    public int updateDeviceAccessAdapter(DeviceAccessAdapter deviceAccessAdapter);

    /**
     * 删除203设备接入
     * 
     * @param id 203设备接入ID
     * @return 结果
     */
    public int deleteDeviceAccessAdapterById(String id);

    /**
     * 批量删除203设备接入
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceAccessAdapterByIds(String[] ids);
}
