package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eyecool.abis.callmicroservice.common.CheckLiveResponse;
import com.eyecool.abis.callmicroservice.common.FaceExtractResult;
import com.eyecool.abis.callmicroservice.common.FaceSearchResult;
import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.constant.OperateLock;
import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonPutInfo;
import cn.eyecool.basedata.domain.FaceRegister;
import cn.eyecool.basedata.enums.MultiRegistOptionTypeEnum;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.manager.IPersonFaceRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.service.IBasePersonFaceService;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileModel;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.file.ZipEntryFileModel;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.common.utils.sql.SqlUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 人脸图像信息Service业务层处理
 *
 * @author mawj
 * @date 2021-01-27
 */
@Service
@Slf4j
public class BasePersonFaceServiceImpl implements IBasePersonFaceService {

    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IPersonFaceRecogLogicService personFaceRecogLogicService;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    /**
     * 自己注入自己实现同一个类内部方法调用事务起作用，spring框架解决了循环注入问题，但注入自己这种方式要慎用
     */
    @Autowired
    private IBasePersonFaceService basePersonFaceService;

    /**
     * 查询人脸图像信息
     *
     * @param id 人脸图像信息ID
     * @return 人脸图像信息
     */
    @Override
    public BasePersonFace selectBasePersonFaceById(String id) {
        return basePersonFaceMapper.selectBasePersonFaceById(id);
    }

    /**
     * 查询人脸图像信息列表
     *
     * @param basePersonFace 人脸图像信息
     * @return 人脸图像信息
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonFace> selectBasePersonFaceList(BasePersonFace basePersonFace) {
        return basePersonFaceMapper.selectBasePersonFaceList(basePersonFace);
    }

    /**
     * 新增人脸图像信息
     *
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBasePersonFace(BasePersonFace basePersonFace) {
        // 查询人员信息是否存在,获取personId
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonFace.getUniqueId());
        if (null == basePersonInfo) {
            throw new CustomException(MessageUtils.message("base.person.face.user.not.exists"));
        }
        String personId = basePersonInfo.getId();
        basePersonFace.setPersonId(personId);
        int result = handlePersonFace(basePersonFace, basePersonInfo);
        // 发布人员信息改变事件
        personChangeEventPublishService.personFaceChangePublish(personId, basePersonInfo.getUniqueId(), null);
        return result;
    }

    /**
     * 添加人脸信息通用方法
     *
     * @param basePersonFace
     * @param basePersonInfo
     * @return
     */
    private int handlePersonFace(BasePersonFace basePersonFace, BasePersonInfo basePersonInfo) {

        // 查询人脸图片信息是否存在
        if (null != getEnabledPersonFaceByPersonId(basePersonInfo.getId())) {
            throw new CustomException(MessageUtils.message("base.person.face.image.exists"));
        }
        String faceImgBase64 = basePersonFace.getImgBase64();
        // 活体检测
        if (personFaceRecogLogicService.getFaceAddIsCheckLive()) {
            CheckLiveResponse checkLiveResponse = personFaceRecogLogicService.checkLive(faceImgBase64, null);
            if (!checkLiveResponse.getResult()) {
                log.error("Face warehousing live detection failed,uniqueId:[{}]!", basePersonInfo.getUniqueId());
                throw new CustomException(MessageUtils.message("base.person.face.checklive.failed"));
            }
        }

        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        BasePersonFace destFace =
            personFaceRecogLogicService.execCheckAndUploadFace(faceImgBase64, null, basePersonFace, null, false, false);

        // 入库是否进行1-N校验
        boolean faceAddIsValidateN = personFaceRecogLogicService.getFaceAddIsValidateN();
        if (faceAddIsValidateN) {
            List<FaceSearchResult> searchNResult =
                personFaceRecogLogicService.faceSearchN(destFace.getFeature(), StringUtils.EMPTY, 1, null);
            if (CollectionUtils.isNotEmpty(searchNResult)
                && !searchNResult.get(0).getUserId().equals(destFace.getUniqueId())) {
                // 查询人脸是否存在
                String featureId = searchNResult.get(0).getFeatureId();
                String faceId = featureId.substring(featureId.indexOf("_") + 1);
                BasePersonFace face = basePersonFaceMapper.selectBasePersonFaceById(faceId);
                if (null != face && DictConstants.Status.ENABLE.equals(face.getStatus())) {
                    log.error(
                        "Face warehousing 1-N verification faile,search results:[uniqueId:{},faceId:{}],The face already exists and is not the person [uniqueId:{}]人脸!",
                        searchNResult.get(0).getUserId(), faceId, destFace.getUniqueId());
                    // TODO 删除上传的人脸图片
                    throw new CustomException(MessageUtils.message("base.person.face.1n.check.failed.not.me"));
                } else {
                    log.error(
                        "[FOX_MINISEARCH]Face 1-N search results are wrong, faces that do not exist (or have invalid status) in the relation library [uniqueId:{}, faceId:{}] be identified!",
                        searchNResult.get(0).getUserId(), faceId);
                }
            }
        }
        int result = basePersonFaceMapper.insertBasePersonFace(destFace);
        // 保存人员信息到datamanager
        personDataManagerLogicService.updatePersonData(basePersonInfo.getUniqueId(), basePersonInfo.getName(), destFace,
            null, null, null);
        return result;
    }

