package cn.eyecool.web.controller.system;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysRole;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.framework.web.service.TokenService;
import cn.eyecool.system.domain.SysUserFace;
import cn.eyecool.system.domain.SysUserFinger;
import cn.eyecool.system.domain.SysUserIris;
import cn.eyecool.system.domain.SysUserIrisFace;
import cn.eyecool.system.domain.SysUserPutInfo;
import cn.eyecool.system.domain.SysUserPutInfo.SysUserFacePutInfo;
import cn.eyecool.system.domain.SysUserPutInfo.SysUserFingerPutInfo;
import cn.eyecool.system.domain.SysUserPutInfo.SysUserIrisFacePutInfo;
import cn.eyecool.system.domain.SysUserPutInfo.SysUserIrisPutInfo;
import cn.eyecool.system.service.ISysPostService;
import cn.eyecool.system.service.ISysRoleService;
import cn.eyecool.system.service.ISysUserFaceService;
import cn.eyecool.system.service.ISysUserFingerService;
import cn.eyecool.system.service.ISysUserIrisFaceService;
import cn.eyecool.system.service.ISysUserIrisService;
import cn.eyecool.system.service.ISysUserService;

/**
 * 用户信息
 * 
 * @author admin
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserFaceService sysUserFaceService;

    @Autowired
    private ISysUserFingerService sysUserFingerService;

    @Autowired
    private ISysUserIrisService sysUserIrisService;

    @Autowired
    private ISysUserIrisFaceService sysUserIrisFaceService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "user.management", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @GetMapping("/export")
    public AjaxResult export(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        String msg = MessageUtils.message("user.data");
        return util.exportExcel(list, msg);
    }

    @Log(title = "user.management", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        String operName = loginUser.getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return AjaxResult.success(message);
    }

    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        String msg = MessageUtils.message("user.data");
        return util.importTemplateExcel(msg);
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles",
            SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            SysUserPutInfo putInfo = new SysUserPutInfo();
            BeanUtils.copyBeanProp(putInfo, sysUser);
            SysUserFace sysUserFace = new SysUserFace();
            sysUserFace.setUserId(userId);
            sysUserFace.setStatus(DictConstants.Status.ENABLE);
            List<SysUserFace> sysUserFaces = sysUserFaceService.selectSysUserFaceList(sysUserFace);
            if (CollectionUtils.isNotEmpty(sysUserFaces)) {
                SysUserFacePutInfo face = sysUserFaces.stream().map(it -> {
                    SysUserFacePutInfo facePutInfo = new SysUserFacePutInfo();
                    String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (StringUtils.isNotBlank(stringBase64)) {
                        stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    }
                    facePutInfo.setImageBase64(stringBase64);
                    facePutInfo.setId(it.getId());
                    return facePutInfo;
                }).findFirst().orElse(null);
                putInfo.setFacePutInfo(face);
            } else {
                putInfo.setFacePutInfo(new SysUserFacePutInfo());
            }
            SysUserFinger sysUserFinger = new SysUserFinger();
            sysUserFinger.setUserId(userId);
            List<SysUserFinger> syUserFingers = sysUserFingerService.selectSysUserFingerList(sysUserFinger);
            if (CollectionUtils.isNotEmpty(syUserFingers)) {
                List<SysUserFingerPutInfo> fingerPutInfos = syUserFingers.stream().map(it -> {
                    SysUserFingerPutInfo fingerPutInfo = new SysUserFingerPutInfo();
                    String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (StringUtils.isNotBlank(stringBase64)) {
                        stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    }
                    fingerPutInfo.setFingerNo(it.getFingerNo());
                    fingerPutInfo.setImageBase64(stringBase64);
                    fingerPutInfo.setId(it.getId());
                    return fingerPutInfo;
                }).collect(Collectors.toList());
                putInfo.setFingerPutInfoList(fingerPutInfos);
            } else {
                putInfo.setFingerPutInfoList(Collections.emptyList());
            }
            SysUserIris sysUserIris = new SysUserIris();
            sysUserIris.setUserId(userId);
            List<SysUserIris> sysUserIrises = sysUserIrisService.selectSysUserIrisList(sysUserIris);
            if (CollectionUtils.isNotEmpty(sysUserIrises)) {
                SysUserIrisPutInfo userIrisPutInfo = sysUserIrises.stream().map(it -> {
                    SysUserIrisPutInfo irisPutInfo = new SysUserIrisPutInfo();
                    String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (StringUtils.isNotBlank(stringBase64)) {
                        stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    }
                    irisPutInfo.setImageBase64(stringBase64);
                    irisPutInfo.setFeature(it.getFeature());
                    irisPutInfo.setId(it.getId());
                    return irisPutInfo;
                }).findFirst().orElse(null);
                putInfo.setIrisPutInfo(userIrisPutInfo);
            } else {
                putInfo.setIrisPutInfo(new SysUserIrisPutInfo());
            }
            SysUserIrisFace sysUserIrisFace = new SysUserIrisFace();
            sysUserIrisFace.setUserId(userId);
            List<SysUserIrisFace> sysUserIrisFaces = sysUserIrisFaceService.selectSysUserIrisFaceList(sysUserIrisFace);
            if (CollectionUtils.isNotEmpty(sysUserIrisFaces)) {
                SysUserIrisFacePutInfo userIrisFacePutInfo = sysUserIrisFaces.stream().map(it -> {
                    SysUserIrisFacePutInfo irisFacePutInfo = new SysUserIrisFacePutInfo();
                    String faceImgBase64 = PlatformFileUtils.getImageBase64(it.getFaceImageUrl());
                    if (StringUtils.isNotBlank(faceImgBase64)) {
                        faceImgBase64 = PlatformCryptUtils.decryptImageBase64(faceImgBase64);
                    }
                    String irisImgBase64 = PlatformFileUtils.getImageBase64(it.getIrisImageUrl());
                    if (StringUtils.isNotBlank(irisImgBase64)) {
                        irisImgBase64 = PlatformCryptUtils.decryptImageBase64(irisImgBase64);
                    }
                    irisFacePutInfo.setFaceImgBase64(faceImgBase64);
                    irisFacePutInfo.setIrisImgBase64(irisImgBase64);
                    irisFacePutInfo.setIrisFeature(it.getIrisFeature());
                    irisFacePutInfo.setId(it.getId());
                    return irisFacePutInfo;
                }).findFirst().orElse(new SysUserIrisFacePutInfo());
                putInfo.setIrisFacePutInfo(userIrisFacePutInfo);
            } else {
                putInfo.setIrisFacePutInfo(new SysUserIrisFacePutInfo());
            }
            ajax.put(AjaxResult.DATA_TAG, putInfo);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", roleService.selectRoleListByUserId(userId));
        }
        return ajax;
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "user.management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            String msg = MessageUtils.message("user.add.fail.account.exist", user.getUserName());
            return AjaxResult.error(msg);
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            String msg = MessageUtils.message("user.add.fail.phone.exist", user.getUserName());
            return AjaxResult.error(msg);
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            String msg = MessageUtils.message("user.add.fail.email.exist", user.getUserName());
            return AjaxResult.error(msg);
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "user.management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        if (UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            String msg = MessageUtils.message("user.update.fail.phone.exist", user.getUserName());
            return AjaxResult.error(msg);
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            String msg = MessageUtils.message("user.update.fail.email.exist", user.getUserName());
            return AjaxResult.error(msg);
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "user.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "user.management", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "user.management", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 新增生物信息
     * 
     * @param sysUserPutInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PostMapping("/bioInfo/add")
    @ResponseBody
    public AjaxResult insertBioInfo(@RequestBody SysUserPutInfo sysUserPutInfo) {
        sysUserPutInfo.setUpdateBy(SecurityUtils.getUsername());
        return AjaxResult.success(userService.insertOrUpdateSysUserBioInfo(sysUserPutInfo));
    }

    /**
     * 删除用户face
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "user.face.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/face/{id}")
    public AjaxResult removeFace(@PathVariable String id) {
        return toAjax(sysUserFaceService.deleteSysUserFaceById(id));
    }

    /**
     * 删除用户finger
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "user.finger.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/finger/{id}")
    public AjaxResult removeFinger(@PathVariable String id) {
        return toAjax(sysUserFingerService.deleteSysUserFingerById(id));
    }

    /**
     * 删除用户iris
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "user.iris.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/iris/{id}")
    public AjaxResult removeIris(@PathVariable String id) {
        return toAjax(sysUserIrisService.deleteSysUserIrisById(id));
    }

    /**
     * 删除用户人脸虹膜多模态
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "user.face.iris.management", businessType = BusinessType.DELETE)
    @DeleteMapping("/irisface/{id}")
    public AjaxResult removeIrisFace(@PathVariable String id) {
        return toAjax(sysUserIrisFaceService.deleteSysUserIrisFaceById(id));
    }
}
