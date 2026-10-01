package cn.eyecool.device.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.domain.DeviceUpgradeLog;
import cn.eyecool.device.mapper.DeviceUpgradeLogMapper;
import cn.eyecool.device.service.IDeviceUpgradeLogService;

/**
 * 升级日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-09
 */
@Service
public class DeviceUpgradeLogServiceImpl implements IDeviceUpgradeLogService {

    @Autowired
    private DeviceUpgradeLogMapper deviceUpgradeLogMapper;

    /**
     * 查询升级日志
     * 
     * @param id 升级日志ID
     * @return 升级日志
     */
    @Override
    public DeviceUpgradeLog selectDeviceUpgradeLogById(String id) {
        return deviceUpgradeLogMapper.selectDeviceUpgradeLogById(id);
    }

    /**
     * 查询升级日志列表
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 升级日志
     */
    @Override
    public List<DeviceUpgradeLog> selectDeviceUpgradeLogList(DeviceUpgradeLog deviceUpgradeLog) {
        return deviceUpgradeLogMapper.selectDeviceUpgradeLogList(deviceUpgradeLog);
    }

    /**
     * 新增升级日志
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceUpgradeLog(DeviceUpgradeLog deviceUpgradeLog) {
        deviceUpgradeLog.setId(IdWorker.getNextStringId());
        deviceUpgradeLog.setCreateTime(DateUtils.getNowDate());
        return deviceUpgradeLogMapper.insertDeviceUpgradeLog(deviceUpgradeLog);
    }

    /**
     * 修改升级日志
     * 
     * @param deviceUpgradeLog 升级日志
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceUpgradeLog(DeviceUpgradeLog deviceUpgradeLog) {
        deviceUpgradeLog.setUpdateTime(DateUtils.getNowDate());
        return deviceUpgradeLogMapper.updateDeviceUpgradeLog(deviceUpgradeLog);
    }

    /**
     * 批量删除升级日志
     * 
     * @param ids 需要删除的升级日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceUpgradeLogByIds(String[] ids) {
        return deviceUpgradeLogMapper.deleteDeviceUpgradeLogByIds(ids);
    }

    /**
     * 删除升级日志信息
     * 
     * @param id 升级日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceUpgradeLogById(String id) {
        return deviceUpgradeLogMapper.deleteDeviceUpgradeLogById(id);
    }
}
