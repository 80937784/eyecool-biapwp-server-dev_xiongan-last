package cn.eyecool.device.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.mqtt.PlatformMqttClientUtil;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.device.constant.MqttTopicConstants;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceParamDistributeLog;
import cn.eyecool.device.mapper.DeviceParamDistributeLogMapper;
import cn.eyecool.device.proto.EdgeProto;
import cn.eyecool.device.proto.EdgeProto.ConfigMessage;
import cn.eyecool.device.proto.EdgeProto.ConfigMessage.Builder;
import cn.eyecool.device.service.IDeviceParamDistributeLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 参数下发日志Service业务层处理
 * 
 * @author admin
 * @date 2021-04-13
 */
@Service
@Slf4j
public class DeviceParamDistributeLogServiceImpl implements IDeviceParamDistributeLogService {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private DeviceParamDistributeLogMapper deviceParamDistributeLogMapper;
    @Autowired
    private MqttAuthAesKeyComponent mqttAuthAesKeyComponent;

    @Value("${mqtt.enabled}")
    private boolean mqttEnabled;
    @Value("${mqtt.device-param.encrypt.enabled:true}")
    private boolean paramValueEncryptEnabled;

    private static final String PARAM_APP_KEY = "appKey";
    private static final String PARAM_APP_SECRECT = "appSecret";
    private static final String PARAM_APP_MANAGE_PWD = "appManagePwd";

    private static final Object lockObj = new Object();

    /**
     * 查询参数下发日志
     * 
     * @param id 参数下发日志ID
     * @return 参数下发日志
     */
    @Override
    public DeviceParamDistributeLog selectDeviceParamDistributeLogById(String id) {
        return deviceParamDistributeLogMapper.selectDeviceParamDistributeLogById(id);
    }

    /**
     * 查询参数下发日志列表
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 参数下发日志
     */
    @Override
    public List<DeviceParamDistributeLog>
        selectDeviceParamDistributeLogList(DeviceParamDistributeLog deviceParamDistributeLog) {
        return deviceParamDistributeLogMapper.selectDeviceParamDistributeLogList(deviceParamDistributeLog);
    }

    /**
     * 新增参数下发日志
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDeviceParamDistributeLog(DeviceParamDistributeLog deviceParamDistributeLog) {
        if (!mqttEnabled) {
            throw new CustomException(MessageUtils.message("device.param.distribute.not.find.mqtt"));
        }
        Map<String, Object> params = deviceParamDistributeLog.getParams();
        if (null == params || params.size() == 0) {
            throw new CustomException(MessageUtils.message("device.param.distribute.params.empty"));
        }
        List<DeviceParamDistributeLog> logList = Lists.newArrayList();
        String[] deviceNoArray = Convert.toStrArray(deviceParamDistributeLog.getDeviceNo());
        Map<String, Long> deviceIndexMap = Maps.newHashMap();
        for (String deviceNo : deviceNoArray) {
            Long sortIndex = incrementAndGetSortIndex();
            deviceIndexMap.put(deviceNo, sortIndex);
            params.keySet().forEach(it -> {
                DeviceParamDistributeLog log = new DeviceParamDistributeLog();
                log.setDeviceNo(deviceNo);
                log.setParamCode(it);
                log.setParamValue(params.get(it).toString());
                log.setCreateTime(DateUtils.getNowDate());
                log.setId(IdWorker.getNextStringId());
                log.setSortIndex(sortIndex);
                logList.add(log);
            });
        }
        for (DeviceParamDistributeLog log : logList) {
            deviceParamDistributeLogMapper.insertDeviceParamDistributeLog(log);
        }
        List<String> succDeviceNoList = publishConfigParam(params, deviceIndexMap);
        if (CollectionUtils.isEmpty(succDeviceNoList)) {
            deviceIndexMap.keySet().stream().forEach(deviceNo -> {
                deviceParamDistributeLogMapper.updateDistributeLogBySortIndex(deviceIndexMap.get(deviceNo),
                    Constants.STATUS_ONE, DateUtils.getNowDate());
            });
            throw new CustomException(MessageUtils.message("device.param.distribute.params.distribute.failed"));
        }
        deviceIndexMap.keySet().stream().filter(deviceNo -> !succDeviceNoList.contains(deviceNo)).forEach(deviceNo -> {
            deviceParamDistributeLogMapper.updateDistributeLogBySortIndex(deviceIndexMap.get(deviceNo),
                Constants.STATUS_ONE, DateUtils.getNowDate());
        });
        return succDeviceNoList.size();
    }

    /**
     * 查询最大的参数下发排序索引
     * 
     * @return
     */
    private Long incrementAndGetSortIndex() {
        Long sortIndex = redisCache.incrementAndGet(RedisKeyConstants.DEVICE_PARAM_INDEX_CACHE_KEY);
        if (null != sortIndex) {
            if (log.isDebugEnabled()) {
                log.debug("Get the current maximum device parameter from the cache to deliver the log sorting index:[{}]", sortIndex);
            }
            return sortIndex;
        }
        synchronized (lockObj) {
            sortIndex = redisCache.incrementAndGet(RedisKeyConstants.DEVICE_PARAM_INDEX_CACHE_KEY);
            if (null == sortIndex) {
                Long maxSortIndex = deviceParamDistributeLogMapper.selectMaxSortIndex();
                if (null == maxSortIndex) {
                    maxSortIndex = 1L;
                }
                sortIndex = redisCache.addAndGetLong(RedisKeyConstants.DEVICE_PARAM_INDEX_CACHE_KEY, maxSortIndex);
            }
        }
        return sortIndex;
    }

