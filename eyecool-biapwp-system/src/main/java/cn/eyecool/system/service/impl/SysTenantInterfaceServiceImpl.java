package cn.eyecool.system.service.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.PlatformDateUtils;
import cn.eyecool.system.constant.RedisKeyConstants;
import cn.eyecool.system.domain.SysTenantInterface;
import cn.eyecool.system.mapper.SysTenantInterfaceMapper;
import cn.eyecool.system.service.ISysTenantInterfaceService;

/**
 * 租户和接口关联Service业务层处理
 * 
 * @author admin
 * @date 2021-02-22
 */
@Service
public class SysTenantInterfaceServiceImpl implements ISysTenantInterfaceService {

    @Autowired
    private SysTenantInterfaceMapper sysTenantInterfaceMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询租户和接口关联列表
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 租户和接口关联
     */
    @Override
    public List<SysTenantInterface> selectSysTenantInterfaceList(SysTenantInterface sysTenantInterface) {
        return sysTenantInterfaceMapper.selectSysTenantInterfaceList(sysTenantInterface);
    }

    /**
     * 新增租户和接口关联
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 结果
     */
    @Override
    public int insertSysTenantInterface(SysTenantInterface sysTenantInterface) {
        sysTenantInterface.setCreateTime(DateUtils.getNowDate());
        return sysTenantInterfaceMapper.insertSysTenantInterface(sysTenantInterface);
    }

    /**
     * 修改租户和接口关联
     * 
     * @param sysTenantInterface 租户和接口关联
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSysTenantInterface(SysTenantInterface sysTenantInterface) {
        sysTenantInterface.setUpdateTime(DateUtils.getNowDate());
        int rows = sysTenantInterfaceMapper.updateSysTenantInterface(sysTenantInterface);
        if (rows > 0) {
            redisCache.setCacheMapValue(
                RedisKeyConstants.TENANT_INTERFACE_REDIS_KEY_PREFIX + sysTenantInterface.getTenantId(),
                sysTenantInterface.getTranscode(), sysTenantInterface);
        }
        return rows;
    }

    /**
     * 删除租户和接口关联信息
     * 
     * @param sysTenantInterface
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSysTenantInterface(SysTenantInterface sysTenantInterface) {
        int rows = sysTenantInterfaceMapper.deleteSysTenantInterface(sysTenantInterface);
        if (rows > 0) {
            redisCache
                .deleteObject(RedisKeyConstants.TENANT_INTERFACE_REDIS_KEY_PREFIX + sysTenantInterface.getTenantId());
        }
        return rows;
    }

    /**
     * 新增租户接口授权
     * 
     * @param sysTenantInterface
     * @return
     */
    @Override
    @Transactional
    public int insertAuthInterfaces(SysTenantInterface sysTenantInterface) {
        SysTenantInterface tenantInterface = new SysTenantInterface();
        tenantInterface.setCreateTime(DateUtils.getNowDate());
        tenantInterface.setTenantId(sysTenantInterface.getTenantId());
        // 默认100年内都有效
        tenantInterface.setExpireTime(PlatformDateUtils.addYears(DateUtils.getNowDate(), 100));
        int result = 0;
        String transcodes = sysTenantInterface.getTranscode();
        for (String transcode : Convert.toStrArray(transcodes)) {
            tenantInterface.setTranscode(transcode);
            result += sysTenantInterfaceMapper.insertSysTenantInterface(tenantInterface);
        }
        return result;
    }

    /**
     * 查询未分配的接口列表
     * 
     * @param dictData
     * @return
     */
    @Override
    public List<SysDictData> selectUnallocatedInterfaceList(SysDictData dictData) {
        return sysTenantInterfaceMapper.selectUnallocatedInterfaceList(dictData);
    }

    /**
     * 查询租户接口权限
     * 
     * @param tenantId
     * @param transcode
     * @return
     */
    @Override
    public SysTenantInterface selectSysTenantInterface(String tenantId, String transcode) {
        SysTenantInterface tenantInterface = (SysTenantInterface)redisCache
            .getCacheMapValue(RedisKeyConstants.TENANT_INTERFACE_REDIS_KEY_PREFIX + tenantId, transcode);
        if (null == tenantInterface) {
            SysTenantInterface condition = new SysTenantInterface();
            condition.setTenantId(tenantId);
            condition.setTranscode(transcode);
            List<SysTenantInterface> interfaceList =
                sysTenantInterfaceMapper.selectSysTenantInterfaceList(tenantInterface);
            if (CollectionUtils.isNotEmpty(interfaceList)) {
                tenantInterface = interfaceList.get(0);
                redisCache.setCacheMapValue(RedisKeyConstants.TENANT_INTERFACE_REDIS_KEY_PREFIX + tenantId, transcode,
                    tenantInterface);
            }
        }
        return tenantInterface;
    }
}
