package cn.eyecool.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.annotation.SqlParser;

import cn.eyecool.common.core.domain.entity.SysMenu;

/**
 * 菜单表 数据层
 *
 * @author admin
 */
@SuppressWarnings("deprecation")
public interface SysMenuMapper {
    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 根据用户所有权限
     *
     * @return 权限列表
     */
    public List<String> selectMenuPerms();

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     * 
     * @param roleId 角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    public List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId,
        @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    public SysMenu selectMenuById(Long menuId);

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int hasChildByMenuId(Long menuId);

    /**
     * 新增菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public int insertMenu(SysMenu menu);

    /**
     * 修改菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public int updateMenu(SysMenu menu);

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int deleteMenuById(Long menuId);

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @return 结果
     */
    public SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);

    /**
     * 根据租户角色ID查询菜单列表
     *
     * @param menu
     * @return
     */
    @SqlParser(filter = true)
    public List<SysMenu> selectMenuListByTenantRoleId(SysMenu menu);

    /**
     * 根据租户角色ID查询菜单树
     *
     * @param tenantRoleId
     * @return
     */
    @SqlParser(filter = true)
    public List<SysMenu> selectMenuTreeAllByTenantRoleId(String tenantRoleId);

    /**
     * 根据租户角色ID查询权限
     * 
     * @param tenantId
     * @return
     */
    @SqlParser(filter = true)
    public List<String> selectMenuPermsByTenantRoleId(String tenantRoleId);

    /**
     * 查询租户角色菜单列表
     * 
     * @param tenantRoleId 租户角色Id
     * @param menuCheckStrictly 菜单树选项是否关联显示
     * @return
     */
    @SqlParser(filter = true)
    public List<Long> selectMenuIdListByTenantRoleId(@Param("tenantRoleId") String tenantRoleId,
        @Param("menuCheckStrictly") boolean menuCheckStrictly);
}
