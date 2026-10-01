package cn.eyecool.device.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.device.domain.DeviceUpgradeTask;

/**
 * 升级任务Mapper接口
 * 
 * @author admin
 * @date 2021-04-08
 */
@SuppressWarnings("deprecation")
public interface DeviceUpgradeTaskMapper {
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
     * 删除升级任务
     * 
     * @param id 升级任务ID
     * @return 结果
     */
    public int deleteDeviceUpgradeTaskById(String id);

    /**
     * 批量删除升级任务
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceUpgradeTaskByIds(String[] ids);

    /**
     * 查询最大的任务排序
     * 
     * @return
     */
    @SqlParser(filter = true)
    public Long selectMaxTaskIndex();

    /**
     * 根据设备ID跳过所有待执行任务
     * 
     * @param deviceId
     * @param updateTime 修改时间
     */
    @SqlParser(filter = true)
    public void skipTaskByDeviceId(@Param("deviceId") String deviceId, @Param("updateTime") Date updateTime);

    /**
     * 根据设备ID跳过所有待执行任务
     * 
     * @param versionId
     * @param updateTime 修改时间
     */
    @SqlParser(filter = true)
    public void skipTaskByVersionId(@Param("versionId") String versionId, @Param("updateTime") Date updateTime);

    /**
     * 根据任务ID跳过升级任务
     * 
     * @param taskIds
     * @param updateTime
     */
    public void skipTaskByTaskIds(@Param("taskIds") String[] taskIds, @Param("updateTime") Date updateTime);

    @SqlParser(filter = true)
    public List<DeviceUpgradeTask> selectToBeAutoPublishedTaskList();
}
