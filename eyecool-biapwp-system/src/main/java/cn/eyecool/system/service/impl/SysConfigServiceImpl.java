package cn.eyecool.system.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.annotation.DataSource;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.enums.DataSourceType;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.domain.SysConfig;
import cn.eyecool.system.mapper.SysConfigMapper;
import cn.eyecool.system.service.ISysConfigService;

/**
 * 参数配置 服务层实现
 * 
 * @author admin
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService {
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList) {
            redisCache.setCacheObject(getCacheKey(config.getTenantId(), config.getConfigKey()),
                config.getConfigValue());
        }
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    @DataSource(DataSourceType.MASTER)
    public SysConfig selectConfigById(Long configId) {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey) {
        String tenantId = TenantContextHolder.getTenantId();
        String configValue = Convert.toStr(redisCache.getCacheObject(getCacheKey(tenantId, configKey)));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);

        if (StringUtils.isNotNull(retConfig)) {
            redisCache.setCacheObject(getCacheKey(retConfig.getTenantId(), configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }

        return StringUtils.EMPTY;
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config) {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config) {
        int row = configMapper.insertConfig(config);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(null, config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config) {
        String tenantId = TenantContextHolder.getTenantId();
        int row = 0;
        if (StringUtils.isBlank(tenantId) || UserConstants.SUPER_TENANT.equals(tenantId)) {
            row = configMapper.updateConfig(config);
        } else {
            SysConfig configCondition = new SysConfig();
            configCondition.setConfigId(config.getConfigId());
            SysConfig retConfig = configMapper.selectConfig(configCondition);
            if (DictConstants.YesOrNoState.NO.equals(retConfig.getTenantMaintain())) {
                throw new CustomException(MessageUtils.message("sysconfig.service.tenant.prohibit.param"));
            }
            if (StringUtils.isBlank(retConfig.getTenantId())) {
                config.setUpdateTime(DateUtils.getNowDate());
                row = configMapper.insertTenantConfig(config);
            } else {
                config.setUpdateTime(DateUtils.getNowDate());
                row = configMapper.updateTenantConfig(config);
            }
        }
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(tenantId, config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType())) {
                throw new CustomException(MessageUtils.message("sysconfig.service.param.not.delete", config.getConfigKey()));
            }
        }
        int count = configMapper.deleteConfigByIds(configIds);
        configMapper.deleteTenantConfigByConfigIds(configIds);
        if (count > 0) {
            Collection<String> keys = redisCache.keys(Constants.SYS_CONFIG_KEY + "*");
            redisCache.deleteObject(keys);
        }
        return count;
    }

    /**
     * 清空缓存数据
     */
    @Override
    public void clearCache() {
        String tenantId = TenantContextHolder.getTenantId();
        Collection<String> keys = Collections.emptyList();
        if (StringUtils.isBlank(tenantId) || UserConstants.SUPER_TENANT.equals(tenantId)) {
            keys = redisCache.keys(Constants.SYS_CONFIG_KEY + "*");
        } else {
            keys = redisCache.keys(Constants.SYS_CONFIG_KEY + tenantId + ":*");
        }
        redisCache.deleteObject(keys);
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public String checkConfigKeyUnique(SysConfig config) {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String tenantId, String configKey) {
        if (StringUtils.isBlank(tenantId) || UserConstants.SUPER_TENANT.equals(tenantId)) {
            return Constants.SYS_CONFIG_KEY + configKey;
        }
        return Constants.SYS_CONFIG_KEY + tenantId + ":" + configKey;
    }

    /**
     * 根据参数key获取多租户参数列表
     * 
     * @return
     */
    @Override
    public List<SysConfig> getTenantConfigListByConfigKey(String configKey) {
        return configMapper.getTenantConfigListByConfigKey(configKey);
    }
}
