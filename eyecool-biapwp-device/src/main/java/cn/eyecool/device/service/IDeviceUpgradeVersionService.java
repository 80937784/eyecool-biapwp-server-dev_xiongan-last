package cn.eyecool.device.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.device.domain.DeviceUpgradeVersion;

/**
 * 版本信息Service接口
 * 
 * @author admin
 * @date 2021-04-07
 */
public interface IDeviceUpgradeVersionService {
    /**
     * 查询版本信息
     * 
     * @param id 版本信息ID
     * @return 版本信息
     */
    public DeviceUpgradeVersion selectDeviceUpgradeVersionById(String id);

    /**
     * 查询版本信息列表
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 版本信息集合
     */
    public List<DeviceUpgradeVersion> selectDeviceUpgradeVersionList(DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 新增版本信息
     * 
     * @param file 版本文件
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    public int insertDeviceUpgradeVersion(MultipartFile file, DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 修改版本信息
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    public int updateDeviceUpgradeVersion(DeviceUpgradeVersion deviceUpgradeVersion);

    /**
     * 批量删除版本信息
     * 
     * @param ids 需要删除的版本信息ID
     * @return 结果
     */
    public int deleteDeviceUpgradeVersionByIds(String[] ids);

    /**
     * 删除版本信息信息
     * 
     * @param id 版本信息ID
     * @return 结果
     */
    public int deleteDeviceUpgradeVersionById(String id);

    /**
     * 版本文件下载
     * 
     * @param id
     * @param request
     * @param response
     */
    public void downloadVersionFile(String id, HttpServletRequest request, HttpServletResponse response);

}
