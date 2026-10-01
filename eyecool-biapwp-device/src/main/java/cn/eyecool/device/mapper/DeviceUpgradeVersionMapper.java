package cn.eyecool.device.mapper;

import java.util.List;
import cn.eyecool.device.domain.DeviceUpgradeVersion;

/**
 * 版本信息Mapper接口
 * 
 * @author admin
 * @date 2021-04-07
 */
public interface DeviceUpgradeVersionMapper 
{
    /**
     * 查询版本信息
     * 
     * @param id 版本信息ID
     * @return 版本信息
     */
    public DeviceUpgradeVersion selectDeviceUpgradeVersionById(String id);

    /**
     * 查询版本信息列表
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 版本信息集合
     */
    public List<DeviceUpgradeVersion> selectDeviceUpgradeVersionList(DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 新增版本信息
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    public int insertDeviceUpgradeVersion(DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 修改版本信息
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    public int updateDeviceUpgradeVersion(DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 删除版本信息
     * 
     * @param id 版本信息ID
     * @return 结果
     */
    public int deleteDeviceUpgradeVersionById(String id);

    /**
     * 批量删除版本信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceUpgradeVersionByIds(String[] ids);
}
