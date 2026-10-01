package cn.eyecool.statistic.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.statistic.domain.DeviceLogNumInfo;
import cn.eyecool.statistic.domain.StatisticDevice;
import cn.eyecool.statistic.mapper.DeviceStatisticMapper;
import cn.eyecool.statistic.mapper.TradeLogStatisticMapper;
import cn.eyecool.statistic.service.IDeviceStatisticService;

/**
 * 设备信息统计业务层
 * 
 * @author mawj
 * @date 2021/08/26
 */
@Service
public class DeviceStatisticServiceImpl implements IDeviceStatisticService {

    @Autowired
    private DeviceStatisticMapper deviceStatisticMapper;
    @Autowired
    private TradeLogStatisticMapper tradeLogStatisticMapper;

    /**
     * 查询所有设备数量
     * 
     * @return
     */
    @Override
    public int countDevice() {
        return deviceStatisticMapper.countDevice();
    }

    /**
     * 倒叙查询今日设备日志数量信息
     * 
     * @return
     */
    @Override
    public List<DeviceLogNumInfo> getTodaySortedDeviceLogNum() {
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        List<DeviceLogNumInfo> face1vNDeviceLogNumList =
            tradeLogStatisticMapper.getFace1vNDeviceLogNumInfo(startTime, endTime, null);
        List<DeviceLogNumInfo> irisface1vNDeviceLogNumList =
            tradeLogStatisticMapper.getIrisFace1vNDeviceLogNumInfo(startTime, endTime, null);
        List<DeviceLogNumInfo> list = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(face1vNDeviceLogNumList)) {
            list.addAll(face1vNDeviceLogNumList);
        }
        if (CollectionUtils.isNotEmpty(irisface1vNDeviceLogNumList)) {
            irisface1vNDeviceLogNumList.stream().forEach(it -> {
                DeviceLogNumInfo info =
                    list.stream().filter(el -> el.getDeviceNo().equals(it.getDeviceNo())).findFirst().orElse(null);
                if (null == info) {
                    list.add(it);
                } else {
                    info.setLogNum(info.getLogNum() + it.getLogNum());
                }
            });
        }
        return list.stream().sorted(Comparator.comparing(DeviceLogNumInfo::getLogNum).reversed()).limit(5L)
            .collect(Collectors.toList());
    }

    /**
     * 倒叙查询今日设备人脸日志数量信息
     * 
     * @return
     */
    @Override
    public List<DeviceLogNumInfo> getTodaySortedDeviceFaceLogNum() {
        Date startTime = DateUtils.getTodayStartTime();
        Date endTime = DateUtils.getNowDate();
        List<DeviceLogNumInfo> face1vNDeviceLogNumList =
            tradeLogStatisticMapper.getFace1vNDeviceLogNumInfo(startTime, endTime, null);
        List<DeviceLogNumInfo> face1v1DeviceLogNumList =
            tradeLogStatisticMapper.getFace1v1DeviceLogNumInfo(startTime, endTime, null);
        List<DeviceLogNumInfo> list = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(face1vNDeviceLogNumList)) {
            list.addAll(face1vNDeviceLogNumList);
        }
        if (CollectionUtils.isNotEmpty(face1v1DeviceLogNumList)) {
            face1v1DeviceLogNumList.stream().forEach(it -> {
                DeviceLogNumInfo info =
                    list.stream().filter(el -> el.getDeviceNo().equals(it.getDeviceNo())).findFirst().orElse(null);
                if (null == info) {
                    list.add(it);
                } else {
                    info.setLogNum(info.getLogNum() + it.getLogNum());
                }
            });
        }
        return list.stream().sorted(Comparator.comparing(DeviceLogNumInfo::getLogNum).reversed()).limit(5L)
            .collect(Collectors.toList());
    }

    /**
     * 获取设备[日]数量趋势
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<StatisticDevice> getDeviceNumTrend(String startDate, String endDate) {
        return deviceStatisticMapper.getDeviceNumTrend(startDate, endDate);
    }

}
