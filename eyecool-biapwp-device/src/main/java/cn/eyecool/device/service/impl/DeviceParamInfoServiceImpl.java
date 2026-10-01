package cn.eyecool.device.service.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.device.domain.DeviceParamInfo;
import cn.eyecool.device.mapper.DeviceParamInfoMapper;
import cn.eyecool.device.mapper.DeviceParamModelRelMapper;
import cn.eyecool.device.service.IDeviceParamInfoService;

/**
 * 设备参数信息Service业务层处理
 * 
 * @author admin
 * @date 2021-03-30
 */
@Service
public class DeviceParamInfoServiceImpl implements IDeviceParamInfoService {
    @Autowired
    private DeviceParamInfoMapper deviceParamInfoMapper;
    @Autowired
    private DeviceParamModelRelMapper deviceParamModelRelMapper;

    /**
     * 查询设备参数信息
     * 
     * @param id 设备参数信息ID
     * @return 设备参数信息
     */
    @Override
    public DeviceParamInfo selectDeviceParamInfoById(String id) {
        return deviceParamInfoMapper.selectDeviceParamInfoById(id);
    }

    /**
     * 查询设备参数信息列表
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 设备参数信息
     */
    @Override
    public List<DeviceParamInfo> selectDeviceParamInfoList(DeviceParamInfo deviceParamInfo) {
        return deviceParamInfoMapper.selectDeviceParamInfoList(deviceParamInfo);
    }

    /**
     * 新增设备参数信息
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceParamInfo(DeviceParamInfo deviceParamInfo) {
        // 查询参数编码是否已经存在
        DeviceParamInfo condition = new DeviceParamInfo();
        condition.setParamCode(deviceParamInfo.getParamCode());
        List<DeviceParamInfo> list = deviceParamInfoMapper.selectDeviceParamInfoList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("device.param.info.param.exists", deviceParamInfo.getParamCode()));
        }
        deviceParamInfo.setId(IdWorker.getNextStringId());
        try {
            deviceParamInfo.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceParamInfo.setCreateTime(DateUtils.getNowDate());
        return deviceParamInfoMapper.insertDeviceParamInfo(deviceParamInfo);
    }

    /**
     * 修改设备参数信息
     * 
     * @param deviceParamInfo 设备参数信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceParamInfo(DeviceParamInfo deviceParamInfo) {
        try {
            deviceParamInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceParamInfo.setUpdateTime(DateUtils.getNowDate());
        return deviceParamInfoMapper.updateDeviceParamInfo(deviceParamInfo);
    }

    /**
     * 批量删除设备参数信息
     * 
     * @param ids 需要删除的设备参数信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceParamInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteDeviceParamInfoById(id);
        }
        return result;
    }

    /**
     * 删除设备参数信息信息
     * 
     * @param id 设备参数信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceParamInfoById(String id) {
        // 删除设备型号和参数绑定关系
        DeviceParamInfo paramInfo = deviceParamInfoMapper.selectDeviceParamInfoById(id);
        deviceParamModelRelMapper.deleteParamModelRelByParamCode(paramInfo.getParamCode());
        return deviceParamInfoMapper.deleteDeviceParamInfoById(id);
    }
}
