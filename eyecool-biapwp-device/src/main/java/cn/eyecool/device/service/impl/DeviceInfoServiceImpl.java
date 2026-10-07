package cn.eyecool.device.service.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceActionLog;
import cn.eyecool.device.domain.DeviceBatchLog;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.domain.DeviceModel;
import cn.eyecool.device.mapper.DeviceActionLogMapper;
import cn.eyecool.device.mapper.DeviceBatchLogMapper;
import cn.eyecool.device.mapper.DeviceInfoMapper;
import cn.eyecool.device.mapper.DeviceModelMapper;
import cn.eyecool.device.mapper.DeviceUpgradeLogMapper;
import cn.eyecool.device.mapper.DeviceUpgradeTaskMapper;
import cn.eyecool.device.service.IDeviceInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-01
 */
@Service
@Slf4j
public class DeviceInfoServiceImpl implements IDeviceInfoService {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    private DeviceModelMapper deviceModelMapper;
    @Autowired
    private DeviceBatchLogMapper deviceBatchLogMapper;
    @Autowired
    private DeviceUpgradeTaskMapper deviceUpgradeTaskMapper;
    @Autowired
    private DeviceUpgradeLogMapper deviceUpgradeLogMapper;
    @Autowired
    private MqttAuthAesKeyComponent mqttAuthAesKeyComponent;
    @Autowired
    private DeviceActionLogMapper deviceActionLogMapper;
    @Autowired
    private RedisCache redisCache;
    // 导入设备信息加锁
    public static final Lock importLock = new ReentrantLock();

    /**
     * 查询设备信息
     * 
     * @param id 设备信息ID
     * @return 设备信息
     */
    @Override
    public DeviceInfo selectDeviceInfoById(String id) {
        return deviceInfoMapper.selectDeviceInfoById(id);
    }

    /**
     * 查询设备信息列表
     * 
     * @param deviceInfo 设备信息
     * @return 设备信息
     */
    @Override
    public List<DeviceInfo> selectDeviceInfoList(DeviceInfo deviceInfo) {
        return deviceInfoMapper.selectDeviceInfoList(deviceInfo);
    }

