package cn.eyecool.system.service;

import java.util.List;

import cn.eyecool.system.domain.AppInfo;

/**
 * 应用系统信息Service接口
 * 
 * @author admin
 * @date 2021-04-14
 */
public interface IAppInfoService {
    /**
     * 查询应用系统信息
     * 
     * @param id 应用系统信息ID
     * @return 应用系统信息
     */
    public AppInfo selectAppInfoById(String id);

    /**
     * 查询应用系统信息列表
     * 
     * @param appInfo 应用系统信息
     * @return 应用系统信息集合
     */
    public List<AppInfo> selectAppInfoList(AppInfo appInfo);

    /**
     * 新增应用系统信息
     * 
     * @param appInfo 应用系统信息
     * @return 结果
     */
    public int insertAppInfo(AppInfo appInfo);

    /**
     * 修改应用系统信息
     * 
     * @param appInfo 应用系统信息
     * @return 结果
     */
    public int updateAppInfo(AppInfo appInfo);

    /**
     * 批量删除应用系统信息
     * 
     * @param ids 需要删除的应用系统信息ID
     * @return 结果
     */
    public int deleteAppInfoByIds(String[] ids);

    /**
     * 删除应用系统信息信息
     * 
     * @param id 应用系统信息ID
     * @return 结果
     */
    public int deleteAppInfoById(String id);

    /**
     * 查询应用系统信息
     * 
     * @param appKey 应用系统信息appKey
     * @return 应用系统信息
     */
    public AppInfo selectAppInfoByAppkey(String appKey);
}
