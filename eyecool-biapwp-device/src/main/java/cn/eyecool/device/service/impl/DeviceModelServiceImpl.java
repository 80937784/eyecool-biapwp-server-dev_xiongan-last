package cn.eyecool.device.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.uuid.UUID;
import cn.eyecool.device.domain.DeviceModel;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.mapper.DeviceModelMapper;
import cn.eyecool.device.mapper.DeviceParamModelRelMapper;
import cn.eyecool.device.service.IDeviceModelService;
import cn.eyecool.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备型号信息Service业务层处理
 * 
 * @author admin
 * @date 2021-03-29
 */
@Service
@Slf4j
public class DeviceModelServiceImpl implements IDeviceModelService {

    @Autowired
    private DeviceModelMapper deviceModelMapper;
    @Autowired
    private DeviceParamModelRelMapper deviceParamModelRelMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 查询设备型号信息
     * 
     * @param id 设备型号信息ID
     * @return 设备型号信息
     */
    @Override
    public DeviceModel selectDeviceModelById(String id) {
        return deviceModelMapper.selectDeviceModelById(id);
    }

    /**
     * 查询设备型号信息列表
     * 
     * @param deviceModel 设备型号信息
     * @return 设备型号信息
     */
    @Override
    public List<DeviceModel> selectDeviceModelList(DeviceModel deviceModel) {
        return deviceModelMapper.selectDeviceModelList(deviceModel);
    }

    /**
     * 新增设备型号信息
     * 
     * @param deviceModel 设备型号信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceModel(DeviceModel deviceModel) {
        // 校验设备类型是否已经存在
        DeviceModel condition = new DeviceModel();
        condition.setModelCode(deviceModel.getModelCode());
        List<DeviceModel> list = deviceModelMapper.selectDeviceModelList(condition);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new CustomException(MessageUtils.message("device.model.service.model.code.exists",  deviceModel.getModelCode()));
        }
        // 上传设备外观图片
        String imageBase64 = deviceModel.getImageBase64();
        if (StringUtils.isNotBlank(imageBase64)) {
            try {
                String uploadPath =
                    PlatformFileUploadUtils.upload(getBaseDir(), UUID.randomUUID() + ".jpg", imageBase64);
                deviceModel.setExteriorImage(uploadPath);
            } catch (IOException e) {
                log.error("Device appearance picture upload error!", e);
                throw new CustomException(e.getMessage());
            }
        }
        try {
            deviceModel.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceModel.setId(IdWorker.getNextStringId());
        deviceModel.setCreateTime(DateUtils.getNowDate());
        return deviceModelMapper.insertDeviceModel(deviceModel);
    }

    /**
     * 修改设备型号信息
     * 
     * @param deviceModel 设备型号信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceModel(DeviceModel deviceModel) {
        DeviceModel model = deviceModelMapper.selectDeviceModelById(deviceModel.getId());
        String oldImgUrl = model.getExteriorImage();
        boolean isDelOldImg = false;
        // 上传设备外观图片
        String imageBase64 = deviceModel.getImageBase64();
        if (StringUtils.isNotBlank(imageBase64)) {
            try {
                String uploadPath =
                    PlatformFileUploadUtils.upload(getBaseDir(), UUID.randomUUID() + ".jpg", imageBase64);
                deviceModel.setExteriorImage(uploadPath);
                isDelOldImg = StringUtils.isNotBlank(oldImgUrl);
            } catch (IOException e) {
                log.error("Device appearance picture upload error!", e);
                throw new CustomException(e.getMessage());
            }
        }
        try {
            deviceModel.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceModel.setUpdateTime(DateUtils.getNowDate());
        int result = deviceModelMapper.updateDeviceModel(deviceModel);
        if (isDelOldImg) {
            CompletableFuture.runAsync(() -> {
                FileUtils.deleteFile(oldImgUrl);
            });
        }
        return result;
    }

    /**
     * 批量删除设备型号信息
     * 
     * @param ids 需要删除的设备型号信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceModelByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteDeviceModelById(id);
        }
        return result;
    }

    /**
     * 删除设备型号信息信息
     * 
     * @param id 设备型号信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceModelById(String id) {
        // 查询设备型号下是否有设备信息
        DeviceModel model = deviceModelMapper.selectDeviceModelById(id);
        int count = deviceInfoMapper.selectDeviceCountByModelCode(model.getModelCode());
        if (count > 0) {
            throw new CustomException(MessageUtils.message("device.model.service.not.allow.deleted", model.getModelCode()));
        }
        // 删除设备型号和参数绑定关系
        deviceParamModelRelMapper.deleteParamModelRelByModelCode(model.getModelCode());
        return deviceModelMapper.deleteDeviceModelById(id);
    }

    /**
     * 查询外观图片文件存储文件夹
     * 
     * @return
     */
    private String getBaseDir() {
        String baseDir = configService.selectConfigByKey(SysConfigConstants.DEVICE_MODEL_IMAGE_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(MessageUtils.message("device.model.service.image.folder.need", SysConfigConstants.DEVICE_MODEL_IMAGE_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        return baseDir;
    }
}
