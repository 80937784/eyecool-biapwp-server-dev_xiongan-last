package cn.eyecool.statistic.service;

import java.util.List;

import cn.eyecool.statistic.domain.DeviceLogNumInfo;
import cn.eyecool.statistic.domain.StatisticDevice;

/**
 * 设备统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
public interface IDeviceStatisticService {

    /**
     * 查询所有设备数量
     * 
     * @return
     */
    public int countDevice();

    /**
     * 倒叙查询今日设备日志数量信息
     * 
     * @return
     */
    public List<DeviceLogNumInfo> getTodaySortedDeviceLogNum();

    /**
     * 倒叙查询今日设备人脸日志数量信息
     * 
     * @return
     */
    public List<DeviceLogNumInfo> getTodaySortedDeviceFaceLogNum();

    /**
     * 获取设备[日]数量趋势
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    public List<StatisticDevice> getDeviceNumTrend(String startDate, String endDate);

}