    /**
     * 修改人脸图像信息(强制更新，不做任何校验)
     *
     * @param basePersonFace 人脸图像信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonFace(BasePersonFace basePersonFace) {
        BasePersonFace destFace = personFaceRecogLogicService.execCheckAndUploadFace(basePersonFace.getImgBase64(),
            null, basePersonFace, null, false, true);
        // 查询更新之前的信息
        BasePersonFace face = basePersonFaceMapper.selectBasePersonFaceById(destFace.getId());
        String oldImgUrl = face.getImageUrl();
        int result = basePersonFaceMapper.updateBasePersonFace(destFace);
        // 保存人脸信息到datamanager, 无效的人脸信息之前被逻辑删除时从datamanager已经删除，此处不需要处理
        if (DictConstants.Status.ENABLE.equals(basePersonFace.getStatus())
            && StringUtils.isNotBlank(destFace.getFeature())) {
            personDataManagerLogicService.updatePersonData(face.getUniqueId(), null, destFace, null, null, null);
        }
        // 删除旧的图片
        CompletableFuture.runAsync(() -> {
            if (StringUtils.isNotBlank(destFace.getImageUrl())) {
                FileUtils.deleteFile(oldImgUrl);
            }
        });
        // 发布人员信息改变事件
        personChangeEventPublishService.personFaceChangePublish(face.getPersonId(), face.getUniqueId(), null);
        return result;
    }

    /**
     * 批量删除人脸图像信息
     *
     * @param ids 需要删除的人脸图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonFaceByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonFaceById(id);
        }
        return result;
    }

    /**
     * 删除人脸图像信息信息
     *
     * @param id 人脸图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonFaceById(String id) {
        BasePersonFace basePersonFace = new BasePersonFace();
        basePersonFace.setId(id);
        basePersonFace.setStatus(DictConstants.Status.DISABLE);
        basePersonFace.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonFace.setUpdateBy(loginName);
        int result = basePersonFaceMapper.updateBasePersonFace(basePersonFace);
        // 删除DataManager中人员人脸信息
        BasePersonFace face = basePersonFaceMapper.selectBasePersonFaceById(id);
        personDataManagerLogicService.deletePersonFace(face.getUniqueId(), id);
        // 发布人员信息改变事件
        personChangeEventPublishService.personFaceChangePublish(face.getPersonId(), face.getUniqueId(), null);
        return result;
    }

    /**
     * 一键更新人脸特征
     *
     * @param algsVersion
     * @return
     */
    @Override
    public AjaxResult batchUpdateFaceFeature(String algsVersion) {
        boolean tryLock = OperateLock.updateFaceFeatureLock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("base.person.face.feature.updating"));
        }
        try {
            long total = 0; // 数据总量
            int pageNum = 1;// 分页
            int pageSize = 1000;// 分页数量
            String orderBy = SqlUtil.escapeOrderBySql("algs_version asc, id asc");
            // 序列号原子对象
            final AtomicInteger successNum = new AtomicInteger(0);// 更新成功数量原子对象
            final AtomicInteger failNum = new AtomicInteger(0);// 更新失败数量原子对象
            do {
                PageHelper.startPage(pageNum, pageSize, orderBy);
                BasePersonFace basePersonFace = new BasePersonFace();
                // 查询指定版本，对指定算法版本进行更新，重新提取特征
                basePersonFace.setAlgsVersion(algsVersion);
                List<BasePersonFace> list = basePersonFaceMapper.selectBasePersonFaceList(basePersonFace);
                list.stream().forEach(face -> {
                    try {
                        // 判断人脸是否加密
                        String imageBase64 = PlatformFileUtils.getImageBase64(face.getImageUrl());
                        if (DictConstants.Encrypted.ENABLE.equals(face.getEncrypted())) {
                            imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                        }
                        String newFeature = null;
                        String newArgsVersion = null;
                        // 获取特征
                        FaceExtractResult faceExtractResult =
                            personFaceRecogLogicService.getFaceExtractResult(imageBase64);
                        FeatureBean featureBean =
                            personFaceRecogLogicService.getFeatureBean(faceExtractResult, null, null);
                        newFeature = featureBean.getFeature();
                        newArgsVersion = faceExtractResult.getAlgVersion();
                        face.setFeature(newFeature);
                        face.setFeatureMd5(Md5Utils.hash(newFeature));
                        face.setAlgsVersion(newArgsVersion);
                        face.setUpdateTime(DateUtils.getNowDate());
                        // 同时更新人脸特征到关系库和datamanager，启用事务，确保一致性
                        basePersonFaceService.updateFaceFeature(face);
                        successNum.getAndIncrement();
                    } catch (Exception e) {
                        log.error("Update face feature exception", e);
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<BasePersonFace>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("base.person.face.feature.update.sumary", successNum.get(), failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg = MessageUtils.message("base.person.face.feature.update.success.sumary");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.updateFaceFeatureLock.unlock();
        }
    }

    /**
     * 更新人脸特征
     *
     * @param face
     * @return
     */
    @Override
    @Transactional
    public void updateFaceFeature(BasePersonFace face) {
        try {
            // 更新人脸信息到数据库
            basePersonFaceMapper.updateBasePersonFace(face);
            // 保存人员信息到datamanager, 无效的人脸信息之前被逻辑删除时datamanager已经删除，此处不需要处理
            if (DictConstants.Status.ENABLE.equals(face.getStatus())) {
                personDataManagerLogicService.updatePersonData(face.getUniqueId(), null, face, null, null, null);
            }
        } catch (Exception e) {
            log.error("Update face feature exception,faceId:[{}],personId:[{}]", face.getId(), face.getPersonId(), e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 图片下载
     *
     * @param basePersonFace
     * @return
     */
    @Override
    public String downloadImages(BasePersonFace basePersonFace) {
        List<ZipEntryFileModel> entryList = new ArrayList<>();
        String zipNameSuffix = "_face.zip";
        List<BasePersonFace> personFaceList = basePersonFaceMapper.selectBasePersonFaceList(basePersonFace);
        personFaceList.stream().filter(it -> StringUtils.isNotBlank(it.getImageUrl())).forEach(face -> {
            String extension = face.getImageUrl().substring(face.getImageUrl().lastIndexOf("."));
            String fileName = face.getUniqueId()
                + (DictConstants.Status.ENABLE.equals(face.getStatus()) ? "" : "_" + face.getId()) + extension;
            entryList.add(new ZipEntryFileModel(new File(face.getImageUrl()), fileName,
                DictConstants.Encrypted.ENABLE.equals(face.getEncrypted())));
        });
        // zip包的名称
        String downloadFilename = System.currentTimeMillis() + new Random().nextInt() + zipNameSuffix;
        String zipFilePath = getAbsoluteFile(downloadFilename);
        PlatformFileUtils.zipFiles(entryList, new File(zipFilePath));
        return downloadFilename;
    }

    /**
     * 保存批量导入人员
     *
     * @param zipFile
     * @return
     */
    @Override
    public String saveImportData(MultipartFile zipFile) {
        Map<String, Object> uploadImgResMap = uploadFaceImgs(zipFile);
        Integer uploadFailNum = (Integer)uploadImgResMap.get("failureNum");
        if (null != uploadFailNum && uploadFailNum > 0) {
            String failureMsg = (String)uploadImgResMap.get("failureMsg");
            throw new CustomException(failureMsg);
        }
        return (String)uploadImgResMap.get("successMsg");
    }

    /**
     * 保存批量导入人员
     *
     * @param zipFileIStream
     * @return
     */
    @Override
    public String saveImportData(InputStream zipFileIStream) {
        Map<String, Object> uploadImgResMap = uploadFaceImgs(zipFileIStream);
        Integer uploadFailNum = (Integer)uploadImgResMap.get("failureNum");
        if (null != uploadFailNum && uploadFailNum > 0) {
            String failureMsg = (String)uploadImgResMap.get("failureMsg");
            throw new CustomException(failureMsg);
        }
        return (String)uploadImgResMap.get("successMsg");
    }

    /**
     * 保存上传人脸数据到关系库和datamanager
     *
     * @param destFace
     * @param personName
     * @param isUpdate
     */
    @Override
    @Transactional
    public void saveUploadFace(BasePersonFace destFace, String personName, boolean isUpdate) {
        try {
            if (isUpdate) {
                basePersonFaceMapper.updateBasePersonFace(destFace);
            } else {
                basePersonFaceMapper.insertBasePersonFace(destFace);
            }
            // 保存人脸信息到dataManager
            String uniqueId = destFace.getUniqueId();
            personDataManagerLogicService.updatePersonData(uniqueId, personName, destFace, null, null, null);
            // 发布人员信息改变事件
            personChangeEventPublishService.personFaceChangePublish(destFace.getPersonId(), uniqueId, null);
        } catch (Exception e) {
            log.error("Saving and uploading face data is abnormal", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 人脸图片上传
     *
     * @param zipFileIStream
     * @return
     */
    private Map<String, Object> uploadFaceImgs(InputStream zipFileIStream) {
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFileIStream);
        return uploadFaceImgs(unzipFiles);
    }

    /**
     * 人脸图片上传
     *
     * @param zipFile
     * @return
     */
    private Map<String, Object> uploadFaceImgs(MultipartFile zipFile) {
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFile);
        return uploadFaceImgs(unzipFiles);
    }

    /**
     * 人脸图片上传
     *
     * @param unzipFiles
     * @return
     */
    private Map<String, Object> uploadFaceImgs(List<FileModel> unzipFiles) {
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (FileModel fileModel : unzipFiles) {
            String originalFilename = fileModel.getFileName();
            // 文件名（唯一标识）
            String uniqueId = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            // 查询此人信息是否存在(fileName是以用户唯一标识命名的)
            BasePersonInfo existsPerson = getEnabledPersonInfo(uniqueId);
            if (StringUtils.isNull(existsPerson)) {
                failureNum++;
                String msg = MessageUtils.message("base.person.face.upload.photo.failed.no.user", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg);
                continue;
            }
            // 查询此人人脸在数据库中存在与否(fileName是以用户唯一标识命名的)
            BasePersonFace existsFace = getEnabledPersonFaceByUniqueId(uniqueId);
            boolean existsFlag = StringUtils.isNotNull(existsFace);
            try {
                if (!existsFlag) {
                    // 人员信息存在，但是人脸信息不存在， 执行文件上传，并保存人脸
                    BasePersonFace srcFace = new BasePersonFace();
                    srcFace.setUniqueId(uniqueId);
                    srcFace.setPersonId(existsPerson.getId());
                    srcFace.setDatasource(DictConstants.DataSource.IMP);
                    BasePersonFace destFace =
                        personFaceRecogLogicService.execCheckAndUploadFace(fileModel, srcFace, null, false, false);
                    // 保存人脸到关系库和datamanager, 此方法启用事务，确保关系库和datamanager数据一致性
                    basePersonFaceService.saveUploadFace(destFace, existsPerson.getName(), false);
                    successNum++;
                    continue;
                }
                // 比较两张图片的base64的MD5是否一致，一致则略过认为更新成功
                String importImageBase64 = PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
                String stockImageBase64 = PlatformFileUtils.getImageBase64(existsFace.getImageUrl());
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(existsFace.getEncrypted());
                stockImageBase64 =
                    isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
                if (Md5Utils.hash(importImageBase64).equals(Md5Utils.hash(stockImageBase64))) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Uploading face pictures is the same as the pictures in the library, no need to update!");
                    }
                    successNum++;
                    continue;
                }
                existsFace.setDatasource(DictConstants.DataSource.IMP);
                BasePersonFace destFace = personFaceRecogLogicService.execCheckAndUploadFace(importImageBase64,
                    fileModel.getFileName(), existsFace, null, false, true);
                // 保存人脸到关系库和datamanager,此方法启用事务，确保关系库和datamanager数据一致性
                basePersonFaceService.saveUploadFace(destFace, existsPerson.getName(), true);
                successNum++;
            } catch (CustomException e) {
                log.error("Upload face image upload failed,uniqueId:[{}]", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.face.upload.photo.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg + e.getMessage());
            } catch (Exception e) {
                log.error("Failed to upload face image,uniqueId:[{}]", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.face.upload.photo.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("base.person.face.upload.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("base.person.face.upload.success.sumary", successNum);
            successMsg.insert(0, msg);
            log.info(successMsg.toString());
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("failureNum", failureNum);
        resMap.put("failureMsg", failureMsg.toString());
        resMap.put("successNum", successNum);
        resMap.put("successMsg", successMsg.toString());
        return resMap;
    }

    /**
     * 获取下载路径
     *
     * @param filename 文件名称
     */
    private String getAbsoluteFile(String filename) {
        String downloadPath = EyecoolConfig.getDownloadPath() + filename;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        return downloadPath;
    }

    /**
     * 查询有效人员信息
     *
     * @param uniqueId 唯一标识
     * @return 人员信息
     */
    private BasePersonInfo getEnabledPersonInfo(String uniqueId) {
        BasePersonInfo personCondition = new BasePersonInfo();
        personCondition.setUniqueId(uniqueId);
        personCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> personInfoList = basePersonInfoMapper.selectBasePersonInfoList(personCondition);
        if (CollectionUtils.isEmpty(personInfoList)) {
            return null;
        }
        return personInfoList.get(0);
    }

    /**
     * 查询有效人脸信息
     *
     * @param personId 人员主键
     * @return
     */
    private BasePersonFace getEnabledPersonFaceByPersonId(String personId) {
        BasePersonFace faceCondition = new BasePersonFace();
        faceCondition.setPersonId(personId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
        if (CollectionUtils.isEmpty(faceList)) {
            return null;
        }
        return faceList.get(0);
    }

    /**
     * 查询有效人脸信息
     *
     * @param uniqueId 唯一标识
     * @return
     */
    private BasePersonFace getEnabledPersonFaceByUniqueId(String uniqueId) {
        BasePersonFace faceCondition = new BasePersonFace();
        faceCondition.setUniqueId(uniqueId);
        faceCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> faceList = basePersonFaceMapper.selectBasePersonFaceList(faceCondition);
        if (CollectionUtils.isEmpty(faceList)) {
            return null;
        }
        return faceList.get(0);
    }

    /**
     * 校验人员是否有人脸数据
     *
     * @param personId
     * @return
     */
    @Override
    public boolean checkPersonHasFace(String personId) {
        int count = basePersonFaceMapper.countEnabledFaceByPersonId(personId);
        return count > 0;
    }

    /**
     * 进行人脸注册
     *
     * @param faceRegister 人脸注册参数
     */
    @Override
    public void faceRegister(FaceRegister faceRegister, String channleCode, String primarySubCode) {
        String uniqueId = faceRegister.getUniqueId();
        String optionType = faceRegister.getOptionType();
        BasePersonFace condition = new BasePersonFace();
        condition.setUniqueId(uniqueId);
        condition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFace> basePersonFaces = basePersonFaceMapper.selectBasePersonFaceList(condition);
        BasePersonFace basePersonFace = CollectionUtils.isEmpty(basePersonFaces) ? null : basePersonFaces.get(0);
        switch (MultiRegistOptionTypeEnum.parse(optionType)) {
            case ADD:
            case UPDATE:
                if (basePersonFace != null) {
                    deleteBasePersonFaceById(basePersonFace.getId());
                }
                addPersonFace(faceRegister, channleCode, primarySubCode);
                break;
            case DELETE:
                if (basePersonFaces != null) {
                    deleteBasePersonFaceById(basePersonFace.getId());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 添加人脸数据
     *
     * @param faceRegister 人脸注册数据
     */
    private void addPersonFace(FaceRegister faceRegister, String channleCode, String primarySubCode) {
        BasePersonInfo basePersonInfo = operatePersonInfo(faceRegister);
        BasePersonFace basePersonFace = new BasePersonFace();
        basePersonFace.setPersonId(basePersonInfo.getId());
        basePersonFace.setUniqueId(faceRegister.getUniqueId());
        basePersonFace.setImgBase64(faceRegister.getFaceBase64Img());
        basePersonFace.setFeature(faceRegister.getFaceFeatureBase64());
        handlePersonFace(basePersonFace, basePersonInfo);
        personChangeEventPublishService.personFaceRegisterPublish(basePersonInfo.getId(), basePersonInfo.getUniqueId(),
            channleCode, primarySubCode, DictConstants.DataSource.HTTP_INTERFACE);
    }

    /**
     * 多模态注册处理人员基础信息
     *
     * @param faceRegister
     * @return
     */
    private BasePersonInfo operatePersonInfo(FaceRegister faceRegister) {
        String uniqueId = faceRegister.getUniqueId();
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setUniqueId(uniqueId);
        List<BasePersonInfo> basePersonInfos = basePersonInfoMapper.selectBasePersonInfoList(basePersonInfo);
        if (CollectionUtils.isEmpty(basePersonInfos)) {
            BasePersonPutInfo personInfo = new BasePersonPutInfo();
            personInfo.setId(IdWorker.getNextStringId());
            personInfo.setUniqueId(uniqueId);
            personInfo.setName(faceRegister.getName());
            personInfo.setCardNo(faceRegister.getCardNo());
            personInfo.setPhone(faceRegister.getPhone());
            personInfo.setStatus(DictConstants.Status.ENABLE);
            personInfo.setCreateTime(new Date());
            personInfo.setDatasource(DictConstants.DataSource.HTTP_INTERFACE);
            personInfo.setRemark(faceRegister.getRemark());
            basePersonInfoMapper.insertBasePersonInfo(personInfo);
            return personInfo;
        }
        BasePersonInfo person = basePersonInfos.get(0);
        person.setStatus(DictConstants.Status.ENABLE);
        person.setUpdateTime(new Date());
        person.setCardNo(faceRegister.getCardNo());
        person.setName(faceRegister.getName());
        person.setPhone(faceRegister.getPhone());
        basePersonInfoMapper.updateBasePersonInfo(person);
        return person;
    }

}
