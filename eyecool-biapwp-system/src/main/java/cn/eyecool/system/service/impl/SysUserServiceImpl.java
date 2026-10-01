package cn.eyecool.system.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.eyecool.abis.callmicroservice.IMultiFeatureService;
import com.eyecool.abis.callmicroservice.MicroConstants.AlgType;
import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.google.common.collect.Lists;

import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.entity.SysRole;
import cn.eyecool.common.core.domain.entity.SysTenant;
import cn.eyecool.common.core.domain.entity.SysUser;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.match.MultiFusionFeatureService;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.system.constant.SystemConstants;
import cn.eyecool.system.domain.SysPost;
import cn.eyecool.system.domain.SysUserFace;
import cn.eyecool.system.domain.SysUserFinger;
import cn.eyecool.system.domain.SysUserIris;
import cn.eyecool.system.domain.SysUserIrisFace;
import cn.eyecool.system.domain.SysUserPost;
import cn.eyecool.system.domain.SysUserPutInfo;
import cn.eyecool.system.domain.SysUserRole;
import cn.eyecool.system.mapper.SysDeptMapper;
import cn.eyecool.system.mapper.SysPostMapper;
import cn.eyecool.system.mapper.SysRoleMapper;
import cn.eyecool.system.mapper.SysTenantMapper;
import cn.eyecool.system.mapper.SysUserFaceMapper;
import cn.eyecool.system.mapper.SysUserFingerMapper;
import cn.eyecool.system.mapper.SysUserIrisFaceMapper;
import cn.eyecool.system.mapper.SysUserIrisMapper;
import cn.eyecool.system.mapper.SysUserMapper;
import cn.eyecool.system.mapper.SysUserPostMapper;
import cn.eyecool.system.mapper.SysUserRoleMapper;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.system.service.ISysDictTypeService;
import cn.eyecool.system.service.ISysUserFaceRecogLogicService;
import cn.eyecool.system.service.ISysUserFingerRecogLogicService;
import cn.eyecool.system.service.ISysUserIrisRecogLogicService;
import cn.eyecool.system.service.ISysUserService;

/**
 * 用户 业务层处理
 * 
 * @author admin
 */
