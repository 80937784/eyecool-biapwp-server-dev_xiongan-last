package cn.eyecool.basedata.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonFingerPutInfo;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.domain.BasePersonIrisFace;
import cn.eyecool.basedata.domain.BasePersonIrisFacePutInfo;
import cn.eyecool.basedata.domain.BasePersonIrisPutInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.event.EventCallback;
import cn.eyecool.basedata.event.ListChannelInfoEventPublishService;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.basedata.service.IBasePersonFingerService;
import cn.eyecool.basedata.service.IBasePersonInfoService;
import cn.eyecool.basedata.service.IBasePersonIrisFaceService;
import cn.eyecool.basedata.service.IBasePersonIrisService;
import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.HttpStatus;
import cn.eyecool.common.context.LoginUserContextHolder;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.model.LoginUser;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.enums.PersonTypeEnum;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.system.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员基础信息Controller
 * 
 * @author mawj
 * @date 2021-01-27
 */
@RestController
@RequestMapping("/basedata/person")
@Slf4j
public class BasePersonInfoController extends BaseController {

    /** 访客部门名称：用于判断是否为访客部门 */
    private static final String VISITOR_DEPT_NAME = "访客";
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private IBasePersonInfoService basePersonInfoService;
    @Autowired
    private IBasePersonFaceService basePersonFaceService;
    @Autowired
    private IBasePersonFingerService basePersonFingerService;
    @Autowired
    private IBasePersonIrisService basePersonIrisService;
    @Autowired
    private IBasePersonIrisFaceService basePersonIrisFaceService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ListChannelInfoEventPublishService listChannelInfoEventPublishService;

