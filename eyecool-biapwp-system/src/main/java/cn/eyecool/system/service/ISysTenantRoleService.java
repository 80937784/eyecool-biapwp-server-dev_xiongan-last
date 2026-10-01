package cn.eyecool.system.service;

import java.util.List;
import java.util.Set;

import cn.eyecool.common.core.domain.entity.SysTenantRole;

/**
 * 租户角色业务层
 * 
 * @author admin
 */
public interface ISysTenantRoleService {
    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    public List<SysTenantRole> selectTenantRoleList(SysTenantRole role);

    /**
     * 根据租户ID查询角色
     * 
     * @param tenantId 租户ID
     * @return 权限列表
     */
    public Set<String> selectTenantRolePermissionByTenantId(String tenantId);

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    public List<SysTenantRole> selectTenantRoleAll();

    /**
     * 根据租户ID获取角色选择框列表
     * 
     * @param tenantId 租户ID
     * @return 选中角色ID列表
     */
    public List<Integer> selectTenantRoleListByTenantId(String tenantId);

    /**
     * 通过角色ID查询角色
     * 
     * @param id 角色ID
     * @return 角色对象信息
     */
    public SysTenantRole selectTenantRoleById(String id);

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    public String checkTenantRoleNameUnique(SysTenantRole role);

    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    public String checkTenantRoleKeyUnique(SysTenantRole role);

    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    public void checkTenantRoleAllowed(SysTenantRole role);

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int countTenantRoleByRoleId(String id);

    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    public int insertTenantRole(SysTenantRole role);

    /**
     * 修改保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    public int updateTenantRole(SysTenantRole role);

    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    public int updateTenantRoleStatus(SysTenantRole role);

    /**
     * 通过角色ID删除角色
     * 
     * @param id 角色ID
     * @return 结果
     */
    public int deleteTenantRoleById(String id);

    /**
     * 批量删除角色信息
     * 
     * @param ids 需要删除的角色ID
     * @return 结果
     */
    public int deleteTenantRoleByIds(String[] ids);
}