    /**
     * 新增设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceInfo(DeviceInfo deviceInfo) {
        if (!checkDeviceNoUnique(deviceInfo)) {
            throw new CustomException(MessageUtils.message("device.info.service.deviceno.exists"));
        }
        // 如果没有选择主库，默认第一个绑定子场景作为主库
        if (StringUtils.isBlank(deviceInfo.getPrimarySubCode())
            && StringUtils.isNotBlank(deviceInfo.getSubtreasuryCode())) {
            deviceInfo.setPrimarySubCode(Convert.toStrArray(deviceInfo.getSubtreasuryCode())[0]);
        }
        try {
            deviceInfo.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        deviceInfo.setId(IdWorker.getNextStringId());
        deviceInfo.setCreateTime(DateUtils.getNowDate());
        // 设备Mqtt连接认证信息设置
        initMqttAuthInfo(deviceInfo);
        redisCache.setCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceInfo.getDeviceNo(), deviceInfo);
        return deviceInfoMapper.insertDeviceInfo(deviceInfo);
    }

    /**
     * 校验设备编号是否唯一
     *
     * @param deviceInfo 设备信息
     * @return
     */
    @Override
    public boolean checkDeviceNoUnique(DeviceInfo deviceInfo) {
        // 多租户支持后，跨租户设备编码也不允许重复
        DeviceInfo cacheDevice =
            redisCache.getCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceInfo.getDeviceNo());
        if (StringUtils.isNull(cacheDevice)) {
            return true;
        }
        DeviceInfo info = deviceInfoMapper.checkDeviceNoUnique(deviceInfo.getDeviceNo());
        if (StringUtils.isNull(info)) {
            return true;
        }
        return info.getId().equals(deviceInfo.getId());
    }

    /**
     * 设置设备的Mqtt连接认证信息
     * 
     * @param deviceInfo
     * @return
     */
    @Override
    public DeviceInfo initMqttAuthInfo(DeviceInfo deviceInfo) {
        String deviceNo = deviceInfo.getDeviceNo();
        try {
            String aesEncodeStr =
                AESUtils.encrypt(Md5Utils.hash(deviceNo), mqttAuthAesKeyComponent.getDeviceMqttAuthSkey());
            String pwd = aesEncodeStr.substring(0, 10);
            pwd = pwd.substring(4) + pwd.substring(0, 4);
            // 拼接一个"1"，增加复杂度
            String salt = "1" + aesEncodeStr.substring(10, 16);
            String encryptedPwd = Md5Utils.hash(deviceNo + pwd + salt);
            deviceInfo.setMqttPwd(encryptedPwd);
            deviceInfo.setMqttSalt(salt);
            return deviceInfo;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("device.info.service.device.mqtt.authenticate",  e.getMessage()));
        }

    }

    /**
     * 更新设备mqtt认证信息
     * 
     * @param info
     * @return
     */
    @Override
    public int updateMqttAuthInfo(DeviceInfo info) {
        return deviceInfoMapper.updateMqttAuthInfo(info);
    }

    /**
     * 修改设备信息
     * 
     * @param deviceInfo 设备信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDeviceInfo(DeviceInfo deviceInfo) {
        DeviceInfo oldInfo = deviceInfoMapper.selectDeviceInfoById(deviceInfo.getId());
        String channelCode = deviceInfo.getChannelCode();
        String subtreasuryCode = deviceInfo.getSubtreasuryCode();
        String primarySubCode = deviceInfo.getPrimarySubCode();
        // 场景是否置空
        boolean channelEmpty = StringUtils.EMPTY.equals(channelCode);
        // 子场景是否置空
        boolean subEmpty = StringUtils.EMPTY.equals(subtreasuryCode);
        // 主子场景是否置空
        boolean primarySubEmpty = StringUtils.EMPTY.equals(primarySubCode);
        // 场景是否变化
        boolean isChannelChanged =
            channelEmpty || (null != channelCode && !channelCode.equals(oldInfo.getChannelCode()));
        // 绑定子场景是否变化
        boolean isSubChanged =
            subEmpty || (null != subtreasuryCode && !subtreasuryCode.equals(oldInfo.getSubtreasuryCode()));
        // 设备绑定子场景的主库是否变化
        boolean isPrimarySubChanged =
            primarySubEmpty || (null != primarySubCode && !primarySubCode.equals(oldInfo.getPrimarySubCode()));
        if (isChannelChanged || isSubChanged || isPrimarySubChanged) {
            // 设置设备重新拉取全部数据标志
            deviceInfo.setPullAllFlag(Constants.STATUS_ONE);
        }
        deviceInfo.setUpdateTime(DateUtils.getNowDate());
        try {
            deviceInfo.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        redisCache.setCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + deviceInfo.getDeviceNo(), deviceInfo);
        Long expire = redisCache.getExpire(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + deviceInfo.getDeviceNo());
        if (null != expire && expire > 0) {
            redisCache.setCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + deviceInfo.getDeviceNo(), deviceInfo,
                expire, TimeUnit.SECONDS);
        }
        return deviceInfoMapper.updateDeviceInfo(deviceInfo);
    }

    @Override
    @Transactional
    public int updateDeviceData(DeviceInfo deviceInfo) {
        return deviceInfoMapper.updateDeviceInfo(deviceInfo);
    }
    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的设备信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceInfoByIds(String[] ids) {
        int count = 0;
        for (String id : ids) {
            count += deleteDeviceInfoById(id);
        }
        return count;
    }

    /**
     * 删除设备信息信息
     * 
     * @param id 设备信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeviceInfoById(String id) {
        DeviceInfo device = deviceInfoMapper.selectDeviceInfoById(id);
        // 该设备的所有待执行升级任务自动跳过
        deviceUpgradeTaskMapper.skipTaskByDeviceId(id, DateUtils.getNowDate());
        // 该设备的所有待执行和待下载任务日志自动跳过
        deviceUpgradeLogMapper.skipUpgradeLogByDeviceNo(device.getDeviceNo(), DateUtils.getNowDate());
        redisCache.deleteObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + device.getDeviceNo());
        redisCache.deleteObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + device.getDeviceNo());
        return deviceInfoMapper.deleteDeviceInfoById(id);
    }

    /**
     * 批量导入设备
     * 
     * @param excelFile
     * @param deviceType
     * @param importBatchNum
     * @param updateSupport
     * @param clazz
     * @param batchDesc
     * @return
     * @throws Exception
     */
    @Override
    public String saveImportData(MultipartFile excelFile, String deviceType, String importBatchNum,
        Boolean updateSupport, Class<? extends DeviceInfo> clazz, String batchDesc) throws Exception {
        return saveImportData(null, excelFile.getInputStream(), deviceType, importBatchNum, updateSupport, clazz,
            batchDesc);
    }

    /**
     * 批量导入设备
     * 
     * @param username
     * @param inputStream
     * @param deviceType
     * @param importBatchNum
     * @param updateSupport
     * @param clazz
     * @param batchDesc
     * @return
     * @throws Exception
     */
    @Override
    public String saveImportData(String username, InputStream inputStream, String deviceType, String importBatchNum,
        Boolean updateSupport, Class<? extends DeviceInfo> clazz, String batchDesc) throws Exception {
        boolean tryLock = importLock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("device.info.service.system.busying"));
        }
        Integer importFailNum = 0;
        Integer importSuccNum = 0;
        try {
            StringBuilder successMsg = new StringBuilder();
            StringBuilder failureMsg = new StringBuilder();
            ExcelUtil<? extends DeviceInfo> util = new ExcelUtil<>(clazz);
            List<? extends DeviceInfo> infoList = util.importExcel(inputStream);
            // 执行导入数据
            Map<String, Object> importResMap =
                importDeviceInfoData(infoList, deviceType, importBatchNum, updateSupport);
            importSuccNum = (Integer)importResMap.get("insertSuccNum");
            importFailNum = (Integer)importResMap.get("failureNum");
            if (null != importFailNum && importFailNum > 0) {
                failureMsg.append(importResMap.get("failureMsg"));
                throw new CustomException(failureMsg.toString());
            }

            String importSuccMsg = (String)importResMap.get("successMsg");
            if (StringUtils.isNotBlank(importSuccMsg)) {
                successMsg = successMsg.append(StringUtils.nvl(importSuccMsg, ""));
            }
            return successMsg.toString();
        } finally {
            importLock.unlock();
            if (importSuccNum > 0) {
                insertBatchLog(importBatchNum, batchDesc, username);
            }
        }
    }

    /**
     * 执行导入
     *
     * @param importList
     * @param deviceType
     * @param importBatchNum
     * @param updateSupport
     * @return
     */
    private Map<String, Object> importDeviceInfoData(List<? extends DeviceInfo> importList, String deviceType,
        String importBatchNum, Boolean updateSupport) {
        if (CollectionUtils.isEmpty(importList)) {
            log.error("Import device information data is empty!");
            throw new CustomException(MessageUtils.message("device.info.service.import.data.empty"));
        }
        int successNum = 0;
        int insertSuccNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        for (DeviceInfo device : importList) {
            try {
                // 进行字段合法性校验
                validateImportField(device);
                device.setDeviceType(deviceType);
                // 判断设备编号是否存在
                DeviceInfo info = deviceInfoMapper.checkDeviceNoUnique(device.getDeviceNo());
                boolean exists = StringUtils.isNotNull(info);
                if (exists) {
                    if (!Boolean.TRUE.equals(updateSupport)) {
                        failureNum++;
                        String msg = "<br/>" + failureNum +MessageUtils.message("device.info.service.import.device.exists", device.getDeviceNo());
                        failureMsg.append(msg);
                        log.error(msg);
                        continue;
                    }
                    device.setId(info.getId());
                    device.setUpdateBy(loginName);
                    device.setUpdateTime(DateUtils.getNowDate());
                    device.setCreateMethod(DictConstants.DeviceCreateMethod.BG_IMPORT);
                    deviceInfoMapper.updateDeviceInfo(device);
                    successNum++;
                    continue;
                }
                device.setCreateBy(loginName);
                device.setCreateTime(DateUtils.getNowDate());
                device.setCreateMethod(DictConstants.DeviceCreateMethod.BG_IMPORT);
                device.setImportBatchNum(importBatchNum);
                device.setId(IdWorker.getNextStringId());
                Method afterDataSet = BeanUtils.findMethod(device.getClass(), "afterDataSet");
                Objects.requireNonNull(afterDataSet).invoke(device);
                // 初始化设置设备mqtt连接鉴权信息
                initMqttAuthInfo(device);
                deviceInfoMapper.insertDeviceInfo(device);
                redisCache.setCacheObject(RedisKeyConstants.DEVICE_CACHE_PREFIX + device.getDeviceNo(), device);
                successNum++;
                insertSuccNum++;
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + MessageUtils.message("device.info.service.import.failed", device.getDeviceNo()) + e.getMessage();
                failureMsg.append(msg);
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("device.info.service.import.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("device.info.service.import.success.sumary", successNum);
            successMsg.insert(0, msg);
            log.info(successMsg.toString());
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("failureNum", failureNum);
        resMap.put("failureMsg", failureMsg.toString());
        resMap.put("successNum", successNum);
        resMap.put("insertSuccNum", insertSuccNum);
        resMap.put("successMsg", successMsg.toString());
        return resMap;
    }

    /**
     * 校验和设置导入的设备信息字段
     * 
     * @param device
     */
    private void validateImportField(DeviceInfo device) {
        String deviceNo = device.getDeviceNo();
        if (StringUtils.isBlank(deviceNo)) {
            throw new CustomException(MessageUtils.message("device.info.service.devicesn.empty"));
        }
        String deviceModelCode = device.getDeviceModelCode();
        if (StringUtils.isBlank(deviceModelCode)) {
            throw new CustomException(MessageUtils.message("device.info.service.device.model.code.empty"));
        }
        DeviceModel model = new DeviceModel();
        model.setModelCode(deviceModelCode);
        List<DeviceModel> modelList = deviceModelMapper.selectDeviceModelList(model);
        if (CollectionUtils.isEmpty(modelList)) {
            throw new CustomException(MessageUtils.message("device.info.service.device.model.not.exists"));
        }
    }

    /**
     * 新增设备导入批次日志
     * 
     * @param batchNum
     * @param batchDesc
     * @param username
     */
    private void insertBatchLog(String batchNum, String batchDesc, String username) {
        DeviceBatchLog batchLog = new DeviceBatchLog();
        batchLog.setId(IdWorker.getNextStringId());
        batchLog.setBatchNum(batchNum);
        batchLog.setBatchDesc(batchDesc);
        batchLog.setCreateTime(DateUtils.getNowDate());
        batchLog.setCreateBy(username);
        batchLog.setRollbacked(false);
        batchLog.setTenantId(TenantContextHolder.getTenantId());
        deviceBatchLogMapper.insertDeviceBatchLog(batchLog);
    }

    /**
     * 选择待加入升级任务的设备列表
     * 
     * @param versionId
     * @param deviceInfo
     * @return
     */
    @Override
    public List<DeviceInfo> listUpgradeDevice(String versionId, DeviceInfo deviceInfo) {
        return deviceInfoMapper.listUpgradeDevice(versionId, deviceInfo);
    }

    /**
     * 设备注册（修改设备在线状态和部分属性）
     * 
     * @param deviceInfo
     */
    @Override
    @Transactional
    public void registerDevice(DeviceInfo deviceInfo) {
        String deviceNo = deviceInfo.getDeviceNo();
        if (StringUtils.isBlank(deviceNo)) {
            throw new CustomException(MessageUtils.message("device.info.service.device.code.empty"));
        }
        // 查询设备编码是否存在
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = deviceInfoMapper.selectDeviceInfoList(condition);
        if (CollectionUtils.isEmpty(list)) {
            log.debug("The device corresponding to the registered device code does not exist, deviceNo:[{}]", deviceNo);
            return;
        }
        // 修改设备在线状态信息
        DeviceInfo existInfo = list.get(0);
        String newState = deviceInfo.getDeviceState();
        String oldIp = existInfo.getDeviceIp();
        String newIp = deviceInfo.getDeviceIp();
        boolean exeUpdate = StringUtils.isNotBlank(newIp) && !newIp.equals(oldIp);
        if (exeUpdate) {
            existInfo.setDeviceIp(newIp);
            existInfo.setDeviceState(deviceInfo.getDeviceState());
            existInfo.setUpdateTime(DateUtils.getNowDate());
            deviceInfoMapper.updateDeviceInfo(existInfo);
        }
        boolean isOnline = DictConstants.DeviceOnlineState.ONLINE.equals(newState);
        // 修改设备在线状态（redis）
        if (isOnline) {
            redisCache.setCacheObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + deviceNo, existInfo);
        } else {
            // 从在线设备中移除
            redisCache.deleteObject(RedisKeyConstants.ONLINE_DEVICE_KEY_PREFIX + deviceNo);
        }
        // 记录设备上下线日志
        String tenantId = TenantContextHolder.getTenantId();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            String actionType =
                isOnline ? DictConstants.DeviceActionLogType.ONLINE : DictConstants.DeviceActionLogType.OFFLINE;
            log.info("record equipment action logs, deviceNo:[{}],onlineState:[{}], actionType：[{}]", deviceNo, newState, actionType);
            saveDeviceActionLog(deviceNo, existInfo.getDeviceName(), existInfo.getDeviceModelCode(),
                existInfo.getChannelCode(), actionType);
        });
    }

    /**
     * 保存设备动作日志
     * 
     * @param deviceNo
     * @param deviceName
     * @param modelCode
     * @param channelCode
     * @param actionType
     */
    private void saveDeviceActionLog(String deviceNo, String deviceName, String modelCode, String channelCode,
        String actionType) {
        // 保存设备动作日志
        DeviceActionLog log = new DeviceActionLog();
        log.setDeviceNo(deviceNo);
        log.setDeviceName(deviceName);
        log.setActionType(actionType);
        log.setCreateTime(DateUtils.getNowDate());
        log.setModelCode(modelCode);
        log.setChannelCode(channelCode);
        log.setId(IdWorker.getNextStringId());
        deviceActionLogMapper.insertDeviceActionLog(log);
    }

    /**
     * 根据设备编码取消设备全量拉取人员标志
     * 
     * @param deviceNo
     */
    @Override
    public void cancelPullAllDataFlagByDeviceNo(String deviceNo) {
        deviceInfoMapper.cancelPullAllDataFlagByDeviceNo(deviceNo);
    }
}
