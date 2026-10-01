package cn.eyecool.web.controller.system;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.system.service.ISysDeptService;

/**
 * 部门信息
 * 
 * @author admin
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {
    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return AjaxResult.success(depts);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        Iterator<SysDept> it = depts.iterator();
        while (it.hasNext()) {
            SysDept d = it.next();
            if (d.getDeptId().intValue() == deptId
                || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + "")) {
                it.remove();
            }
        }
        return AjaxResult.success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId) {
        return AjaxResult.success(deptService.selectDeptById(deptId));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return AjaxResult.success(deptService.buildDeptTreeSelect(depts));
    }

    /**
     * 加载对应角色部门列表树
     */
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public AjaxResult roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.buildDeptTreeSelect(depts));
        return ajax;
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "department.management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDept dept) {
        // 查询父节点是否是默认部门
        Long parentId = dept.getParentId();
        SysDept parentDept = deptService.selectDeptById(parentId);
        if (null != parentDept && UserConstants.DEFAULT_DEPT_CODE.equals(parentDept.getDeptCode())) {
            String msg = MessageUtils.message("parent.dept.not.default.dept");
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptCodeUnique(dept))) {
            String msg = MessageUtils.message("add.fail.code.exist", dept.getDeptName());
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            String msg = MessageUtils.message("add.fail.name.exist", dept.getDeptName());
            return AjaxResult.error(msg);
        }
        dept.setCreateBy(SecurityUtils.getUsername());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "department.management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept) {
        SysDept parentDept = deptService.selectDeptById(dept.getParentId());
        if (null != parentDept && UserConstants.DEFAULT_DEPT_CODE.equals(parentDept.getDeptCode())) {
            String msg = MessageUtils.message("parent.dept.not.default.dept");
            return AjaxResult.error(msg);
        }
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptCodeUnique(dept))) {
            String msg = MessageUtils.message("update.fail.code.exist", dept.getDeptName());
            return AjaxResult.error(msg);
        } else if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            String msg = MessageUtils.message("update.fail.name.exist", dept.getDeptName());
            return AjaxResult.error(msg);
        } else if (dept.getParentId().equals(dept.getDeptId())) {
            String msg = MessageUtils.message("update.fail.parent.not.oneself", dept.getDeptName());
            return AjaxResult.error(msg);
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())
            && deptService.selectNormalChildrenDeptById(dept.getDeptId()) > 0) {
            String msg = MessageUtils.message("dept.contains.used.sub");
            return AjaxResult.error(msg);
        }
        dept.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "department.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId) {
        SysDept parentDept = deptService.selectDeptById(deptId);
        if (null != parentDept && UserConstants.DEFAULT_DEPT_CODE.equals(parentDept.getDeptCode())) {
            String msg = MessageUtils.message("not.allow.delete.default.dept");
            return AjaxResult.error(msg);
        }
        if (deptService.hasChildByDeptId(deptId)) {
            String msg = MessageUtils.message("not.allow.delete.has.children");
            return AjaxResult.error(msg);
        }
        if (deptService.checkDeptExistUser(deptId)) {
            String msg = MessageUtils.message("not.allow.delete.has.user");
            return AjaxResult.error(msg);
        }
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
