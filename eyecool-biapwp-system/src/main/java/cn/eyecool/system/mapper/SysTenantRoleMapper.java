package cn.eyecool.system.mapper;

import java.util.List;

import cn.eyecool.common.core.domain.entity.SysTenantRole;

/**
 * 租户角色表 数据层
 * 
 * @author admin
 */
public interface SysTenantRoleMapper {
    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    public List<SysTenantRole> selectTenantRoleList(SysTenantRole role);

    /**
     * 根据用户ID查询角色
     * 
     * @param tenantId 租户ID
     * @return 角色列表
     */
    public List<SysTenantRole> selectTenantRolePermissionByTenantId(String tenantId);

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
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    public SysTenantRole selectTenantRoleById(String id);

    /**
     * 校验角色名称是否唯一
     * 
     * @param roleName 角色名称
     * @return 角色信息
     */
    public SysTenantRole checkTenantRoleNameUnique(String roleName);

    /**
     * 校验角色权限是否唯一
     * 
     * @param roleKey 角色权限
     * @return 角色信息
     */
    public SysTenantRole checkTenantRoleKeyUnique(String roleKey);

    /**
     * 修改角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    public int updateTenantRole(SysTenantRole role);

    /**
     * 新增角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    public int insertTenantRole(SysTenantRole role);

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
