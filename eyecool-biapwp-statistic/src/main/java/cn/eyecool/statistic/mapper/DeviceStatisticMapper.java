package cn.eyecool.statistic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.statistic.domain.StatisticDevice;
import cn.eyecool.statistic.domain.TenantDeviceCount;

/**
 * 设备信息统计数据层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@SuppressWarnings("deprecation")
public interface DeviceStatisticMapper {

    /**
     * 查询设备数量
     * 
     * @return
     */
    public int countDevice();

    /**
     * 获取设备数量趋势
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticDevice> getDeviceNumTrend(@Param("startDate") String startDate,
        @Param("endDate") String endDate);

    /**
     * 根据租户查询设备数量信息
     * 
     * @return
     */
    @SqlParser(filter = true)
    public List<TenantDeviceCount> countDeviceByTenantId();

    /**
     * 根据统计日期查询设备统计信息
     * 
     * @param statisticDate
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public StatisticDevice getStatisticDeviceByDate(@Param("statisticDate") String statisticDate,
        @Param("tenantId") String tenantId);

    /**
     * 新增设备统计信息
     * 
     * @param statisticDevice
     */
    @SqlParser(filter = true)
    public int insertStatisticDevice(StatisticDevice statisticDevice);

}