    /**
     * 执行参数下发发布
     * 
     * @param params
     * @param deviceIndexMap
     * @param paramTypeMap
     */
    private List<String> publishConfigParam(Map<String, Object> params, Map<String, Long> deviceIndexMap) {
        PlatformMqttClientUtil mqttClientUtil = SpringUtils.getBean(PlatformMqttClientUtil.class);
        Builder newBuilder = EdgeProto.ConfigMessage.newBuilder();
        // 对参数中的appKey和appSecrect加密处理
        params.keySet().forEach(key -> {
            if (paramValueEncryptEnabled
                && (PARAM_APP_KEY.equals(key) || PARAM_APP_SECRECT.equals(key) || PARAM_APP_MANAGE_PWD.equals(key))) {
                try {
                    params.put(key,
                        AESUtils.encrypt((String)params.get(key), mqttAuthAesKeyComponent.getDeviceMqttAuthSkey()));
                } catch (Exception e) {
                    throw new CustomException(e.getMessage(), e);
                }
            }
            newBuilder.addConfigItem(EdgeProto.ConfigMessage.ConfigItem.newBuilder().setConfigKey(key)
                .setConfigValue(params.get(key).toString()).build());
        });
        List<String> succDeviceNoList = Lists.newArrayList();
        deviceIndexMap.keySet().forEach(deviceNo -> {
            newBuilder.setSortIndex(deviceIndexMap.get(deviceNo));
            ConfigMessage message = newBuilder.build();
            boolean res =
                mqttClientUtil.publishMessage(MqttTopicConstants.PUB_DEVICE_CONFIG_DISTRIBUTE_TOPIC_PREFIX + deviceNo,
                    message.toByteArray(), 2, false, false);
            if (res) {
                succDeviceNoList.add(deviceNo);
            }
        });
        return succDeviceNoList;
    }

    /**
     * 修改参数下发日志
     * 
     * @param deviceParamDistributeLog 参数下发日志
     * @return 结果
     */
    @Override
    public int updateDeviceParamDistributeLog(DeviceParamDistributeLog deviceParamDistributeLog) {
        deviceParamDistributeLog.setUpdateTime(DateUtils.getNowDate());
        return deviceParamDistributeLogMapper.updateDeviceParamDistributeLog(deviceParamDistributeLog);
    }

    /**
     * 批量删除参数下发日志
     * 
     * @param ids 需要删除的参数下发日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceParamDistributeLogByIds(String[] ids) {
        return deviceParamDistributeLogMapper.deleteDeviceParamDistributeLogByIds(ids);
    }

    /**
     * 删除参数下发日志信息
     * 
     * @param id 参数下发日志ID
     * @return 结果
     */
    @Override
    public int deleteDeviceParamDistributeLogById(String id) {
        return deviceParamDistributeLogMapper.deleteDeviceParamDistributeLogById(id);
    }

    /**
     * 回写参数下发结果
     * 
     * @param sortIndex
     * @param result
     */
    @Override
    @Transactional
    public void updateParamDistributeResult(long sortIndex, boolean result) {
        deviceParamDistributeLogMapper.updateDistributeLogBySortIndex(sortIndex,
            result ? Constants.STATUS_ZERO : Constants.STATUS_ONE, DateUtils.getNowDate());

    }
}
