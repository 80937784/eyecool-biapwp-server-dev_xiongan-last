package cn.eyecool.device.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.domain.DeviceParamModelRel;
import cn.eyecool.device.mapper.DeviceParamModelRelMapper;
import cn.eyecool.device.service.IDeviceParamModelRelService;

/**
 * 参数型号关系Service业务层处理
 * 
 * @author admin
 * @date 2021-03-31
 */
@Service
public class DeviceParamModelRelServiceImpl implements IDeviceParamModelRelService {
    @Autowired
    private DeviceParamModelRelMapper deviceParamModelRelMapper;

    /**
     * 查询参数型号关系
     * 
     * @param id 参数型号关系ID
     * @return 参数型号关系
     */
    @Override
    public DeviceParamModelRel selectDeviceParamModelRelById(String id) {
        return deviceParamModelRelMapper.selectDeviceParamModelRelById(id);
    }

    /**
     * 查询参数型号关系列表
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 参数型号关系
     */
    @Override
    public List<DeviceParamModelRel> selectDeviceParamModelRelList(DeviceParamModelRel deviceParamModelRel) {
        return deviceParamModelRelMapper.selectDeviceParamModelRelList(deviceParamModelRel);
    }

    /**
     * 新增参数型号关系
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceParamModelRel(DeviceParamModelRel deviceParamModelRel) {
        DeviceParamModelRel relation = new DeviceParamModelRel();
        relation.setModelCode(deviceParamModelRel.getModelCode());
        relation.setCreateTime(DateUtils.getNowDate());
        int result = 0;
        String paramCodes = deviceParamModelRel.getParamCode();
        for (String paramCode : Convert.toStrArray(paramCodes)) {
            relation.setId(IdWorker.getNextStringId());
            relation.setParamCode(paramCode);
            result += deviceParamModelRelMapper.insertDeviceParamModelRel(relation);
        }
        return result;
    }

    /**
     * 修改参数型号关系
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 结果
     */
    @Override
    public int updateDeviceParamModelRel(DeviceParamModelRel deviceParamModelRel) {
        deviceParamModelRel.setUpdateTime(DateUtils.getNowDate());
        return deviceParamModelRelMapper.updateDeviceParamModelRel(deviceParamModelRel);
    }

    /**
     * 批量删除参数型号关系
     * 
     * @param ids 需要删除的参数型号关系ID
     * @return 结果
     */
    @Override
    public int deleteDeviceParamModelRelByIds(String[] ids) {
        return deviceParamModelRelMapper.deleteDeviceParamModelRelByIds(ids);
    }

    /**
     * 删除参数型号关系信息
     * 
     * @param id 参数型号关系ID
     * @return 结果
     */
    @Override
    public int deleteDeviceParamModelRelById(String id) {
        return deviceParamModelRelMapper.deleteDeviceParamModelRelById(id);
    }
}
