package cn.eyecool.device.service.impl;

import java.util.List;
import cn.eyecool.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.eyecool.device.mapper.DeviceActionLogMapper;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.service.IDeviceActionLogService;

/**
 * 设备动作日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-14
 */
@Service
public class DeviceActionLogServiceImpl implements IDeviceActionLogService 
{
    @Autowired
    private DeviceActionLogMapper deviceActionLogMapper;

    /**
     * 查询设备动作日志
     * 
     * @param id 设备动作日志ID
     * @return 设备动作日志
     */
    @Override
    public DeviceActionLog selectDeviceActionLogById(String id)
    {
        return deviceActionLogMapper.selectDeviceActionLogById(id);
    }

    /**
     * 查询设备动作日志列表
     * 
     * @param deviceActionLog 设备动作日志
     * @return 设备动作日志
     */
    @Override
    public List<DeviceActionLog> selectDeviceActionLogList(DeviceActionLog deviceActionLog)
    {
        return deviceActionLogMapper.selectDeviceActionLogList(deviceActionLog);
    }

    /**
     * 新增设备动作日志
     * 
     * @param deviceActionLog 设备动作日志
     * @return 结果
     */
    @Override
    public int insertDeviceActionLog(DeviceActionLog deviceActionLog)
    {
        deviceActionLog.setCreateTime(DateUtils.getNowDate());
        return deviceActionLogMapper.insertDeviceActionLog(deviceActionLog);
    }

    /**
     * 修改设备动作日志
     * 
     * @param deviceActionLog 设备动作日志
     * @return 结果
     */
    @Override
    public int updateDeviceActionLog(DeviceActionLog deviceActionLog)
    {
        deviceActionLog.setUpdateTime(DateUtils.getNowDate());
        return deviceActionLogMapper.updateDeviceActionLog(deviceActionLog);
    }

    /**
     * 批量删除设备动作日志
     * 
     * @param ids 需要删除的设备动作日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceActionLogByIds(String[] ids)
    {
        return deviceActionLogMapper.deleteDeviceActionLogByIds(ids);
    }

    /**
     * 删除设备动作日志信息
     * 
     * @param id 设备动作日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceActionLogById(String id)
    {
        return deviceActionLogMapper.deleteDeviceActionLogById(id);
    }
}
