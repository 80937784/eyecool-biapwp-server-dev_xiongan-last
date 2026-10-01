package cn.eyecool.device.service;

import java.util.List;
import cn.eyecool.device.domain.DeviceParamModelRel;

/**
 * 参数型号关系Service接口
 * 
 * @author admin
 * @date 2021-03-31
 */
public interface IDeviceParamModelRelService 
{
    /**
     * 查询参数型号关系
     * 
     * @param id 参数型号关系ID
     * @return 参数型号关系
     */
    public DeviceParamModelRel selectDeviceParamModelRelById(String id);

    /**
     * 查询参数型号关系列表
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 参数型号关系集合
     */
    public List<DeviceParamModelRel> selectDeviceParamModelRelList(DeviceParamModelRel deviceParamModelRel);

    /**
     * 新增参数型号关系
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 结果
     */
    public int insertDeviceParamModelRel(DeviceParamModelRel deviceParamModelRel);

    /**
     * 修改参数型号关系
     * 
     * @param deviceParamModelRel 参数型号关系
     * @return 结果
     */
    public int updateDeviceParamModelRel(DeviceParamModelRel deviceParamModelRel);

    /**
     * 批量删除参数型号关系
     * 
     * @param ids 需要删除的参数型号关系ID
     * @return 结果
     */
    public int deleteDeviceParamModelRelByIds(String[] ids);

    /**
     * 删除参数型号关系信息
     * 
     * @param id 参数型号关系ID
     * @return 结果
     */
    public int deleteDeviceParamModelRelById(String id);
}
