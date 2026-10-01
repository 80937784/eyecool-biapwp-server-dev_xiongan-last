package cn.eyecool.system.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.constant.RedisKeyConstants;
import cn.eyecool.system.domain.AppInfo;
import cn.eyecool.system.domain.AppInterfaceAuth;
import cn.eyecool.system.mapper.AppInfoMapper;
import cn.eyecool.system.mapper.AppInterfaceAuthMapper;
import cn.eyecool.system.service.IAppInfoService;
import cn.eyecool.system.util.AppUtils;

/**
 * 应用系统信息Service业务层处理
 * 
 * @author admin
 * @date 2021-04-14
 */
@Service
public class AppInfoServiceImpl implements IAppInfoService {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private AppInfoMapper appInfoMapper;
    @Autowired
    private AppInterfaceAuthMapper appInterfaceAuthMapper;

    /**
     * 查询应用系统信息
     * 
     * @param id 应用系统信息ID
     * @return 应用系统信息
     */
    @Override
    public AppInfo selectAppInfoById(String id) {
        return appInfoMapper.selectAppInfoById(id);
    }

    /**
     * 查询应用系统信息列表
     * 
     * @param appInfo 应用系统信息
     * @return 应用系统信息
     */
    @Override
    public List<AppInfo> selectAppInfoList(AppInfo appInfo) {
        return appInfoMapper.selectAppInfoList(appInfo);
    }

    /**
     * 新增应用系统信息
     * 
     * @param appInfo 应用系统信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertAppInfo(AppInfo appInfo) {
        // 校验应用系统是否存在
        AppInfo condition = new AppInfo();
        condition.setAppDesc(appInfo.getAppDesc());
        List<AppInfo> appList = appInfoMapper.selectAppInfoList(condition);
        if (CollectionUtils.isNotEmpty(appList)) {
            throw new CustomException(MessageUtils.message("appinfo.service.app.exists"));
        }
        if (StringUtils.isNotBlank(appInfo.getAppKey())) {
            condition.setAppDesc(null);
            condition.setAppKey(appInfo.getAppKey());
            appList = appInfoMapper.selectAppInfoList(condition);
            if (CollectionUtils.isNotEmpty(appList)) {
                throw new CustomException(MessageUtils.message("appinfo.service.appkey.exists"));
            }
        } else {
            String appKey = AppUtils.getAppId();
            appInfo.setAppKey(appKey);
        }
        if (StringUtils.isBlank(appInfo.getAppSecret())) {
            String appSecret = AppUtils.getAppSecret(appInfo.getAppKey());
            appInfo.setAppSecret(appSecret);
        }
        appInfo.setId(IdWorker.getNextStringId());
        try {
            appInfo.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
        }
        appInfo.setCreateTime(DateUtils.getNowDate());
        appInfo.setBuiltIn(DictConstants.YesOrNoState.NO);
        return appInfoMapper.insertAppInfo(appInfo);
    }

    /**
     * 修改应用系统信息
     * 
     * @param appInfo 应用系统信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateAppInfo(AppInfo appInfo) {
        appInfo.setUpdateTime(DateUtils.getNowDate());
        int result = appInfoMapper.updateAppInfo(appInfo);
        redisCache.setCacheObject(RedisKeyConstants.APP_MGR_REDIS_KEY_PREFIX + appInfo.getAppKey(), appInfo);
        return result;
    }

    /**
     * 批量删除应用系统信息
     * 
     * @param ids 需要删除的应用系统信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAppInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteAppInfoById(id);
        }
        return result;
    }

    /**
     * 删除应用系统信息信息
     * 
     * @param id 应用系统信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteAppInfoById(String id) {
        AppInfo appInfo = appInfoMapper.selectAppInfoById(id);
        if (DictConstants.YesOrNoState.YES.equals(appInfo.getBuiltIn())) {
            throw new CustomException(MessageUtils.message("appinfo.service.appkey.not.allow.delete",appInfo.getAppKey()));
        }
        int result = appInfoMapper.deleteAppInfoById(id);
        // 级联删除授权信息
        AppInterfaceAuth auth = new AppInterfaceAuth();
        auth.setAppId(id);
        List<AppInterfaceAuth> authList = appInterfaceAuthMapper.selectAppInterfaceAuthList(auth);
        if (CollectionUtils.isNotEmpty(authList)) {
            List<String> authIdList = authList.stream().map(AppInterfaceAuth::getId).collect(Collectors.toList());
            appInterfaceAuthMapper.deleteAppInterfaceAuthByIds(authIdList.toArray(new String[authIdList.size()]));
        }
        redisCache.deleteObject(RedisKeyConstants.APP_MGR_REDIS_KEY_PREFIX + appInfo.getAppKey());
        return result;
    }

    /**
     * 根据AppKey查询app信息
     * 
     * @param appKey
     * @return
     */
    @Override
    public AppInfo selectAppInfoByAppkey(String appKey) {
        AppInfo appInfo = (AppInfo)redisCache.getCacheObject(RedisKeyConstants.APP_MGR_REDIS_KEY_PREFIX + appKey);
        if (null == appInfo) {
            AppInfo condition = new AppInfo();
            condition.setAppKey(appKey);
            List<AppInfo> appList = appInfoMapper.selectAppInfoList(condition);
            if (CollectionUtils.isNotEmpty(appList)) {
                appInfo = appList.get(0);
                redisCache.setCacheObject(RedisKeyConstants.APP_MGR_REDIS_KEY_PREFIX + appKey, appInfo);
            }
        }
        return appInfo;
    }
}
