package cn.eyecool.device.mapper;

import java.util.List;

import cn.eyecool.device.domain.DeviceParamModelRel;

/**
 * 参数型号关系Mapper接口
 * 
 * @author admin
 * @date 2021-03-31
 */
public interface DeviceParamModelRelMapper {
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
     * 删除参数型号关系
     * 
     * @param id 参数型号关系ID
     * @return 结果
     */
    public int deleteDeviceParamModelRelById(String id);

    /**
     * 批量删除参数型号关系
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDeviceParamModelRelByIds(String[] ids);

    /**
     * 根据设备型号删除型号参数关系
     * 
     * @param modelCode 型号编码
     */
    public void deleteParamModelRelByModelCode(String modelCode);

    /**
     * 根据参数编码删除型号参数关系
     * 
     * @param paramCode 参数编码
     */
    public void deleteParamModelRelByParamCode(String paramCode);
}
