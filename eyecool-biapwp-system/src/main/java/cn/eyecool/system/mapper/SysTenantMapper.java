package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysTenant;

/**
 * 租户信息Mapper接口
 * 
 * @author admin
 * @date 2020-11-04
 */
public interface SysTenantMapper {
    /**
     * 查询租户信息
     * 
     * @param id 租户信息ID
     * @return 租户信息
     */
    public SysTenant selectSysTenantById(Long id);

    /**
     * 查询租户信息列表
     * 
     * @param sysTenant 租户信息
     * @return 租户信息集合
     */
    public List<SysTenant> selectSysTenantList(SysTenant sysTenant);

    /**
     * 新增租户信息
     * 
     * @param sysTenant 租户信息
     * @return 结果
     */
    public int insertSysTenant(SysTenant sysTenant);

    /**
     * 修改租户信息
     * 
     * @param sysTenant 租户信息
     * @return 结果
     */
    public int updateSysTenant(SysTenant sysTenant);

    /**
     * 删除租户信息
     * 
     * @param id 租户信息ID
     * @return 结果
     */
    public int deleteSysTenantById(Long id);

    /**
     * 批量删除租户信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysTenantByIds(Long[] ids);

    /**
     * 根据tenantId查询租户信息
     * 
     * @param tenantId
     * @return
     */
    public SysTenant selectSysTenantByTenantId(String tenantId);

    /**
     * 根据租户名称查询租户信息
     * 
     * @param tenantName
     * @return
     */
    public SysTenant selectSysTenantByTenantName(String tenantName);

    /**
     * 查询最大租户ID
     * 
     * @return
     */
    public String selectMaxTenantId();

    /**
     * 根据手机号查询租户信息
     * 
     * @param phone
     * @return
     */
    public SysTenant selectSysTenantByPhone(String phone);

    /**
     * 查询租户角色使用数量
     * 
     * @param tenantRoleId
     * @return
     */
    public int countTenantRoleByRoleId(String tenantRoleId);

}
