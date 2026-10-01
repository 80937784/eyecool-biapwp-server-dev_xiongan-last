package cn.eyecool.system.service;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysTenant;

/**
 * 租户信息Service接口
 * 
 * @author admin
 * @date 2020-11-04
 */
public interface ISysTenantService {
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
     * 批量删除租户信息
     * 
     * @param ids 需要删除的租户信息ID
     * @return 结果
     */
    public int deleteSysTenantByIds(Long[] ids);

    /**
     * 删除租户信息信息
     * 
     * @param id 租户信息ID
     * @return 结果
     */
    public int deleteSysTenantById(Long id);

    /**
     * 查询租户信息
     * 
     * @param tenantId 租户ID
     * @return
     */
    public SysTenant selectSysTenantByTenantId(String tenantId);

    /**
     * 校验租户名称是否唯一
     * 
     * @param excludeTenantId
     * @param tenantName
     * @return
     */
    boolean checkTenantNameUnique(String excludeTenantId, String tenantName);

    /**
     * 校验租户预留手机号是否唯一
     * 
     * @param excludeTenantId
     * @param phone
     * @return
     */
    boolean checkPhoneUnique(String excludeTenantId, String phone);
}
