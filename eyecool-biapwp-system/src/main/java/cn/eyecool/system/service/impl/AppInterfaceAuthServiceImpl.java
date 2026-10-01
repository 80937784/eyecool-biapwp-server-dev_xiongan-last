package cn.eyecool.system.service.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.system.constant.RedisKeyConstants;
import cn.eyecool.system.domain.AppInfo;
import cn.eyecool.system.domain.AppInterfaceAuth;
import cn.eyecool.system.mapper.AppInfoMapper;
import cn.eyecool.system.mapper.AppInterfaceAuthMapper;
import cn.eyecool.system.service.IAppInterfaceAuthService;

/**
 * 应用接口授权Service业务层处理
 * 
 * @author admin
 * @date 2021-04-14
 */
@Service
public class AppInterfaceAuthServiceImpl implements IAppInterfaceAuthService {

    @Autowired
    private AppInterfaceAuthMapper appInterfaceAuthMapper;
    @Autowired
    private AppInfoMapper appInfoMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询应用接口授权
     * 
     * @param id 应用接口授权ID
     * @return 应用接口授权
     */
    @Override
    public AppInterfaceAuth selectAppInterfaceAuthById(String id) {
        return appInterfaceAuthMapper.selectAppInterfaceAuthById(id);
    }

    /**
     * 查询应用接口授权列表
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 应用接口授权
     */
    @Override
    public List<AppInterfaceAuth> selectAppInterfaceAuthList(AppInterfaceAuth appInterfaceAuth) {
        return appInterfaceAuthMapper.selectAppInterfaceAuthList(appInterfaceAuth);
    }

    /**
     * 新增应用接口授权
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 结果
     */
    @Override
    @Transactional
    public int insertAppInterfaceAuth(AppInterfaceAuth appInterfaceAuth) {
        String[] transcodeArray = Convert.toStrArray(appInterfaceAuth.getTransCode());
        int result = 0;
        for (String transCode : transcodeArray) {
            // 校验授权是否已存在
            AppInterfaceAuth auth = new AppInterfaceAuth();
            auth.setAppId(appInterfaceAuth.getAppId());
            auth.setTransCode(transCode);
            List<AppInterfaceAuth> authList = appInterfaceAuthMapper.selectAppInterfaceAuthList(auth);
            if (CollectionUtils.isNotEmpty(authList)) {
                throw new CustomException(MessageUtils.message("appinterface.auth.service.been.auth",transCode));
            }
            BeanUtils.copyBeanProp(auth, appInterfaceAuth);
            auth.setTransCode(transCode);
            auth.setId(IdWorker.getNextStringId());
            try {
                auth.setCreateBy(SecurityUtils.getUsername());
            } catch (Exception e) {
            }
            auth.setCreateTime(DateUtils.getNowDate());
            result += appInterfaceAuthMapper.insertAppInterfaceAuth(auth);
        }
        return result;
    }

    /**
     * 修改应用接口授权
     * 
     * @param appInterfaceAuth 应用接口授权
     * @return 结果
     */
    @Override
    @Transactional
    public int updateAppInterfaceAuth(AppInterfaceAuth appInterfaceAuth) {
        try {
            appInterfaceAuth.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        appInterfaceAuth.setUpdateTime(DateUtils.getNowDate());
        int row = appInterfaceAuthMapper.updateAppInterfaceAuth(appInterfaceAuth);
        if (row > 0) {
            AppInfo appInfo = appInfoMapper.selectAppInfoById(appInterfaceAuth.getAppId());
            redisCache.setCacheMapValue(RedisKeyConstants.APP_INTERFACE_REDIS_KEY_PREFIX + appInfo.getAppKey(),
                appInterfaceAuth.getTransCode(), appInterfaceAuth);
        }
        return row;
    }

    /**
     * 批量删除应用接口授权
     * 
     * @param ids 需要删除的应用接口授权ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAppInterfaceAuthByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteAppInterfaceAuthById(id);
        }
        return result;
    }

    /**
     * 删除应用接口授权信息
     * 
     * @param id 应用接口授权ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAppInterfaceAuthById(String id) {
        AppInterfaceAuth auth = appInterfaceAuthMapper.selectAppInterfaceAuthById(id);
        AppInfo appInfo = appInfoMapper.selectAppInfoById(auth.getAppId());
        int rows = appInterfaceAuthMapper.deleteAppInterfaceAuthById(id);
        if (rows > 0) {
            redisCache.deleteCacheMapValue(RedisKeyConstants.APP_INTERFACE_REDIS_KEY_PREFIX + appInfo.getAppKey(),
                auth.getTransCode());
        }
        return rows;
    }

    /**
     * 查询App应用接口权限
     * 
     * @param appId
     * @param appKey
     * @param transCode
     * @return
     */
    @Override
    public AppInterfaceAuth selectAppInterfaceAuth(String appId, String appKey, String transCode) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appKey) || StringUtils.isBlank(transCode)) {
            throw new IllegalArgumentException();
        }
        AppInterfaceAuth appInterfaceAuth = (AppInterfaceAuth)redisCache
            .getCacheMapValue(RedisKeyConstants.APP_INTERFACE_REDIS_KEY_PREFIX + appKey, transCode);
        if (null == appInterfaceAuth) {
            AppInterfaceAuth authCondition = new AppInterfaceAuth();
            authCondition.setAppId(appId);
            authCondition.setTransCode(transCode);
            List<AppInterfaceAuth> authList = appInterfaceAuthMapper.selectAppInterfaceAuthList(authCondition);
            if (CollectionUtils.isNotEmpty(authList)) {
                appInterfaceAuth = authList.get(0);
                redisCache.setCacheMapValue(RedisKeyConstants.APP_INTERFACE_REDIS_KEY_PREFIX + appKey, transCode,
                    appInterfaceAuth);
            }
        }
        return appInterfaceAuth;
    }
}