@Service
public class SysUserServiceImpl implements ISysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPostMapper postMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysUserPostMapper userPostMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ISysUserFingerRecogLogicService userFingerRecogLogicService;
    @Autowired
    private ISysUserIrisRecogLogicService userIrisRecogLogicService;
    @Autowired
    private ISysUserFaceRecogLogicService userFaceRecogLogicService;
    @Autowired
    private SysUserFaceMapper sysUserFaceMapper;
    @Autowired
    private SysUserFingerMapper sysUserFingerMapper;
    @Autowired
    private SysUserIrisMapper sysUserIrisMapper;
    @Autowired
    private SysUserIrisFaceMapper sysUserIrisFaceMapper;
    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private IMultiFeatureService multiFeatureService;
    @Autowired
    private MultiFusionFeatureService multiFusionFeatureService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private SysTenantMapper tenantMapper;

    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        StringBuilder idsStr = new StringBuilder();
        for (SysRole role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        StringBuilder idsStr = new StringBuilder();
        for (SysPost post : list) {
            idsStr.append(post.getPostName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 校验用户名称是否唯一
     * 
     * @param userName 用户名称
     * @return 结果
     */
    @Override
    public String checkUserNameUnique(String userName) {
        int count = userMapper.checkUserNameUnique(userName);
        if (count > 0) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkPhoneUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkEmailUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     * 
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user) {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new CustomException(MessageUtils.message("sysuser.service.prohibit.operate.super.user"));
        }
        if (StringUtils.isNotNull(user.getUserId()) && user.isTenantSuperUser()) {
            throw new CustomException(MessageUtils.message("sysuser.service.prohibit.operate.tenant.super.user"));
        }
    }

    /**
     * 新增保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user) {
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 修改保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
        // 同步修改所属修改租户手机号和邮箱
        SysUser existsUser = userMapper.selectUserById(userId);
        if (tenantProperties.getEnabled() && existsUser.isTenantSuperUser()) {
            SysTenant tenant = tenantMapper.selectSysTenantByTenantId(existsUser.getTenantId());
            tenant.setPhone(user.getPhonenumber());
            tenant.setEmail(user.getEmail());
            tenant.setUpdateTime(DateUtils.getNowDate());
            tenantMapper.updateSysTenant(tenant);
        }
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户头像
     * 
     * @param userName 用户名
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return userMapper.updateUserAvatar(userName, avatar) > 0;
    }

    /**
     * 重置用户密码
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 重置用户密码
     * 
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return userMapper.resetUserPwd(userName, password);
    }

    /**
     * 新增用户角色信息
     * 
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (StringUtils.isNotNull(roles)) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0) {
                userRoleMapper.batchUserRole(list);
            }
        }
    }

    /**
     * 新增用户岗位信息
     * 
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user) {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotNull(posts)) {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>();
            for (Long postId : posts) {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (list.size() > 0) {
                userPostMapper.batchUserPost(list);
            }
        }
    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            SysUser user = userMapper.selectUserById(userId);
            checkUserAllowed(user);
        }
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 导入用户数据
     * 
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(userList) || userList.isEmpty()) {
            throw new CustomException(MessageUtils.message("sysuser.service.import.userdata.empty"));
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String password = configService.selectConfigByKey("sys.user.initPassword");
        for (SysUser user : userList) {
            try {
                // 校验部门是否存在
                if (!StringUtils.isNull(user.getDeptId())) {
                    SysDept dept = deptMapper.selectDeptById(user.getDeptId());
                    if (null == dept) {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + MessageUtils.message("sysuser.service.import.dept.not.exists",user.getUserName()));
                        continue;
                    }
                }
                // 校验手机号是否存在
                if (StringUtils.isNotBlank(user.getPhonenumber())) {
                    SysUser u1 = userMapper.checkPhoneUnique(user.getPhonenumber());
                    if (null != u1 && StringUtils.isNotBlank(u1.getUserName())
                        && !u1.getUserName().equals(user.getUserName())) {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum +  MessageUtils.message("sysuser.service.import.phone.used",user.getUserName(),user.getPhonenumber()));
                        continue;
                    }
                }
                // 校验邮箱是否存在
                if (StringUtils.isNotBlank(user.getEmail())) {
                    SysUser u2 = userMapper.checkEmailUnique(user.getEmail());
                    if (null != u2 && StringUtils.isNotBlank(u2.getUserName())
                        && !u2.getUserName().equals(user.getUserName())) {
                        failureNum++;
                        failureMsg.append(
                            "<br/>" + failureNum + MessageUtils.message("sysuser.service.import.email.used",user.getUserName(),user.getEmail()));
                        continue;
                    }
                }
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u)) {
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    this.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum +  MessageUtils.message("sysuser.service.import.success",user.getUserName()));
                } else if (isUpdateSupport) {
                    user.setUpdateBy(operName);
                    this.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + MessageUtils.message("sysuser.service.update.success",user.getUserName()));
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + MessageUtils.message("sysuser.service.import.account.exists",user.getUserName()));
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum +MessageUtils.message("sysuser.service.import.failed",user.getUserName());
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, MessageUtils.message("channel.busi.service.import.person.failed.sumary",failureNum));
            throw new CustomException(failureMsg.toString());
        } else {
            successMsg.insert(0, MessageUtils.message("channel.busi.service.import.person.success.sumary",successNum));
        }
        return successMsg.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertOrUpdateSysUserBioInfo(SysUserPutInfo sysUserPutInfo) {
        // 1、查询人员是否存在(有效或者无效)
        Long userId = sysUserPutInfo.getUserId();
        SysUser sysUser = userMapper.selectUserById(userId);
        if (sysUser == null) {
            throw new CustomException(MessageUtils.message("sysuser.service.user.not.exists"));
        }
        sysUser.setUpdateTime(DateUtils.getNowDate());
        int result = userMapper.updateUser(sysUser);
        insertOrUpdateSysUserFacePutInfo(sysUserPutInfo, true);
        // 保存指纹信息,校验 传入的多个指纹是否是同一个手指头
        insertOrUpdateSysUserFingerPutInfo(sysUserPutInfo, true);
        // 保存虹膜图片
        insertOrUpdateSysUserIrisPutInfo(sysUserPutInfo, true);
        // 保存虹膜人脸多模态信息
        insertOrUpdateSysUserIrisFacePutInfo(sysUserPutInfo, true);
        return result;
    }

    /**
     * 保存人脸虹膜多模态信息
     * 
     * @param sysUserPutInfo
     * @param b
     * @return
     */
    private SysUserIrisFace insertOrUpdateSysUserIrisFacePutInfo(SysUserPutInfo sysUserPutInfo, boolean isUpdate) {
        SysUserPutInfo.SysUserIrisFacePutInfo irisFacePutInfo = sysUserPutInfo.getIrisFacePutInfo();
        if (null == irisFacePutInfo || (StringUtils.isBlank(irisFacePutInfo.getFaceImgBase64())
            && StringUtils.isBlank(irisFacePutInfo.getIrisImgBase64()))) {
            return null;
        }
        // 活体检测
        if (userFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                userFaceRecogLogicService.checkLive(irisFacePutInfo.getFaceImgBase64(), null);
            if (!checkLiveResponse.getResult()) {
                log.error("The multimodal face storage in vivo detection failed,userId:{}!", sysUserPutInfo.getUserId());
                throw new CustomException(MessageUtils.message("base.person.info.multimodal.face.checklive.failed"));
            }
        }
        SysUserIrisFace irisFace;
        try {
            irisFace = handleFeature(irisFacePutInfo, sysUserPutInfo.getUserId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(MessageUtils.message("sysuser.service.multimodal.save.error", e.getMessage()));
        }
        String stockFusionFeature = null;
        SysUserIrisFace sysUserIrisFace = getSysUserIrisFaceByUserId(sysUserPutInfo.getUserId());
        if (sysUserIrisFace != null) {
            stockFusionFeature = sysUserIrisFace.getFusionFeature();
        }
        if (isUpdate) {
            sysUserIrisFaceMapper.deleteSysUserIrisFaceByUserId(sysUserPutInfo.getUserId());
        }
        if (StringUtils.isNotBlank(stockFusionFeature)) {
            float score =
                multiFusionFeatureService.matchFusionFeatures(irisFace.getFusionFeature(), stockFusionFeature);
            double threshold = getFusionFeatureMatchOneThreshold();
            if (score < threshold) {
                throw new CustomException(MessageUtils.message("base.person.info.multimodal.match.failed"));
            }
        }
        sysUserIrisFaceMapper.insertSysUserIrisFace(irisFace);
        return irisFace;
    }

    /**
     * 新增多模态处理图片特征信息
     * 
     * @param userIrisFace
     * @return
     * @throws Exception
     */
    private SysUserIrisFace handleFeature(SysUserPutInfo.SysUserIrisFacePutInfo irisFacePutInfo, Long userId)
        throws Exception {
        SysUserIrisFace userIrisFace = new SysUserIrisFace();
        String faceImage = irisFacePutInfo.getFaceImgBase64();
        String irisImage = irisFacePutInfo.getIrisImgBase64();
        if (StringUtils.isNotEmpty(faceImage)) {
            // 质量检测
            double qualityScore = userFaceRecogLogicService.qualityDetect(faceImage, null, MessageUtils.message("person.face.image.not.clear"));
            userIrisFace.setFaceQuality(qualityScore);
        }
        CompletableFuture<String> faceFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isEmpty(faceImage)) {
                    return null;
                }
                return multiFeatureService.extractFeatureByImage(faceImage, AlgType.FACE);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<String> irisFeatureFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isNotEmpty(irisFacePutInfo.getIrisFeature())
                    || StringUtils.isEmpty(irisFacePutInfo.getIrisImgBase64())) {
                    return irisFacePutInfo.getIrisFeature();
                }
                return multiFeatureService.extractFeatureByImage(irisImage, AlgType.IRIS);
            } catch (Exception var3) {
                log.error("handleFeature error", var3);
                throw new CustomException(var3.getMessage());
            }
        });
        CompletableFuture<Void> allResult = CompletableFuture.allOf(faceFeatureFuture, irisFeatureFuture);
        allResult.join();
        String faceFeature = faceFeatureFuture.get();
        String irisFeature = irisFeatureFuture.get();
        String feature = multiFusionFeatureService.fusionFeature(faceFeature, irisFeature);
        // String feature = multiFeatureService.faceIrisMixFeatureByFeature(faceFeature, irisFeature);
        userIrisFace.setFaceFeature(faceFeature);
        userIrisFace.setIrisFeature(irisFeature);
        userIrisFace.setFusionFeature(feature);
        userIrisFace.setFaceFeatureMd5(Md5Utils.hash(userIrisFace.getFaceFeature()));
        userIrisFace.setIrisFeatureMd5(Md5Utils.hash(userIrisFace.getIrisFeature()));
        userIrisFace.setFusionFeatureMd5(Md5Utils.hash(userIrisFace.getFusionFeature()));
        userIrisFace.setId(IdWorker.getNextStringId());
        userIrisFace.setCreateTime(DateUtils.getNowDate());
        userIrisFace.setUserId(userId);
        String baseDir = SystemConstants.SYS_USER_BIO_IMAGE_DIR.MULTI_DIR;
        String facePath =
            userFaceRecogLogicService.uploadFaceImg(true, null, faceImage, baseDir + "face" + File.separator);
        String irisPath =
            userIrisRecogLogicService.uploadIrisImg(true, null, irisImage, baseDir + "iris" + File.separator);
        userIrisFace.setFaceImageUrl(facePath);
        userIrisFace.setIrisImageUrl(irisPath);
        return userIrisFace;
    }

    /**
     * 保存人脸
     * 
     * @param sysUserPutInfo
     * @param isUpdate
     * @return
     */
    private SysUserFace insertOrUpdateSysUserFacePutInfo(SysUserPutInfo sysUserPutInfo, boolean isUpdate) {
        SysUserPutInfo.SysUserFacePutInfo facePutInfo = sysUserPutInfo.getFacePutInfo();
        if (null == facePutInfo || StringUtils.isBlank(facePutInfo.getImageBase64())) {
            return null;
        }
        // 活体检测
        if (userFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse =
                userFaceRecogLogicService.checkLive(facePutInfo.getImageBase64(), null);
            if (!checkLiveResponse.getResult()) {
                log.error("The face warehousing liveness detection failed,uniqueId:{}!", sysUserPutInfo.getUserId());
                throw new CustomException(MessageUtils.message("base.person.face.checklive.failed"));
            }
        }
        String stockImgFeature = null;
        SysUserFace sysUserFace = getSysUserFaceByUserId(sysUserPutInfo.getUserId());
        if (sysUserFace != null) {
            stockImgFeature = sysUserFace.getFeature();
        }
        if (isUpdate) {
            sysUserFaceMapper.deleteSysUserFaceByUserId(sysUserPutInfo.getUserId());
        }
        SysUserFace srcFace = new SysUserFace();
        srcFace.setUserId(sysUserPutInfo.getUserId());
        SysUserFace destFace = userFaceRecogLogicService.execCheckAndUploadFace(sysUserPutInfo.getUserName(),
            facePutInfo.getImageBase64(), null, srcFace, stockImgFeature, false, isUpdate);
        destFace.setFeatureMd5(Md5Utils.hash(destFace.getFeature()));
        sysUserFaceMapper.insertSysUserFace(destFace);
        return destFace;
    }

    /**
     * 保存人员入库指纹信息,校验 传入的多个指纹是否是同一个手指头
     *
     * @param sysUserPutInfo
     * @param dataSource
     * @param isUpdate true:更新 false:新增
     * @param deletedPersonFeatureIds 需要删除的datamanager中的特征Id
     * @return
     */
    private List<SysUserFinger> insertOrUpdateSysUserFingerPutInfo(SysUserPutInfo sysUserPutInfo, boolean isUpdate) {
        List<SysUserPutInfo.SysUserFingerPutInfo> fingerPutInfoList = sysUserPutInfo.getFingerPutInfoList();
        if (CollectionUtils.isEmpty(fingerPutInfoList)) {
            return Collections.emptyList();
        }
        // 返回的手指列表
        List<SysUserFinger> destFingerList = Lists.newArrayList();
        // 进行重复校验特征值列表
        List<String> fingerFeatureList = Lists.newArrayList();
        List<SysDictData> type = dictTypeService.selectDictDataByType(DictConstants.BIO_FINGER_NO_DICT_TYPE);
        List<String> fingerNoList = type.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        Long userId = sysUserPutInfo.getUserId();
        List<SysUserFinger> fingerList = getSysUserFingerByUserId(userId);
        if (isUpdate && CollectionUtils.isNotEmpty(fingerList)) {
            sysUserFingerMapper.deleteSysUserFingerByUserId(userId);
        }
        fingerPutInfoList.forEach(fingerPutInfo -> {
            String stockImgFeature = null;
            String fingerNo = fingerPutInfo.getFingerNo();
            if (null != fingerPutInfo && StringUtils.isNotBlank(fingerPutInfo.getImageBase64())) {
                if (StringUtils.isBlank(fingerNo) || !fingerNoList.contains(fingerNo)) {
                    throw new CustomException(MessageUtils.message("base.person.info.finger.serial.need"));
                }
                if (isUpdate && CollectionUtils.isNotEmpty(fingerList)
                    && !DictConstants.UncertainFingerNo.uncertainFingerNoList.contains(fingerNo)) {
                    stockImgFeature = fingerList.stream().filter(it -> fingerNo.equals(it.getFingerNo()))
                        .map(SysUserFinger::getFeature).findFirst().orElse(null);
                }
                SysUserFinger srcFinger = new SysUserFinger();
                srcFinger.setUserId(userId);
                srcFinger.setFingerNo(fingerNo);
                // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
                SysUserFinger destFinger =
                    userFingerRecogLogicService.execCheckAndUploadFinger(sysUserPutInfo.getUserName(),
                        fingerPutInfo.getImageBase64(), null, srcFinger, stockImgFeature, false, isUpdate);
                destFinger.setFeatureMd5(Md5Utils.hash(destFinger.getFeature()));
                sysUserFingerMapper.insertSysUserFinger(destFinger);
                fingerFeatureList.add(destFinger.getFeature());
                destFingerList.add(destFinger);
            }
        });
        // 校验指纹是否是同一个手指头
        boolean hasRepeatFinger = userFingerRecogLogicService.checkHasRepeatFinger(fingerFeatureList, null);
        if (hasRepeatFinger) {
            throw new CustomException(MessageUtils.message("base.person.info.finger.duplicate"));
        }
        return destFingerList;
    }

    /**
     * 保存人员入库虹膜信息
     * 
     * @param sysUserPutInfo
     * @param isUpdate
     * @return
     */
    private SysUserIris insertOrUpdateSysUserIrisPutInfo(SysUserPutInfo sysUserPutInfo, boolean isUpdate) {
        SysUserPutInfo.SysUserIrisPutInfo irisPutInfo = sysUserPutInfo.getIrisPutInfo();
        if (null == irisPutInfo || StringUtils.isBlank(irisPutInfo.getImageBase64())) {
            return null;
        }
        String stockImgFeature = null;
        Long userId = sysUserPutInfo.getUserId();
        SysUserIris sysUserIris = getSysUserIrisByUserId(userId);
        if (sysUserIris != null) {
            stockImgFeature = sysUserIris.getFeature();
        }
        if (isUpdate) {
            sysUserIrisMapper.deleteSysUserIrisByUserId(userId);
        }
        SysUserIris srcIris = new SysUserIris();
        srcIris.setUserId(userId);
        srcIris.setFeature(irisPutInfo.getFeature());
        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        SysUserIris destIris = userIrisRecogLogicService.execCheckAndUploadIris(sysUserPutInfo.getUserName(),
            irisPutInfo.getImageBase64(), null, srcIris, stockImgFeature, false, isUpdate);
        destIris.setStatus(DictConstants.Status.ENABLE);
        destIris.setFeatureMd5(Md5Utils.hash(destIris.getFeature()));
        sysUserIrisMapper.insertSysUserIris(destIris);
        return destIris;
    }

    /**
     * 查询sysUser有效人脸信息
     *
     * @param userId
     * @return
     */
    private SysUserFace getSysUserFaceByUserId(Long userId) {
        // 查询人脸信息是否存在
        SysUserFace faceCondition = new SysUserFace();
        faceCondition.setUserId(userId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserFace> faceList = sysUserFaceMapper.selectSysUserFaceList(faceCondition);
        return CollectionUtils.isNotEmpty(faceList) ? faceList.get(0) : null;
    }

    /**
     * 查询有效指纹信息
     *
     * @param userId
     * @return
     */
    private List<SysUserFinger> getSysUserFingerByUserId(Long userId) {
        // 查询指纹信息是否存在
        SysUserFinger fingerCondition = new SysUserFinger();
        fingerCondition.setUserId(userId);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserFinger> fingerList = sysUserFingerMapper.selectSysUserFingerList(fingerCondition);
        return CollectionUtils.isEmpty(fingerList) ? Collections.emptyList() : fingerList;
    }

    /**
     * 查询有效人员虹膜
     *
     * @param userId
     * @return
     */
    private SysUserIris getSysUserIrisByUserId(Long userId) {
        // 查询虹膜是否存在
        SysUserIris irisCondition = new SysUserIris();
        irisCondition.setUserId(userId);
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserIris> irisList = sysUserIrisMapper.selectSysUserIrisList(irisCondition);
        return CollectionUtils.isNotEmpty(irisList) ? irisList.get(0) : null;
    }

    /**
     * 查询sysUser有效多模态信息
     *
     * @param userId
     * @return
     */
    private SysUserIrisFace getSysUserIrisFaceByUserId(Long userId) {
        SysUserIrisFace irisFaceCondition = new SysUserIrisFace();
        irisFaceCondition.setUserId(userId);
        irisFaceCondition.setStatus(DictConstants.Status.ENABLE);
        List<SysUserIrisFace> irisFaceList = sysUserIrisFaceMapper.selectSysUserIrisFaceList(irisFaceCondition);
        return CollectionUtils.isNotEmpty(irisFaceList) ? irisFaceList.get(0) : null;
    }

    /**
     * 获取1:1比对阈值参数
     *
     * @return
     */
    private double getFusionFeatureMatchOneThreshold() {
        String thresholdStr =
            configService.selectConfigByKey(SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY);
        if (StringUtils.isBlank(thresholdStr)) {
            throw new CustomException(MessageUtils.message("base.person.info.multimodal.face.iris.threshold.configure",SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY ));
        }
        try {
            return Double.valueOf(thresholdStr);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("base.person.info.multimodal.face.iris.threshold.number",SysConfigConstants.BASEDATA_FACE_IRIS_FUSION_COMPARE_THRESHOLD_KEY ));
        }
    }

}
