package cn.eyecool.server.mqtt;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.MqttTopicConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.proto.EdgeProto.ConfigResultMessage;
import cn.eyecool.device.proto.EdgeProto.ConfigResultMessage.ConfigState;
import cn.eyecool.device.proto.EdgeProto.RegistMessage;
import cn.eyecool.device.proto.EdgeProto.RegistMessage.DeviceState;
import cn.eyecool.device.proto.EdgeProto.RegistResultsMessage;
import cn.eyecool.device.proto.EdgeProto.RegistResultsMessage.RegistState;
import cn.eyecool.device.proto.EdgeProto.UpdateResultMessage;
import cn.eyecool.device.proto.EdgeProto.UpdateResultMessage.UpdateState;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.device.service.IDeviceParamDistributeLogService;
import cn.eyecool.device.service.IDeviceUpgradeTaskService;

/**
 * 设备Mqtt通讯数据处理和业务调用
 * 
 * @author admin
 * @date 2020年7月21日
 */
@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class DeviceMqttBusiHandler {

    private static final transient Logger LOG = LoggerFactory.getLogger(DeviceMqttBusiHandler.class);

    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IDeviceUpgradeTaskService deviceUpgradeTaskService;
    @Autowired
    private PlatformMqttClientUtil clientUtil;
    @Autowired
    private IDeviceParamDistributeLogService deviceParamDistributeLogService;
    @Autowired
    private TenantProperties tenantProperties;

    @Value("${mqtt.proto-version}")
    private String serverProtoVersion;

    /**
     * 设备信息注册
     * 
     * @param deviceNo
     * @param message
     */
    public void registerDevice(String deviceNo, MqttMessage message) {
        try {
            if (!setTenantContextHolderByDeviceNo(deviceNo)) {
                LOG.error("Device registration in multi-tenant mode fails to maintain thread tenant ID, deviceNo:[{}]", deviceNo);
                publishRegistResult(deviceNo, RegistState.BUSINESS_ERROR);
                return;
            }
            doRegisterDevice(deviceNo, message);
        } finally {
            TenantContextHolder.clear();
        }
    }

    /**
     * 设备信息注册
     * 
     * @param deviceNo
     * @param message
     */
    private void doRegisterDevice(String deviceNo, MqttMessage message) {
        LOG.debug("device information registration,deviceNo:[{}]", deviceNo);
        if (deviceNo.length() > 48) {
            LOG.error("The device code length is not allowed to be greater than 48 bits, deviceNo:[{}]", deviceNo);
            publishRegistResult(deviceNo, RegistState.BUSINESS_ERROR);
            return;
        }
        byte[] payload = message.getPayload();
        if (null == payload) {
            LOG.error("The device registration information is empty,deviceNo:{},messageId:{}，qos:{}", deviceNo, message.getId(), message.getQos());
            publishRegistResult(deviceNo, RegistState.BUSINESS_ERROR);
            return;
        }
        RegistMessage registMessage = null;
        try {
            registMessage = RegistMessage.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            LOG.error("The device registration information is illegal,deviceNo:{},error:{}", deviceNo, e.getMessage());
            publishRegistResult(deviceNo, RegistState.BUSINESS_ERROR);
            return;
        }
        // 进行协议校验
        String protoVersion = registMessage.getProtocolVersion();
        if (StringUtils.isNotBlank(protoVersion) && !serverProtoVersion.equals(protoVersion)) {
            LOG.error("Device registration protocol mismatch,edgeProtoVersion:{},serverProtoVersion:{}", protoVersion, serverProtoVersion);
            publishRegistResult(deviceNo, RegistState.PROTOCOL_ERROR);
            return;
        }

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceNo(deviceNo);
        DeviceState deviceState = registMessage.getDeviceState();
        if (DeviceState.ONLINE_VALUE == deviceState.getNumber()) {// 设备上线
            LOG.info("Device [{}] is online", deviceNo);
            deviceInfo.setDeviceState(DictConstants.DeviceOnlineState.ONLINE);
        } else {// 设备下线
            LOG.info("Device [{}] goes offline", deviceNo);
            deviceInfo.setDeviceState(DictConstants.DeviceOnlineState.OFFLINE);
        }
        if (StringUtils.isNotBlank(registMessage.getChannelCode())) {
            deviceInfo.setChannelCode(registMessage.getChannelCode());
        }
        if (StringUtils.isNotBlank(registMessage.getDeviceIp())) {
            deviceInfo.setDeviceIp(registMessage.getDeviceIp());
        }
        if (StringUtils.isNotBlank(registMessage.getDeviceMac())) {
            deviceInfo.setDeviceMac(registMessage.getDeviceMac());
        }
        if (StringUtils.isNotBlank(registMessage.getDeviceModelCode())) {
            deviceInfo.setDeviceModelCode(registMessage.getDeviceModelCode());
        }
        if (StringUtils.isNotBlank(registMessage.getDeviceName())) {
            deviceInfo.setDeviceName(registMessage.getDeviceName());
        }
        if (registMessage.getLatitude() != 0D) {
            deviceInfo.setLatitude(registMessage.getLatitude());
        }
        if (registMessage.getLongitude() != 0D) {
            deviceInfo.setLongitude(registMessage.getLongitude());
        }
        try {
            deviceInfoService.registerDevice(deviceInfo);
        } catch (CustomException e) {
            LOG.error("deviceNo:{},error:{}", deviceNo, e.getMessage());
            publishRegistResult(deviceNo, RegistState.BUSINESS_ERROR);
            return;
        } catch (Exception e) {
            LOG.error("deviceNo:{},error:{}", deviceNo, e.getMessage());
            publishRegistResult(deviceNo, RegistState.UNKNOWN);
            return;
        }
        // 发布注册结果
        publishRegistResult(deviceNo, RegistState.SUCCESS);

    }

    /**
     * 设备注册结果发布
     * 
     * @param deviceNo
     * @param state
     */
    private void publishRegistResult(String deviceNo, RegistState state) {
        RegistResultsMessage registResultsMessage = RegistResultsMessage.newBuilder().setRegistState(state).build();
        byte[] resultContent = registResultsMessage.toByteArray();
        boolean result = false;
        for (int i = 0; i < 3; i++) {
            result = clientUtil.publishMessage(MqttTopicConstants.PUB_DEVICE_REGIST_RESULT_TOPIC_PREFIX + deviceNo,
                resultContent, 2, false, false);
            if (result) {
                break;
            }
            try {
                Thread.sleep(2000);
                continue;
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        if (!result) {
            LOG.error("Failed to publish device registration result, deviceNo:{},registState{}", deviceNo, state.getNumber());
        }
    }

    /**
     * 保存设备升级结果
     * 
     * @param deviceNo
     * @param message
     */
    public void saveDeviceUpgradeResult(String deviceNo, MqttMessage message) {
        try {
            if (!setTenantContextHolderByDeviceNo(deviceNo)) {
                LOG.error("Save device upgrade result in multi-tenant mode, maintenance thread tenant ID failed, deviceNo:[{}]", deviceNo);
                return;
            }
            doSaveDeviceUpgradeResult(deviceNo, message);
        } finally {
            TenantContextHolder.clear();
        }
    }

    /**
     * 保存设备升级结果
     * 
     * @param deviceNo
     * @param message
     */
    private void doSaveDeviceUpgradeResult(String deviceNo, MqttMessage message) {
        LOG.debug("Write back the device upgrade result information, deviceNo:{}", deviceNo);
        if (deviceNo.length() > 48) {
            LOG.error("The device code length is not allowed to be greater than 48 bits, deviceNo:[{}]", deviceNo);
            return;
        }
        byte[] payload = message.getPayload();
        if (null == payload) {
            LOG.error("The device upgrade result information is empty, deviceNo:{},messageId:{}, qos:{}", deviceNo, message.getId(), message.getQos());
            return;
        }
        UpdateResultMessage updateResultMessage = null;
        try {
            updateResultMessage = UpdateResultMessage.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            LOG.error(e.getMessage());
            return;
        }
        // 版本名称不能为空
        String versionName = updateResultMessage.getVersionName();
        if (StringUtils.isBlank(versionName) || versionName.length() > 48) {
            LOG.error("The version name [versionName] cannot be empty and the length cannot be greater than 48");
            return;
        }
        // 升级版本号不能为空
        String updateVersion = updateResultMessage.getUpdateVersion();
        if (StringUtils.isBlank(updateVersion) || updateVersion.length() > 48) {
            LOG.error("The upgrade version number [updateVersion] cannot be empty and its length cannot be greater than 48");
            return;
        }
        // 升级前版本号不能为空
        String beforeVersion = updateResultMessage.getBeforeVersion();
        if (StringUtils.isNotBlank(beforeVersion) && beforeVersion.length() > 48) {
            LOG.error("The length of the pre-upgrade version number [beforeVersion] cannot be greater than 48 when it is not empty.");
            return;
        }
        // 更新结果不能为空
        UpdateState updateState = updateResultMessage.getUpdateState();
        if (null == updateState) {
            LOG.error("Update result [updateState] cannot be empty");
            return;
        }
        boolean updateResult = UpdateState.SUCCESS_VALUE == updateState.getNumber();
        // 更新开始时间不能为空
        String startTime = updateResultMessage.getStartTime();
        if (StringUtils.isBlank(startTime)) {
            LOG.error("Update start time [startTime] cannot be empty");
            return;
        }
        try {
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime);
        } catch (Exception e) {
            LOG.error("Update start time [startTime] is malformed: {}", e.getMessage());
            return;
        }
        // 更新耗时
        int timeUsed = updateResultMessage.getTimeUsed();
        // 失败原因
        String failReason = updateResultMessage.getFailReason();
        try {
            deviceUpgradeTaskService.saveUpgradeResult(versionName, beforeVersion, updateVersion, deviceNo,
                updateResult, startTime, timeUsed, failReason);
        } catch (CustomException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * 设备参数下发结果回写
     * 
     * @param deviceNo
     * @param message
     */
    public void saveDeviceParamConfigResult(String deviceNo, MqttMessage message) {
        try {
            if (!setTenantContextHolderByDeviceNo(deviceNo)) {
                LOG.error("In the multi-tenant mode, the result of saving the device parameter delivery, the maintenance thread tenant ID fails, deviceNo:[{}]", deviceNo);
                return;
            }
            doSaveDeviceParamConfigResult(deviceNo, message);
        } finally {
            TenantContextHolder.clear();
        }
    }

    /**
     * 设备参数下发结果回写
     * 
     * @param deviceNo
     * @param message
     */
    private void doSaveDeviceParamConfigResult(String deviceNo, MqttMessage message) {
        LOG.debug("Write-back of device parameter delivery result information, deviceNo:{}", deviceNo);
        if (deviceNo.length() > 48) {
            LOG.error("The device code length is not allowed to be greater than 48 bits, deviceNo:[{}]", deviceNo);
            return;
        }
        byte[] payload = message.getPayload();
        if (null == payload) {
            LOG.error("Device parameter delivery result information is empty, deviceNo:{},messageId:{}, qos:{}", deviceNo, message.getId(), message.getQos());
            return;
        }
        ConfigResultMessage configResultMessage = null;
        try {
            configResultMessage = ConfigResultMessage.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            LOG.error(e.getMessage());
            return;
        }
        // 设备参数下发日志索引
        long sortIndex = configResultMessage.getSortIndex();
        if (0 == sortIndex) {
            LOG.error("Device parameter delivery log index [sortIndex] is invalid");
            return;
        }
        ConfigState configState = configResultMessage.getConfigState();
        if (null == configState) {
            LOG.error("Device parameter delivery result cannot be empty");
            return;
        }
        try {
            deviceParamDistributeLogService.updateParamDistributeResult(sortIndex,
                ConfigState.SUCCESS_VALUE == configState.getNumber());
        } catch (CustomException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * 根据设备编码查询设备信息，设置线程的租户ID
     * 
     * @param deviceNo
     * @return
     */
    private boolean setTenantContextHolderByDeviceNo(String deviceNo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        DeviceInfo info = new DeviceInfo();
        info.setDeviceNo(deviceNo);
        List<DeviceInfo> deviceInfoList = deviceInfoService.selectDeviceInfoList(info);
        if (CollectionUtils.isEmpty(deviceInfoList) || StringUtils.isBlank(deviceInfoList.get(0).getTenantId())) {
            LOG.error("In multi-tenant mode, device information (including tenant ID) is not maintained to the server in advance, deviceNo:[{}]", deviceNo);
            return false;
        }
        String tenantId = deviceInfoList.get(0).getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            LOG.error("In multi-tenant mode, the device does not maintain tenant information, deviceNo:[{}]", deviceNo);
            return false;
        }
        TenantContextHolder.setTenantId(tenantId);
        LOG.info("In multi-tenant mode, the corresponding tenant ID of deviceNo[{}] is [{}]", deviceNo, tenantId);
        return true;
    }
}