    /**
     * 查询人员数量
     */
    @GetMapping("/count")
    public AjaxResult count(BasePersonInfo basePersonInfo) {
        basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        applyVisitorDeptFilter(basePersonInfo);
        int count = basePersonInfoService.selectBasePersonCount(basePersonInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", count);
        return AjaxResult.success(result);
    }

    /**
     * 查询人员基础信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:list')")
    @GetMapping("/listByUidOrName")
    public TableDataInfo listByUidOrName(String name) {
        startPage();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setName(name);
        basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        List<BasePersonInfo> list = basePersonInfoService.selectByUidOrName(basePersonInfo);
        return getDataTable(list);
    }

    /**
     * 查询人员基础信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:list')")
    @GetMapping("/list")
    public TableDataInfo list(BasePersonInfo basePersonInfo) {
        startPage();
        basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        applyVisitorDeptFilter(basePersonInfo);
        List<BasePersonInfo> list = basePersonInfoService.selectBasePersonInfoList(basePersonInfo);
        list.stream().forEach(it -> {
            Long deptId = it.getDeptId();
            if (null != deptId) {
                SysDept dept = deptService.selectDeptById(deptId);
                it.setDeptCode(null == dept ? null : dept.getDeptCode());
                it.setDeptName(null == dept ? null : dept.getDeptName());
            }
            boolean hasFace = basePersonFaceService.checkPersonHasFace(it.getId());
            boolean hasFinger = basePersonFingerService.checkPersonHasFinger(it.getId());
            boolean hasIris = basePersonIrisService.checkPersonHasIris(it.getId());
            boolean hasFaceIris = basePersonIrisFaceService.checkPersonHasFaceIris(it.getId());
            it.setHasFace(hasFace);
            it.setHasFinger(hasFinger);
            it.setHasIris(hasIris);
            it.setHasFvein(false);
            it.setHasFaceIris(hasFaceIris);
        });
        return getDataTable(list);
    }

    /**
     * 导出人员基础信息列表
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:export')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BasePersonInfo basePersonInfo) {
        String taskId = "Export-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(StringUtils.EMPTY), 12, TimeUnit.HOURS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            LoginUserContextHolder.setLoginUser(loginUser);
            basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
            applyVisitorDeptFilter(basePersonInfo);
            List<BasePersonInfo> list = basePersonInfoService.selectBasePersonInfoList(basePersonInfo);
            list = list.parallelStream().map(it -> {
                TenantContextHolder.setTenantId(tenantId);
                Long deptId = it.getDeptId();
                if (null != deptId) {
                    SysDept dept = deptService.selectDeptById(deptId);
                    it.setDeptCode(null == dept ? null : dept.getDeptCode());
                    it.setDeptName(null == dept ? null : dept.getDeptName());
                }
                return it;
            }).collect(Collectors.toList());
            ExcelUtil<BasePersonInfo> util = new ExcelUtil<BasePersonInfo>(BasePersonInfo.class);
            AjaxResult result = util.exportExcel(list, "person");
            String fileName = (String)result.get(AjaxResult.MSG_TAG);
            redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                AjaxResult.success(fileName), 12, TimeUnit.HOURS);
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 获取人员基础信息详细信息
     * 
     * @throws InterruptedException
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws InterruptedException {
        BasePersonInfo personInfo = basePersonInfoService.selectBasePersonInfoById(id);
        BasePersonPutInfo putInfo = new BasePersonPutInfo();
        BeanUtils.copyBeanProp(putInfo, personInfo);
        // 查询人脸图片
        BasePersonFace faceConditon = new BasePersonFace();
        faceConditon.setPersonId(id);
        faceConditon.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(faceConditon);
        if (CollectionUtils.isNotEmpty(faceList)) {
            BasePersonFacePutInfo face = faceList.stream().map(it -> {
                boolean encrypted = DictConstants.Encrypted.ENABLE.equals(it.getEncrypted());
                BasePersonFacePutInfo facePutInfo = new BasePersonFacePutInfo();
                facePutInfo.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
                String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                if (StringUtils.isNotBlank(stringBase64) && encrypted) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                }
                facePutInfo.setImageBase64(stringBase64);
                return facePutInfo;
            }).findFirst().orElse(null);
            putInfo.setFacePutInfo(face);
        }
        // 查询指纹照片
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setPersonId(id);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFinger> fingerList = basePersonFingerService.selectBasePersonFingerList(fingerCondition);
        if (CollectionUtils.isNotEmpty(fingerList)) {
            List<BasePersonFingerPutInfo> list =
                fingerList.stream().filter(it -> !Arrays.asList("A", "B").contains(it.getFingerNo())).map(it -> {
                    boolean encrypted = DictConstants.Encrypted.ENABLE.equals(it.getEncrypted());
                    BasePersonFingerPutInfo fingerPutInfo = new BasePersonFingerPutInfo();
                    fingerPutInfo
                        .setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
                    fingerPutInfo.setFingerNo(it.getFingerNo());
                    String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (StringUtils.isNotBlank(stringBase64)
                        && DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                        stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                    }
                    fingerPutInfo.setImageBase64(stringBase64);
                    return fingerPutInfo;
                }).collect(Collectors.toList());
            putInfo.setFingerPutInfoList(list);
        }
        // 查询虹膜照片
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setPersonId(id);
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisService.selectBasePersonIrisList(irisCondition);
        if (CollectionUtils.isNotEmpty(irisList)) {
            BasePersonIrisPutInfo iris = irisList.stream().map(it -> {
                boolean encrypted = DictConstants.Encrypted.ENABLE.equals(it.getEncrypted());
                BasePersonIrisPutInfo irisPutInfo = new BasePersonIrisPutInfo();
                irisPutInfo.setEncrypted(encrypted ? DictConstants.Encrypted.ENABLE : DictConstants.Encrypted.DISABLE);
                String stringBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                if (StringUtils.isNotBlank(stringBase64) && DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                    stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                }
                irisPutInfo.setImageBase64(stringBase64);
                return irisPutInfo;
            }).findFirst().orElse(null);
            putInfo.setIrisPutInfo(iris);
        }
        // 查询多模态图片
        BasePersonIrisFace irisFaceCondition = new BasePersonIrisFace();
        irisFaceCondition.setPersonId(id);
        irisFaceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIrisFace> irisFaceList =
            basePersonIrisFaceService.selectBasePersonIrisFaceList(irisFaceCondition);
        if (CollectionUtils.isNotEmpty(irisFaceList)) {
            BasePersonIrisFacePutInfo irisFace = irisFaceList.stream().map(it -> {
                BasePersonIrisFacePutInfo irisFacePutInfo = new BasePersonIrisFacePutInfo();
                String faceImgBase64 = PlatformFileUtils.getImageBase64(it.getFaceImageUrl());
                if (StringUtils.isNotBlank(faceImgBase64) && DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                    faceImgBase64 = PlatformCryptUtils.decryptImageBase64(faceImgBase64);
                }
                irisFacePutInfo.setFaceImgBase64(faceImgBase64);
                String irisImgBase64 = PlatformFileUtils.getImageBase64(it.getIrisImageUrl());
                if (StringUtils.isNotBlank(irisImgBase64) && DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                    irisImgBase64 = PlatformCryptUtils.decryptImageBase64(irisImgBase64);
                }
                irisFacePutInfo.setIrisImgBase64(irisImgBase64);
                return irisFacePutInfo;
            }).findFirst().orElse(null);
            putInfo.setIrisFacePutInfo(irisFace);
        }
        String channels = getPersonChannelInfo(personInfo.getUniqueId());
        putInfo.setChannels(channels);
        return AjaxResult.success(putInfo);
    }

    /**
     * 查询人员所属场景及子场景信息
     * 
     * @param uniqueId
     * @throws InterruptedException
     */
    private String getPersonChannelInfo(String uniqueId) throws InterruptedException {
        AjaxResult result = new AjaxResult();
        listChannelInfoEventPublishService.publish(uniqueId, null, true, new EventCallback() {
            @Override
            public void onSuccess(Object data) {
                log.debug("list channel info event process success, return data is [{}]", data.toString());
                result.put(AjaxResult.CODE_TAG, HttpStatus.SUCCESS);
                result.put(AjaxResult.DATA_TAG, data);
            }

            @Override
            public void onError(String errmsg) {
                log.error("list channel info event process error, errmsg is [{}]", errmsg);
                result.put(AjaxResult.CODE_TAG, HttpStatus.ERROR);
                result.put(AjaxResult.MSG_TAG, errmsg);
            }
        });
        int count = 0;
        while (null == result.get(AjaxResult.CODE_TAG)) {
            count++;
            Thread.sleep(200);
            if (count > 100) {
                result.put(AjaxResult.CODE_TAG, HttpStatus.ERROR);
                result.put(AjaxResult.MSG_TAG, MessageUtils.message("ajax.result.operator.failed"));
                break;
            }
        }
        if (HttpStatus.SUCCESS == (Integer)result.get(AjaxResult.CODE_TAG)) {
            List<JSONObject> list = (List<JSONObject>)result.get(AjaxResult.DATA_TAG);
            return list.stream().map(jsonObj -> {
                String channelCode = jsonObj.getString("channelCode");
                JSONArray subArray = jsonObj.getJSONArray("subList");
                String subCodes = subArray.toJavaList(JSONObject.class).stream().map(item -> {
                    return item.getString("subTreasuryCode");
                }).collect(Collectors.joining(","));
                return channelCode + "," + subCodes;
            }).collect(Collectors.joining(","));
        }
        throw new CustomException((String)result.get(AjaxResult.MSG_TAG));
    }

    /**
     * 新增人员基础信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:add')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BasePersonPutInfo basePersonInfo) {
        return toAjax(basePersonInfoService.insertBasePersonInfo(basePersonInfo));
    }

    /**
     * 修改人员基础信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:edit')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BasePersonPutInfo basePersonInfo) {
        return toAjax(basePersonInfoService.updateBasePersonInfo(basePersonInfo));
    }

    /**
     * 删除人员基础信息
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:remove')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(basePersonInfoService.deleteBasePersonInfoByIds(ids));
    }

    /**
     * 导入模板下载
     * 
     * @return
     */
    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<BasePersonInfo> util = new ExcelUtil<BasePersonInfo>(BasePersonInfo.class);
        return util.importTemplateExcel(MessageUtils.message("base.person.basic.information.data"));
    }

