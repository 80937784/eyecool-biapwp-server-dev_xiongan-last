package cn.eyecool.device.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.eyecool.device.domain.DeviceUpgradeTask;

/**
 * 升级任务Service接口
 * 
 * @author admin
 * @date 2021-04-08
 */
public interface IDeviceUpgradeTaskService {
    /**
     * 查询升级任务
     * 
     * @param id 升级任务ID
     * @return 升级任务
     */
    public DeviceUpgradeTask selectDeviceUpgradeTaskById(String id);

    /**
     * 查询升级任务列表
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 升级任务集合
     */
    public List<DeviceUpgradeTask> selectDeviceUpgradeTaskList(DeviceUpgradeTask deviceUpgradeTask);

    /**
     * 新增升级任务
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 结果
     */
    public int insertDeviceUpgradeTask(DeviceUpgradeTask deviceUpgradeTask);

    /**
     * 修改升级任务
     * 
     * @param deviceUpgradeTask 升级任务
     * @return 结果
     */
    public int updateDeviceUpgradeTask(DeviceUpgradeTask deviceUpgradeTask);

    /**
     * 批量删除升级任务
     * 
     * @param ids 需要删除的升级任务ID
     * @return 结果
     */
    public int deleteDeviceUpgradeTaskByIds(String[] ids);

    /**
     * 删除升级任务信息
     * 
     * @param id 升级任务ID
     * @return 结果
     */
    public int deleteDeviceUpgradeTaskById(String id);

    /**
     * 保存升级结果
     * 
     * @param versionName 版本名称
     * @param beforeVersion 升级前版本号
     * @param updateVersion 升级后版本号
     * @param deviceNo 设备编码
     * @param updateResult 升级结果
     * @param startTime 升级时间
     * @param timeUsed 耗时
     * @param failReason 失败原因
     */
    public void saveUpgradeResult(String versionName, String beforeVersion, String updateVersion, String deviceNo,
        boolean updateResult, String startTime, long timeUsed, String failReason);

    /**
     * 下载程序版本
     * 
     * @param versionName 版本名称
     * @param version 版本号
     * @param deviceNo 设备编码
     * @param request 请求
     * @param response 响应
     */
    void downloadVersion(String versionName, String version, String deviceNo, HttpServletRequest request,
        HttpServletResponse response);

    /**
     * 检测设备最新升级任务
     * 
     * @param deviceId 设备主键
     */
    public DeviceUpgradeTask checkDeviceLastUpgradeTask(String deviceId);

    /**
     * 升级任务发布
     * 
     * @param id
     * @return
     */
    public int publish(String id);

}
