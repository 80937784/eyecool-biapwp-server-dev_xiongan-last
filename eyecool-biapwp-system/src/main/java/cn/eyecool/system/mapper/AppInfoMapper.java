package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.system.domain.AppInfo;

/**
 * 应用系统信息Mapper接口
 * 
 * @author admin
 * @date 2021-04-14
 */
public interface AppInfoMapper {
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
     * 删除应用系统信息
     * 
     * @param id 应用系统信息ID
     * @return 结果
     */
    public int deleteAppInfoById(String id);

    /**
     * 批量删除应用系统信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteAppInfoByIds(String[] ids);
}
