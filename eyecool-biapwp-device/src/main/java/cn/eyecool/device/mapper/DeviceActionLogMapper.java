package cn.eyecool.device.mapper;

import java.util.List;
import cn.eyecool.device.domain.DeviceActionLog;

/**
 * 设备动作日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-14
 */
public interface DeviceActionLogMapper 
{
    /**
     * 查询设备动作日志
     * 
     * @param id 设备动作日志ID
     * @return 设备动作日志
     */
    public DeviceActionLog selectDeviceActionLogById(String id);

    /**
     * 查询设备动作日志列表
     * 
     * @param deviceActionLog 设备动作日志
     * @return 设备动作日志集合
     */
    public List<DeviceActionLog> selectDeviceActionLogList(DeviceActionLog deviceActionLog);

    /**
     * 新增设备动作日志
     * 
     * @param deviceActionLog 设备动作日志
     * @return 结果
     */
    public int insertDeviceActionLog(DeviceActionLog deviceActionLog);

    /**
     * 修改设备动作日志
     * 
     * @param deviceActionLog 设备动作日志
     * @return 结果
     */
    public int updateDeviceActionLog(DeviceActionLog deviceActionLog);

    /**
     * 删除设备动作日志
     * 
     * @param id 设备动作日志ID
     * @return 结果
     */
    public int deleteDeviceActionLogById(String id);

    /**
     * 批量删除设备动作日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceActionLogByIds(String[] ids);
}
