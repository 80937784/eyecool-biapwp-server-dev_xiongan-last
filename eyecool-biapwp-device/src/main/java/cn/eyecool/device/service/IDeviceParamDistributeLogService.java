package cn.eyecool.device.service;

import java.util.List;

import cn.eyecool.device.domain.DeviceParamDistributeLog;

/**
 * 参数下发日志Service接口
 * 
 * @author admin
 * @date 2021-04-13
 */
public interface IDeviceParamDistributeLogService {
    /**
     * 查询参数下发日志
     * 
     * @param id 参数下发日志ID
     * @return 参数下发日志
     */
    public DeviceParamDistributeLog selectDeviceParamDistributeLogById(String id);

    /**
     * 查询参数下发日志列表
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 参数下发日志集合
     */
    public List<DeviceParamDistributeLog>
        selectDeviceParamDistributeLogList(DeviceParamDistributeLog deviceParamDistributeLog);

    /**
     * 新增参数下发日志
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 结果
     */
    public int insertDeviceParamDistributeLog(DeviceParamDistributeLog deviceParamDistributeLog);

    /**
     * 修改参数下发日志
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 结果
     */
    public int updateDeviceParamDistributeLog(DeviceParamDistributeLog deviceParamDistributeLog);

    /**
     * 批量删除参数下发日志
     * 
     * @param ids 需要删除的参数下发日志ID
     * @return 结果
     */
    public int deleteDeviceParamDistributeLogByIds(String[] ids);

    /**
     * 删除参数下发日志信息
     * 
     * @param id 参数下发日志ID
     * @return 结果
     */
    public int deleteDeviceParamDistributeLogById(String id);

    /**
     * 回写参数下发结果
     * 
     * @param sortIndex 排序
     * @param result 结果
     */
    public void updateParamDistributeResult(long sortIndex, boolean result);
}
