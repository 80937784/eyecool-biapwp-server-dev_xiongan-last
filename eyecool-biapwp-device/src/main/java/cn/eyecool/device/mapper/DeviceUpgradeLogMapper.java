package cn.eyecool.device.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.device.domain.DeviceUpgradeLog;

/**
 * 升级日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-09
 */
@SuppressWarnings("deprecation")
public interface DeviceUpgradeLogMapper {
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
     * 删除升级日志
     * 
     * @param id 升级日志ID
     * @return 结果
     */
    public int deleteDeviceUpgradeLogById(String id);

    /**
     * 批量删除升级日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceUpgradeLogByIds(String[] ids);

    /**
     * 根据升级任务主键跳过升级任务
     * 
     * @param taskIds 任务ID
     * @param updateTime 修改时间
     */
    public void skipDeviceUpgradeLogByTaskIds(@Param("taskIds") String[] taskIds, @Param("updateTime") Date updateTime);

    /**
     * 根据设备编码跳过升级任务
     * 
     * @param deviceNo 设备编码
     * @param updateTime 修改时间
     */
    @SqlParser(filter = true)
    public void skipUpgradeLogByDeviceNo(@Param("deviceNo") String deviceNo, @Param("updateTime") Date updateTime);

    /**
     * 根据版本信息跳过升级任务
     * 
     * @param appName app名称
     * @param appVersion app版本号
     * @param updateTime 修改时间
     */
    @SqlParser(filter = true)
    public void skipUpgradeLogByAppInfo(@Param("appName") String appName, @Param("appVersion") String appVersion,
        @Param("updateTime") Date updateTime);

}
