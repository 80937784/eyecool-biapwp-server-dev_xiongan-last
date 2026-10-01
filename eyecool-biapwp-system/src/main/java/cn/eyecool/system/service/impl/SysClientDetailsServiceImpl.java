package cn.eyecool.system.service.impl;

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eyecool.common.constant.SsoConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.domain.SysClientDetails;
import cn.eyecool.system.mapper.SysClientDetailsMapper;
import cn.eyecool.system.service.ISysClientDetailsService;

/**
 * 终端配置Service业务层处理
 * 
 * @author admin
 * @date 2020-11-03
 */
@Service
public class SysClientDetailsServiceImpl implements ISysClientDetailsService {
    @Autowired
    private SysClientDetailsMapper sysClientDetailsMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化客户端信息到缓存
     */
    @PostConstruct
    public void init() {
        List<SysClientDetails> clientList = sysClientDetailsMapper.selectSysClientDetailsList(new SysClientDetails());
        for (SysClientDetails client : clientList) {
            redisCache.setCacheObject(getCacheKey(client.getClientId()), client);
        }
    }

    /**
     * 查询终端配置
     * 
     * @param clientId 终端配置ID
     * @return 终端配置
     */
    @Override
    public SysClientDetails selectSysClientDetailsById(String clientId) {
        SysClientDetails client = redisCache.getCacheObject(getCacheKey(clientId));
        if (StringUtils.isNotNull(client)) {
            return client;
        }
        client = sysClientDetailsMapper.selectSysClientDetailsById(clientId);
        if (StringUtils.isNotNull(client)) {
            redisCache.setCacheObject(getCacheKey(clientId), client);
            return client;
        }
        return null;
    }

    /**
     * 查询终端配置列表
     * 
     * @param sysClientDetails 终端配置
     * @return 终端配置
     */
    @Override
    public List<SysClientDetails> selectSysClientDetailsList(SysClientDetails sysClientDetails) {
        return sysClientDetailsMapper.selectSysClientDetailsList(sysClientDetails);
    }

    /**
     * 新增终端配置
     * 
     * @param sysClientDetails 终端配置
     * @return 结果
     */
    @Override
    public int insertSysClientDetails(SysClientDetails sysClientDetails) {
        sysClientDetails.setClientSecret(SecurityUtils.encryptPassword(sysClientDetails.getOriginSecret()));
        int row = sysClientDetailsMapper.insertSysClientDetails(sysClientDetails);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(sysClientDetails.getClientId()), sysClientDetails);
        }
        return row;
    }

    /**
     * 修改终端配置
     * 
     * @param sysClientDetails 终端配置
     * @return 结果
     */
    @Override
    public int updateSysClientDetails(SysClientDetails sysClientDetails) {
        sysClientDetails.setClientSecret(SecurityUtils.encryptPassword(sysClientDetails.getOriginSecret()));
        int row = sysClientDetailsMapper.updateSysClientDetails(sysClientDetails);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(sysClientDetails.getClientId()), sysClientDetails);
        }
        return row;
    }

    /**
     * 批量删除终端配置
     * 
     * @param clientIds 需要删除的终端配置ID
     * @return 结果
     */
    @Override
    public int deleteSysClientDetailsByIds(String[] clientIds) {
        int count = sysClientDetailsMapper.deleteSysClientDetailsByIds(clientIds);
        if (count > 0) {
            Collection<String> keys = redisCache.keys(SsoConstants.SYS_CLIENT_KEY + "*");
            redisCache.deleteObject(keys);
        }
        return count;
    }

    /**
     * 删除终端配置信息
     * 
     * @param clientId 终端配置ID
     * @return 结果
     */
    @Override
    public int deleteSysClientDetailsById(String clientId) {
        int count = sysClientDetailsMapper.deleteSysClientDetailsById(clientId);
        if (count > 0) {
            redisCache.deleteObject(getCacheKey(clientId));
        }
        return count;
    }

    /**
     * 清空缓存数据
     */
    @Override
    public void clearCache() {
        Collection<String> keys = redisCache.keys(SsoConstants.SYS_CLIENT_KEY + "*");
        redisCache.deleteObject(keys);
    }

    /**
     * 设置cache key
     * 
     * @param clientKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String clientKey) {
        return SsoConstants.SYS_CLIENT_KEY + clientKey;
    }

    /**
     * 校验终端编号是否唯一
     * 
     * @see cn.eyecool.system.service.ISysClientDetailsService#checkClientIdUnique(cn.eyecool.system.domain.SysClientDetails)
     * @param client
     * @return
     */
    @Override
    public String checkClientIdUnique(SysClientDetails client) {
        if (StringUtils.isBlank(client.getClientId())) {
            return UserConstants.NOT_UNIQUE;
        }
        SysClientDetails details = sysClientDetailsMapper.selectSysClientDetailsById(client.getClientId());
        if (StringUtils.isNotNull(details)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
