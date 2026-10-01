package cn.eyecool.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.entity.SysRole;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysTenantRole;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.spring.SpringUtils;
import cn.eyecool.system.domain.SysRoleMenu;
import cn.eyecool.system.domain.SysTenantRoleMenu;
import cn.eyecool.system.mapper.SysMenuMapper;
import cn.eyecool.system.mapper.SysRoleMapper;
import cn.eyecool.system.mapper.SysRoleMenuMapper;
import cn.eyecool.system.mapper.SysTenantMapper;
import cn.eyecool.system.mapper.SysTenantRoleMapper;
import cn.eyecool.system.mapper.SysTenantRoleMenuMapper;
import cn.eyecool.system.service.ISysTenantRoleService;

/**
 * 租户角色 业务层处理
 * 
 * @author admin
 */
@Service
public class SysTenantRoleServiceImpl implements ISysTenantRoleService {

    private static final transient Logger LOG = LoggerFactory.getLogger(SysTenantRoleServiceImpl.class);

    @Autowired
    private SysTenantRoleMapper tenantRoleMapper;
    @Autowired
    private SysTenantRoleMenuMapper tenantRoleMenuMapper;
    @Autowired
    private SysTenantMapper tenantMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    public List<SysTenantRole> selectTenantRoleList(SysTenantRole role) {
        return tenantRoleMapper.selectTenantRoleList(role);
    }

