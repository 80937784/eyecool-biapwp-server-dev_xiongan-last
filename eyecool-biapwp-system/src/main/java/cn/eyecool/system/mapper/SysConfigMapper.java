package cn.eyecool.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.system.domain.SysConfig;

/**
 * 参数配置 数据层
 * 
 * @author admin
 */
@SuppressWarnings("deprecation")
public interface SysConfigMapper {
    /**
     * 查询参数配置信息
     * 
     * @param config 参数配置信息
     * @return 参数配置信息
     */
    public SysConfig selectConfig(SysConfig config);

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    public List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    @SqlParser(filter = true)
    public SysConfig checkConfigKeyUnique(String configKey);

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    public int insertConfig(SysConfig config);

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    public int updateConfig(SysConfig config);

    /**
     * 删除参数配置
     * 
     * @param configId 参数ID
     * @return 结果
     */
    public int deleteConfigById(Long configId);

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    public int deleteConfigByIds(Long[] configIds);

    /**
     * 新增租户参数设置
     * 
     * @param config
     * @return
     */
    public int insertTenantConfig(SysConfig config);

    /**
     * 修改租户参数设置
     * 
     * @param config
     */
    public int updateTenantConfig(SysConfig config);

    /**
     * 删除租户参数信息
     * 
     * @param config
     */
    public int deleteTenantConfigByConfigId(Long configId);

    /**
     * 批量删除租户参数信息
     * 
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    public int deleteTenantConfigByConfigIds(Long[] configIds);

    /**
     * 根据参数key获取多租户参数列表
     * 
     * @param configKey
     * @return
     */
    @SqlParser(filter = true)
    public List<SysConfig> getTenantConfigListByConfigKey(String configKey);
}
