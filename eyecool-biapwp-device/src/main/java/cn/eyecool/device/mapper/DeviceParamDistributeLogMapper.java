package cn.eyecool.device.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.device.domain.DeviceParamDistributeLog;

/**
 * 参数下发日志Mapper接口
 * 
 * @author admin
 * @date 2021-04-13
 */
@SuppressWarnings("deprecation")
public interface DeviceParamDistributeLogMapper {
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
     * 删除参数下发日志
     * 
     * @param id 参数下发日志ID
     * @return 结果
     */
    public int deleteDeviceParamDistributeLogById(String id);

    /**
     * 批量删除参数下发日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceParamDistributeLogByIds(String[] ids);

    /**
     * 获取最大排序索引
     * 
     * @return
     */
    @SqlParser(filter = true)
    public Long selectMaxSortIndex();

    /**
     * 根据sortIndex更新设备参数下发结果
     * 
     * @param sortIndex
     * @param distributeResult
     * @param updateTime
     */
    public void updateDistributeLogBySortIndex(@Param("sortIndex") Long sortIndex,
        @Param("distributeResult") String distributeResult, @Param("updateTime") Date updateTime);
}