    /**
     * 根据租户ID查询权限
     * 
     * @param tenantId 租户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectTenantRolePermissionByTenantId(String tenantId) {
        List<SysTenantRole> perms = tenantRoleMapper.selectTenantRolePermissionByTenantId(tenantId);
        Set<String> permsSet = new HashSet<>();
        for (SysTenantRole perm : perms) {
            if (StringUtils.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysTenantRole> selectTenantRoleAll() {
        return SpringUtils.getAopProxy(this).selectTenantRoleList(new SysTenantRole());
    }

    /**
     * 根据租户ID获取角色选择框列表
     * 
     * @param tenantId 租户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Integer> selectTenantRoleListByTenantId(String tenantId) {
        return tenantRoleMapper.selectTenantRoleListByTenantId(tenantId);
    }

    /**
     * 通过角色ID查询角色
     * 
     * @param id 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysTenantRole selectTenantRoleById(String id) {
        return tenantRoleMapper.selectTenantRoleById(id);
    }

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkTenantRoleNameUnique(SysTenantRole role) {
        String roleId = StringUtils.isNotBlank(role.getId()) ? role.getId() : StringUtils.EMPTY;
        SysTenantRole info = tenantRoleMapper.checkTenantRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && !info.getId().equals(roleId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkTenantRoleKeyUnique(SysTenantRole role) {
        String roleId = StringUtils.isNotBlank(role.getId()) ? role.getId() : StringUtils.EMPTY;
        SysTenantRole info = tenantRoleMapper.checkTenantRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && !info.getId().equals(roleId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    @Override
    public void checkTenantRoleAllowed(SysTenantRole role) {
        if (StringUtils.isNotBlank(role.getId()) && role.isTrialRole()) {
            throw new CustomException(MessageUtils.message("systenant.role.service.prohibit.operate.tryout.role"));
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countTenantRoleByRoleId(String roleId) {
        return tenantMapper.countTenantRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTenantRole(SysTenantRole role) {
        // 新增角色信息
        role.setId(IdWorker.getNextStringId());
        role.setCreateTime(DateUtils.getNowDate());
        role.setCreateBy(SecurityUtils.getUsername());
        tenantRoleMapper.insertTenantRole(role);
        return insertTenantRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     * 
     * @param tenantRole 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTenantRole(SysTenantRole tenantRole) {
        // 修改角色信息
        tenantRole.setUpdateBy(SecurityUtils.getUsername());
        tenantRole.setUpdateTime(DateUtils.getNowDate());
        tenantRoleMapper.updateTenantRole(tenantRole);
        // 删除角色与菜单关联
        tenantRoleMenuMapper.deleteRoleMenuByRoleId(tenantRole.getId());
        int row = insertTenantRoleMenu(tenantRole);
        List<Long> tenantRoleMenuIds =
            sysMenuMapper.selectMenuIdListByTenantRoleId(tenantRole.getId(), tenantRole.isMenuCheckStrictly());
        LOG.info("Modify Tenant Role Menu Binding,tenantRoleId:[{}], tenantRoleKey:[{}]", tenantRole.getId(), tenantRole.getRoleKey());
        // 根据角色查询租户，修改改租户下的用户角色权限，保证用户角色权限在租户权限范围内(如果超限了，需要删除掉)
        CompletableFuture.runAsync(() -> {
            if (Boolean.FALSE.equals(tenantProperties.getEnabled())) {
                return;
            }
            TenantContextHolder.clear();
            SysTenant tenantCondition = new SysTenant();
            tenantCondition.setTenantRoleId(tenantRole.getId());
            List<SysTenant> tenantList = tenantMapper.selectSysTenantList(tenantCondition);
            if (CollectionUtils.isEmpty(tenantList)) {
                return;
            }
            tenantList.stream().forEach(tenant -> {
                String tenantId = tenant.getTenantId();
                SysRole roleCondition = new SysRole();
                roleCondition.setTenantId(tenantId);
                List<SysRole> roleList = sysRoleMapper.selectRoleList(roleCondition);
                if (CollectionUtils.isEmpty(roleList)) {
                    return;
                }
                roleList.stream().forEach(it -> {
                    List<Long> menuIds = sysMenuMapper.selectMenuListByRoleId(it.getRoleId(), false);
                    if (CollectionUtils.isEmpty(menuIds)) {
                        return;
                    }
                    int oldMenuCount = menuIds.size();
                    menuIds.retainAll(tenantRoleMenuIds);
                    int newMenuCount = menuIds.size();
                    if (oldMenuCount == newMenuCount) {
                        return;
                    }
                    List<SysRoleMenu> roleMenuList = menuIds.stream().map(menuId -> {
                        SysRoleMenu sysRoleMenu = new SysRoleMenu();
                        sysRoleMenu.setMenuId(menuId);
                        sysRoleMenu.setRoleId(it.getRoleId());
                        return sysRoleMenu;
                    }).collect(Collectors.toList());
                    sysRoleMenuMapper.deleteRoleMenuByRoleId(it.getRoleId());
                    sysRoleMenuMapper.batchRoleMenu(roleMenuList);
                    LOG.info("The menu binding relationship of tenant role [{}] is modified, and the menu binding relationship of role [{}] under tenant [{}] is updated synchronously", tenantRole.getId(), tenantId, it.getRoleId());
                });
            });
        });
        return row;
    }

    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateTenantRoleStatus(SysTenantRole role) {
        return tenantRoleMapper.updateTenantRole(role);
    }

    /**
     * 新增角色菜单信息
     * 
     * @param role 角色对象
     */
    public int insertTenantRoleMenu(SysTenantRole role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysTenantRoleMenu> list = new ArrayList<SysTenantRoleMenu>();
        for (Long menuId : role.getMenuIds()) {
            SysTenantRoleMenu rm = new SysTenantRoleMenu();
            rm.setRoleId(role.getId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            rows = tenantRoleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 
     * @param id 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTenantRoleById(String id) {
        SysTenantRole role = selectTenantRoleById(id);
        checkTenantRoleAllowed(role);
        if (countTenantRoleByRoleId(id) > 0) {
            throw new CustomException(MessageUtils.message("sysdict.service.assigned.not.delete", role.getRoleName()));
        }
        tenantRoleMenuMapper.deleteRoleMenuByRoleId(id);
        return tenantRoleMapper.deleteTenantRoleById(id);
    }

    /**
     * 批量删除角色信息
     * 
     * @param ids 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTenantRoleByIds(String[] ids) {
        for (String id : ids) {
            SysTenantRole role = selectTenantRoleById(id);
            checkTenantRoleAllowed(role);
            if (countTenantRoleByRoleId(id) > 0) {
                throw new CustomException(MessageUtils.message("sysdict.service.assigned.not.delete", role.getRoleName()));
            }
            tenantRoleMenuMapper.deleteRoleMenuByRoleId(id);
        }
        return tenantRoleMapper.deleteTenantRoleByIds(ids);
    }

}
