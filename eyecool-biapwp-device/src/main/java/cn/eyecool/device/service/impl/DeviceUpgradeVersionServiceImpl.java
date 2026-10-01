package cn.eyecool.device.service.impl;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.device.domain.DeviceUpgradeVersion;
import cn.eyecool.device.mapper.DeviceUpgradeLogMapper;
import cn.eyecool.device.mapper.DeviceUpgradeTaskMapper;
import cn.eyecool.device.mapper.DeviceUpgradeVersionMapper;
import cn.eyecool.device.service.IDeviceUpgradeVersionService;
import cn.eyecool.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 版本信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-07
 */
@Service
@Slf4j
public class DeviceUpgradeVersionServiceImpl implements IDeviceUpgradeVersionService {

    @Autowired
    private DeviceUpgradeVersionMapper deviceUpgradeVersionMapper;
    @Autowired
    private DeviceUpgradeTaskMapper deviceUpgradeTaskMapper;
    @Autowired
    private DeviceUpgradeLogMapper deviceUpgradeLogMapper;
    @Autowired
    private ISysConfigService configService;

    /**
     * 查询版本信息
     * 
     * @param id 版本信息ID
     * @return 版本信息
     */
    @Override
    public DeviceUpgradeVersion selectDeviceUpgradeVersionById(String id) {
        return deviceUpgradeVersionMapper.selectDeviceUpgradeVersionById(id);
    }

    /**
     * 查询版本信息列表
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 版本信息
     */
    @Override
    public List<DeviceUpgradeVersion> selectDeviceUpgradeVersionList(DeviceUpgradeVersion deviceUpgradeVersion) {
        return deviceUpgradeVersionMapper.selectDeviceUpgradeVersionList(deviceUpgradeVersion);
    }

    /**
     * 新增版本信息
     * 
     * @param file 版本文件
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceUpgradeVersion(MultipartFile file, DeviceUpgradeVersion deviceUpgradeVersion) {
        String appName = deviceUpgradeVersion.getAppName();
        String version = deviceUpgradeVersion.getVersion();
        DeviceUpgradeVersion condition = new DeviceUpgradeVersion();
        condition.setAppName(appName);
        List<DeviceUpgradeVersion> versionList = deviceUpgradeVersionMapper.selectDeviceUpgradeVersionList(condition);
        if (CollectionUtils.isNotEmpty(versionList)) {
            boolean anyMatch = versionList.stream().anyMatch(it -> version.equals(it.getVersion()));
            if (anyMatch) {
                throw new CustomException(MessageUtils.message("device.version.info.exists"));
            }
        }
        // 查询版本文件配置文件夹
        String baseDir = getBaseDir();
        try {
            // 上传版本文件
            String filePath = PlatformFileUploadUtils.upload(baseDir, file, null);
            deviceUpgradeVersion.setPath(filePath);
            deviceUpgradeVersion.setFileSize(file.getSize());
            deviceUpgradeVersion.setFilename(file.getOriginalFilename());
            String md5 = Md5Utils.hash(new File(filePath));
            deviceUpgradeVersion.setMd5(md5);
        } catch (Exception e) {
            log.error("APP version file upload error!", e);
            throw new CustomException(e.getMessage());
        }
        deviceUpgradeVersion.setId(IdWorker.getNextStringId());
        try {
            deviceUpgradeVersion.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceUpgradeVersion.setCreateTime(DateUtils.getNowDate());
        return deviceUpgradeVersionMapper.insertDeviceUpgradeVersion(deviceUpgradeVersion);
    }

    /**
     * 查询APP文件存储文件夹
     * 
     * @return
     */
    private String getBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.DEVICE_VERSION_FILE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("device.version.app.folder.need", SysConfigConstants.DEVICE_VERSION_FILE_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }

    /**
     * 修改版本信息
     * 
     * @param deviceUpgradeVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceUpgradeVersion(DeviceUpgradeVersion deviceUpgradeVersion) {
        try {
            deviceUpgradeVersion.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceUpgradeVersion.setUpdateTime(DateUtils.getNowDate());
        return deviceUpgradeVersionMapper.updateDeviceUpgradeVersion(deviceUpgradeVersion);
    }

    /**
     * 批量删除版本信息
     * 
     * @param ids 需要删除的版本信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceUpgradeVersionByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteDeviceUpgradeVersionById(id);
        }
        return result;
    }

    /**
     * 删除版本信息信息
     * 
     * @param id 版本信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceUpgradeVersionById(String id) {
        DeviceUpgradeVersion upgradeVersion = deviceUpgradeVersionMapper.selectDeviceUpgradeVersionById(id);
        if (Boolean.TRUE.equals(upgradeVersion.getEnabled())) {
            throw new CustomException(MessageUtils.message("device.version.before.delete.first.disabled"));
        }
        // 该版本的所有待执行升级任务自动跳过
        deviceUpgradeTaskMapper.skipTaskByVersionId(id, DateUtils.getNowDate());
        // 该版本的所有待执行和待下载任务日志自动跳过
        deviceUpgradeLogMapper.skipUpgradeLogByAppInfo(upgradeVersion.getAppName(), upgradeVersion.getVersion(),
            DateUtils.getNowDate());
        return deviceUpgradeVersionMapper.deleteDeviceUpgradeVersionById(id);
    }

    /**
     * 版本文件下载
     * 
     * @param id
     * @param request
     * @param response
     */
    @Override
    public void downloadVersionFile(String id, HttpServletRequest request, HttpServletResponse response) {
        DeviceUpgradeVersion upgradeVersion = deviceUpgradeVersionMapper.selectDeviceUpgradeVersionById(id);
        if (null == upgradeVersion) {
            throw new CustomException(MessageUtils.message("device.upgrade.version.not.exists"));
        }
        String path = upgradeVersion.getPath();
        String filename = upgradeVersion.getFilename();
        response.setContentType("multipart/form-data");
        try {
            response.setHeader("Content-Disposition",
                "attachment;filename=" + FileUtils.setFileDownloadHeader(request, filename));
            FileUtils.writeBytes(path, response.getOutputStream());
        } catch (Exception e) {
            log.error("APP version download error!", e);
            throw new CustomException(e.getMessage());
        }
    }

}
