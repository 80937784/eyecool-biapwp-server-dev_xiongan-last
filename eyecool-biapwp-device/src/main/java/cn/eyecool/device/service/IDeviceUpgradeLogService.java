package cn.eyecool.device.service;

import java.util.List;
import cn.eyecool.device.domain.DeviceUpgradeLog;

/**
 * 升级日志Service接口
 * 
 * @author admin
 * @date 2021-04-09
 */
public interface IDeviceUpgradeLogService 
{
    /**
     * 查询升级日志
     * 
     * @param id 升级日志ID
     * @return 升级日志
     */
    public DeviceUpgradeLog selectDeviceUpgradeLogById(String id);

    /**
     * 查询升级日志列表
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 升级日志集合
     */
    public List<DeviceUpgradeLog> selectDeviceUpgradeLogList(DeviceUpgradeLog deviceUpgradeLog);

    /**
     * 新增升级日志
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 结果
     */
    public int insertDeviceUpgradeLog(DeviceUpgradeLog deviceUpgradeLog);

    /**
     * 修改升级日志
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 结果
     */
    public int updateDeviceUpgradeLog(DeviceUpgradeLog deviceUpgradeLog);

    /**
     * 批量删除升级日志
     * 
     * @param ids 需要删除的升级日志ID
     * @return 结果
     */
    public int deleteDeviceUpgradeLogByIds(String[] ids);

    /**
     * 删除升级日志信息
     * 
     * @param id 升级日志ID
     * @return 结果
     */
    public int deleteDeviceUpgradeLogById(String id);
}
