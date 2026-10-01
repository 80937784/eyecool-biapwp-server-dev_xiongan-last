package cn.eyecool.device.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.device.domain.DeviceBatchLog;
import cn.eyecool.device.mapper.DeviceBatchLogMapper;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.service.IDeviceBatchLogService;

/**
 * 设备批次Service业务层处理
 * 
 * @author admin
 * @date 2021-04-07
 */
@Service
public class DeviceBatchLogServiceImpl implements IDeviceBatchLogService {
    @Autowired
    private DeviceBatchLogMapper deviceBatchLogMapper;
    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 查询设备批次
     * 
     * @param id 设备批次ID
     * @return 设备批次
     */
    @Override
    public DeviceBatchLog selectDeviceBatchLogById(String id) {
        return deviceBatchLogMapper.selectDeviceBatchLogById(id);
    }

    /**
     * 查询设备批次列表
     * 
     * @param deviceBatchLog 设备批次
     * @return 设备批次
     */
    @Override
    public List<DeviceBatchLog> selectDeviceBatchLogList(DeviceBatchLog deviceBatchLog) {
        return deviceBatchLogMapper.selectDeviceBatchLogList(deviceBatchLog);
    }

    /**
     * 新增设备批次
     * 
     * @param deviceBatchLog 设备批次
     * @return 结果
     */
    @Override
    public int insertDeviceBatchLog(DeviceBatchLog deviceBatchLog) {
        deviceBatchLog.setCreateTime(DateUtils.getNowDate());
        try {
            deviceBatchLog.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        return deviceBatchLogMapper.insertDeviceBatchLog(deviceBatchLog);
    }

    /**
     * 修改设备批次
     * 
     * @param deviceBatchLog 设备批次
     * @return 结果
     */
    @Override
    public int updateDeviceBatchLog(DeviceBatchLog deviceBatchLog) {
        deviceBatchLog.setUpdateTime(DateUtils.getNowDate());
        return deviceBatchLogMapper.updateDeviceBatchLog(deviceBatchLog);
    }

    /**
     * 批量删除设备批次
     * 
     * @param ids 需要删除的设备批次ID
     * @return 结果
     */
    @Override
    public int deleteDeviceBatchLogByIds(String[] ids) {
        return deviceBatchLogMapper.deleteDeviceBatchLogByIds(ids);
    }

    /**
     * 删除设备批次信息
     * 
     * @param id 设备批次ID
     * @return 结果
     */
    @Override
    public int deleteDeviceBatchLogById(String id) {
        return deviceBatchLogMapper.deleteDeviceBatchLogById(id);
    }

    /**
     * 设备批次回滚
     * 
     * @param importBatchNums
     * @return
     */
    @Override
    @Transactional
    public int rollbackDeviceBatchByBatchNums(String[] importBatchNums) {
        deviceInfoMapper.deleteDeviceByImportBatchNums(importBatchNums);
        return deviceBatchLogMapper.updateBatchRollBackStatus(importBatchNums, DateUtils.getNowDate());
    }
}
