package cn.eyecool.device.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.device.domain.DeviceInfo;

/**
 * 设备信息Service接口
 * 
 * @author admin
 * @date 2021-04-01
 */
public interface IDeviceInfoService {
    /**
     * 查询设备信息
     * 
     * @param id 设备信息ID
     * @return 设备信息
     */
    public DeviceInfo selectDeviceInfoById(String id);

    /**
     * 查询设备信息列表
     * 
     * @param deviceInfo 设备信息
     * @return 设备信息集合
     */
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo);

    /**
     * 新增设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    public int insertDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 修改设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    public int updateDeviceInfo(DeviceInfo deviceInfo);

    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的设备信息ID
     * @return 结果
     */
    public int deleteDeviceInfoByIds(String[] ids);

    /**
     * 删除设备信息信息
     * 
     * @param id 设备信息ID
     * @return 结果
     */
    public int deleteDeviceInfoById(String id);

    /**
     * 校验设备编号是否唯一
     *
     * @param deviceInfo 设备信息
     * @return
     */
    public boolean checkDeviceNoUnique(DeviceInfo deviceInfo);

    /**
     * 设置设备的Mqtt连接认证信息
     * 
     * @param deviceInfo
     * @return
     */
    public DeviceInfo initMqttAuthInfo(DeviceInfo deviceInfo);

    /**
     * 更新设备mqtt认证信息
     * 
     * @param info
     * @return
     */
    public int updateMqttAuthInfo(DeviceInfo info);

    /**
     * 批量导入设备
     * 
     * @param excelFile 文件
     * @param deviceType 设备类型
     * @param importBatchNum 导入批次
     * @param updateSupport 是否支持覆盖更新
     * @param clazz 设备子类
     * @param batchDesc 导入批次说明
     * @return
     * @throws Exception
     */
    public String saveImportData(MultipartFile excelFile, String deviceType, String importBatchNum,
        Boolean updateSupport, Class<? extends DeviceInfo> clazz, String batchDesc) throws Exception;

    /**
     * 批量导入设备
     * 
     * @param username 当前操作用户
     * @param inputStream excel文件流
     * @param deviceType 设备类型
     * @param importBatchNum 导入批次
     * @param updateSupport 是否支持覆盖更新
     * @param clazz 设备子类
     * @param batchDesc 导入批次说明
     * @return
     * @throws Exception
     */
    public String saveImportData(String username, InputStream inputStream, String deviceType, String importBatchNum,
        Boolean updateSupport, Class<? extends DeviceInfo> clazz, String batchDesc) throws Exception;

    /**
     * 选择待加入升级任务的设备列表
     * 
     * @param versionId
     * @param deviceInfo
     * @return
     */
    public List<DeviceInfo> listUpgradeDevice(String versionId, DeviceInfo deviceInfo);

    /**
     * 设备注册
     * 
     * @param deviceInfo 设备信息
     */
    public void registerDevice(DeviceInfo deviceInfo);

    /**
     * 根据设备编码取消设备全量拉取人员标志
     * 
     * @param deviceNo
     */
    public void cancelPullAllDataFlagByDeviceNo(String deviceNo);

}