    /**
     * 保存批量导入
     * 
     * @throws Exception
     * @throws IOException
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:import')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult saveImportData(@RequestParam(value = "excelFile") MultipartFile excelFile, Boolean updateSupport)
        throws IOException, Exception {
        String originalFilename = excelFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            log.error("The upload file name is null.");
            throw new CustomException(MessageUtils.message("base.person.basic.upload.file.name.null"));
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!"xls".equalsIgnoreCase(suffix) && !"xlsx".equalsIgnoreCase(suffix)) {
            throw new CustomException(MessageUtils.message("base.person.choose.excel"));
        }
        String taskId = "ImportPersonInfo-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        InputStream inputStream = excelFile.getInputStream();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);

        final SecurityContext context = SecurityContextHolder.getContext();
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            SecurityContextHolder.setContext(context);
            try {
                String message = basePersonInfoService.saveImportData(inputStream, updateSupport);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.success(message), 7, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 同步人员信息到Datamanager
     * 
     * @param basePersonInfo
     * @return
     */
    @PreAuthorize("@ss.hasPermi('basedata:person:syncdata')")
    @Log(title = "base.person.basic.information", businessType = BusinessType.OTHER)
    @PostMapping("/syncdata")
    public AjaxResult syncdata(@RequestBody(required = false) BasePersonInfo basePersonInfo) {
        // basePersonInfo.setPersonType(PersonTypeEnum.USER.value());
        String taskId = "SyncPersonData-" + IdWorker.getNextStringId();
        String tenantId = TenantContextHolder.getTenantId();
        redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
            AjaxResult.success(MessageUtils.message("base.person.task.in.progress")), 7, TimeUnit.DAYS);
        CompletableFuture.runAsync(() -> {
            TenantContextHolder.setTenantId(tenantId);
            try {
                AjaxResult ajaxResult = basePersonInfoService.syncdata(basePersonInfo);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId, ajaxResult, 7,
                    TimeUnit.DAYS);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                redisCache.setCacheObject(Constants.ASYNC_TASK_RESULT_REDIS_KEY_PREFIX + taskId,
                    AjaxResult.error(e.getMessage()), 7, TimeUnit.DAYS);
            }
        });
        return AjaxResult.success(taskId);
    }

    /**
     * 查询人员基础信息列表
     * 
     * @throws InterruptedException
     */
    @GetMapping("/listChannel")
    public AjaxResult listChannel(String channelCode, Boolean cascade) throws InterruptedException {
        AjaxResult result = new AjaxResult();
        listChannelInfoEventPublishService.publish(null, channelCode, cascade, new EventCallback() {
            @Override
            public void onSuccess(Object data) {
                log.debug("list channel info event process success, return data is [{}]", data.toString());
                result.put(AjaxResult.CODE_TAG, HttpStatus.SUCCESS);
                result.put(AjaxResult.DATA_TAG, data);
            }

            @Override
            public void onError(String errmsg) {
                log.error("list channel info event process error, errmsg is [{}]", errmsg);
                result.put(AjaxResult.CODE_TAG, HttpStatus.ERROR);
                result.put(AjaxResult.MSG_TAG, errmsg);
            }
        });
        int count = 0;
        while (null == result.get(AjaxResult.CODE_TAG)) {
            count++;
            Thread.sleep(200);
            if (count > 100) {
                result.put(AjaxResult.CODE_TAG, HttpStatus.ERROR);
                result.put(AjaxResult.MSG_TAG, MessageUtils.message("ajax.result.operator.failed"));
                break;
            }
        }
        return result;
    }

    /**
     * 启用/停用 人员
     * @param ids 人员id，多个用英文逗号分割
     * @param type 0 启用 ，1 停用
     */

    @Log(title = "base.person.basic.information", businessType = BusinessType.OTHER)
    @GetMapping("/isStopAndEnable/{ids}")
    public AjaxResult isStopAndEnable(@PathVariable("ids") String ids, @RequestParam(name = "type") String type) {
        int stopAndEnable = basePersonInfoService.isStopAndEnable(ids, type);
        return toAjax(stopAndEnable);
    }
    /**
     * 处理"访客"部门过滤规则：
     * 默认排除部门名称为"访客"的部门(及其子部门)下的人员；
     * 仅当用户手动选中了"访客"部门时才显示访客部门人员。
     *
     * @param basePersonInfo 查询参数
     */
    private void applyVisitorDeptFilter(BasePersonInfo basePersonInfo) {
        boolean selectVisitorDept = false;
        Long deptId = basePersonInfo.getDeptId();
        if (deptId != null && deptId != 0) {
            SysDept dept = deptService.selectDeptById(deptId);
            if (dept != null && VISITOR_DEPT_NAME.equals(dept.getDeptName())) {
                selectVisitorDept = true;
            }
        }
        basePersonInfo.setExcludeVisitorDept(!selectVisitorDept);
    }
}
