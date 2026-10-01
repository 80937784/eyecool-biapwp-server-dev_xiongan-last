package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.eyecool.abis.callmicroservice.common.FeatureBean;
import com.eyecool.abis.callmicroservice.common.FingerExtractResult;
import com.eyecool.abis.callmicroservice.common.FingerSearchResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.constant.OperateLock;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.manager.IPersonFingerRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonFingerMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.service.IBasePersonFingerService;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileModel;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.file.ZipEntryFileModel;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.common.utils.sql.SqlUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 指纹图像信息Service业务层处理
 * 
 * @author mawj
 * @date 2021-01-27
 */
@Service
@Slf4j
public class BasePersonFingerServiceImpl implements IBasePersonFingerService {

    @Autowired
    private BasePersonFingerMapper basePersonFingerMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IPersonFingerRecogLogicService personFingerRecogLogicService;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    /** 自己注入自己实现同一个类内部方法调用事务起作用，spring框架解决了循环注入问题，但注入自己这种方式要慎用 */
    @Autowired
    private IBasePersonFingerService basePersonFingerService;

    /**
     * 查询指纹图像信息
     * 
     * @param id 指纹图像信息ID
     * @return 指纹图像信息
     */
    @Override
    public BasePersonFinger selectBasePersonFingerById(String id) {
        return basePersonFingerMapper.selectBasePersonFingerById(id);
    }

    /**
     * 查询指纹图像信息列表
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 指纹图像信息
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonFinger> selectBasePersonFingerList(BasePersonFinger basePersonFinger) {
        return basePersonFingerMapper.selectBasePersonFingerList(basePersonFinger);
    }

    /**
     * 新增指纹图像信息
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBasePersonFinger(BasePersonFinger basePersonFinger) {
        // 查询人员信息是否存在,获取personId
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonFinger.getUniqueId());
        if (null == basePersonInfo) {
            throw new CustomException(MessageUtils.message("base.person.finger.user.not.exists"));
        }
        String personId = basePersonInfo.getId();
        basePersonFinger.setPersonId(personId);

        // 查询指纹信息是否存在
        List<BasePersonFinger> fingerList = getEnabledPersonFingerByPersonId(personId, basePersonFinger.getFingerNo());
        // 手指号未知的话，允许存在多个未知指号的指纹
        boolean isUncertainFingerNo =
            DictConstants.UncertainFingerNo.uncertainFingerNoList.contains(basePersonFinger.getFingerNo());
        if (CollectionUtils.isNotEmpty(fingerList) && !isUncertainFingerNo) {
            throw new CustomException(MessageUtils.message("base.person.finger.image.exists"));
        }
        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        String fingerImgBase64 = basePersonFinger.getImgBase64();
        BasePersonFinger destFinger = personFingerRecogLogicService.execCheckAndUploadFinger(fingerImgBase64, null,
            basePersonFinger, null, false, false);

        // 入库是否进行1-N校验
        boolean fingerAddIsValidateN = personFingerRecogLogicService.getFingerAddIsValidateN();
        if (fingerAddIsValidateN) {
            List<FingerSearchResult> searchNResult =
                personFingerRecogLogicService.fingerSearchN(destFinger.getFeature(), "", 1, null);
            if (CollectionUtils.isNotEmpty(searchNResult)
                && !searchNResult.get(0).getUserId().equals(destFinger.getUniqueId())) {
                // 查询指纹是否存在
                String featureId = searchNResult.get(0).getFeatureId();
                String fingerId = featureId.substring(featureId.indexOf("_") + 4);
                BasePersonFinger finger = basePersonFingerMapper.selectBasePersonFingerById(fingerId);
                if (null != finger && DictConstants.Status.ENABLE.equals(finger.getStatus())) {
                    log.error(
                        "Fingerprint storage 1-N verification failed, search results:[uniqueId:{},fingerId:{}],Fingerprint already exists and not me.  [uniqueId:{}]",
                        searchNResult.get(0).getUserId(), fingerId, destFinger.getUniqueId());
                    // TODO 删除上传的指纹图片
                    throw new CustomException(MessageUtils.message("base.person.finger.1n.check.failed.not.me"));
                } else {
                    log.error(
                        "[FOX_MINISEARCH] Fingerprints 1-N search results are wrong, fingerprints that do not exist (or have an invalid state) in the relation library. [uniqueId:{}, fingerId:{}]!",
                        searchNResult.get(0).getUserId(), fingerId);
                }
            }
        }
        int result = basePersonFingerMapper.insertBasePersonFinger(destFinger);
        // 保存人员信息到datamanager
        personDataManagerLogicService.updatePersonData(basePersonInfo.getUniqueId(), basePersonInfo.getName(), null,
            Lists.newArrayList(destFinger), null, null);
        // 发布人员信息改变事件
        personChangeEventPublishService.personFingerChangePublish(personId, basePersonInfo.getUniqueId(), null);
        return result;
    }

    /**
     * 修改指纹图像信息(强制更新)
     * 
     * @param basePersonFinger 指纹图像信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonFinger(BasePersonFinger basePersonFinger) {
        BasePersonFinger destFinger = personFingerRecogLogicService
            .execCheckAndUploadFinger(basePersonFinger.getImgBase64(), null, basePersonFinger, null, false, true);
        // 查询更新之前的信息
        BasePersonFinger finger = basePersonFingerMapper.selectBasePersonFingerById(basePersonFinger.getId());
        String oldImgUrl = finger.getImageUrl();
        int result = basePersonFingerMapper.updateBasePersonFinger(destFinger);
        // 保存指纹信息到datamanager, 无效的指纹信息之前被逻辑删除时datamanager已经删除，此处不需要处理
        if (DictConstants.Status.ENABLE.equals(basePersonFinger.getStatus())
            && StringUtils.isNotBlank(destFinger.getFeature())) {
            List<String> deletedPersonFeatureIds =
                personDataManagerLogicService.getPersonFeatureIds(null, Arrays.asList(finger), null);
            personDataManagerLogicService.updatePersonData(finger.getUniqueId(), null, null,
                Lists.newArrayList(destFinger), null, deletedPersonFeatureIds);
        }
        // 删除旧的图片
        CompletableFuture.runAsync(() -> {
            if (StringUtils.isNotBlank(destFinger.getImageUrl())) {
                PlatformFileUtils.deleteFile(oldImgUrl);
            }
        });
        // 发布人员信息改变事件
        personChangeEventPublishService.personFaceChangePublish(finger.getPersonId(), finger.getUniqueId(), null);
        return result;
    }

    /**
     * 批量删除指纹图像信息
     * 
     * @param ids 需要删除的指纹图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonFingerByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonFingerById(id);
        }
        return result;
    }

    /**
     * 删除指纹图像信息信息
     * 
     * @param id 指纹图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonFingerById(String id) {
        BasePersonFinger basePersonFinger = new BasePersonFinger();
        basePersonFinger.setId(id);
        basePersonFinger.setStatus(DictConstants.Status.DISABLE);
        basePersonFinger.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonFinger.setUpdateBy(loginName);
        int result = basePersonFingerMapper.updateBasePersonFinger(basePersonFinger);
        // 删除DataManager中人员指纹信息
        BasePersonFinger finger = basePersonFingerMapper.selectBasePersonFingerById(id);
        personDataManagerLogicService.deletePersonFinger(finger.getUniqueId(), finger.getFingerNo(), id);
        // 发布人员信息改变事件
        personChangeEventPublishService.personFingerChangePublish(finger.getPersonId(), finger.getUniqueId(), null);
        return result;
    }

    /**
     * 一键更新所有指纹特征
     * 
     * @param algsVersion
     * @return
     */
    @Override
    public AjaxResult batchUpdateFingerFeature(String algsVersion) {
        boolean tryLock = OperateLock.updateFingerFeatureLock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("base.person.finger.feature.updating"));
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
                BasePersonFinger basePersonFinger = new BasePersonFinger();
                // 查询指定版本，对指定算法版本进行更新，重新提取特征
                basePersonFinger.setAlgsVersion(algsVersion);
                List<BasePersonFinger> list = basePersonFingerMapper.selectBasePersonFingerList(basePersonFinger);
                list.stream().forEach(finger -> {
                    try {
                        // 判断指纹是否加密
                        String imageBase64 = PlatformFileUtils.getImageBase64(finger.getImageUrl());
                        if (DictConstants.Encrypted.ENABLE.equals(finger.getEncrypted())) {
                            imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                        }
                        String newFeature = null;
                        String newArgsVersion = null;
                        // 获取特征
                        FingerExtractResult fingerExtractResult =
                            personFingerRecogLogicService.getFingerExtractResult(imageBase64);
                        FeatureBean featureBean =
                            personFingerRecogLogicService.getFeatureBean(fingerExtractResult, null, null);
                        newFeature = featureBean.getFeature();
                        newArgsVersion = fingerExtractResult.getAlgVersion();
                        finger.setFeature(newFeature);
                        finger.setFeatureMd5(Md5Utils.hash(newFeature));
                        finger.setAlgsVersion(newArgsVersion);
                        finger.setUpdateTime(DateUtils.getNowDate());
                        // 同时更新指纹特征到关系库和datamanager，启用事务，确保一致性
                        basePersonFingerService.updateFingerFeature(finger);
                        successNum.getAndIncrement();
                    } catch (Exception e) {
                        log.error("Update fingerprint feature exception", e);
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<BasePersonFinger>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("base.person.finger.feature.update.sumary", successNum.get(), failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg = MessageUtils.message("base.person.finger.feature.update.success.sumary");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.updateFingerFeatureLock.unlock();
        }

    }

    /**
     * 更新指纹特征
     *
     * @param finger
     * @return
     */
    @Override
    @Transactional
    public void updateFingerFeature(BasePersonFinger finger) {
        try {
            // 更新指纹信息到数据库
            basePersonFingerMapper.updateBasePersonFinger(finger);
            // 更新指纹信息到datamanager, 无效的指纹信息之前被逻辑删除时datamanager已经删除，此处不需要处理
            if (DictConstants.Status.ENABLE.equals(finger.getStatus())) {
                List<String> deletedPersonFeatureIds =
                    personDataManagerLogicService.getPersonFeatureIds(null, Arrays.asList(finger), null);
                personDataManagerLogicService.updatePersonData(finger.getUniqueId(), null, null,
                    Lists.newArrayList(finger), null, deletedPersonFeatureIds);
            }
        } catch (Exception e) {
            log.error("Update fingerprint feature exception", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 下载指纹图片
     * 
     * @param basePersonFinger
     * @return
     */
    @Override
    public String downloadImages(BasePersonFinger basePersonFinger) {
        List<ZipEntryFileModel> entryList = new ArrayList<>();
        String zipNameSuffix = "_finger.zip";
        List<BasePersonFinger> personFingerList = basePersonFingerMapper.selectBasePersonFingerList(basePersonFinger);
        personFingerList.stream().filter(it -> StringUtils.isNotBlank(it.getImageUrl())).forEach(finger -> {
            String extension = finger.getImageUrl().substring(finger.getImageUrl().lastIndexOf("."));
            String fileName = finger.getUniqueId() + "_" + finger.getFingerNo()
                + (DictConstants.Status.ENABLE.equals(finger.getStatus()) ? "" : "_" + finger.getId()) + extension;
            entryList.add(new ZipEntryFileModel(new File(finger.getImageUrl()), fileName,
                DictConstants.Encrypted.ENABLE.equals(finger.getEncrypted())));
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
        Map<String, Object> uploadImgResMap = uploadFingerImgs(zipFile);
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
        Map<String, Object> uploadImgResMap = uploadFingerImgs(zipFileIStream);
        Integer uploadFailNum = (Integer)uploadImgResMap.get("failureNum");
        if (null != uploadFailNum && uploadFailNum > 0) {
            String failureMsg = (String)uploadImgResMap.get("failureMsg");
            throw new CustomException(failureMsg);
        }
        return (String)uploadImgResMap.get("successMsg");
    }

    /**
     * 保存上传指纹数据到关系库和datamanager
     * 
     * @param destFinger
     * @param personName
     * @param isUpdate
     */
    @Override
    @Transactional
    public void saveUploadFinger(BasePersonFinger destFinger, String personName, boolean isUpdate) {
        try {
            if (isUpdate) {
                basePersonFingerMapper.updateBasePersonFinger(destFinger);
            } else {
                basePersonFingerMapper.insertBasePersonFinger(destFinger);
            }
            // 保存指纹信息到dataManager
            List<String> deletedPersonFeatureIds =
                personDataManagerLogicService.getPersonFeatureIds(null, Lists.newArrayList(destFinger), null);
            personDataManagerLogicService.updatePersonData(destFinger.getUniqueId(), personName, null,
                Lists.newArrayList(destFinger), null, deletedPersonFeatureIds);
            // 发布人员信息改变事件
            personChangeEventPublishService.personFingerChangePublish(destFinger.getPersonId(),
                destFinger.getUniqueId(), null);
        } catch (Exception e) {
            log.error("Save and upload fingerprint data is abnormal", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 指纹图片上传
     * 
     * @param zipFile
     * @return
     */
    private Map<String, Object> uploadFingerImgs(MultipartFile zipFile) {
        // 解压缩
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFile);
        return uploadFingerImgs(unzipFiles);
    }

    /**
     * 指纹图片上传
     * 
     * @param zipFile
     * @return
     */
    private Map<String, Object> uploadFingerImgs(InputStream zipFileIStream) {
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFileIStream);
        return uploadFingerImgs(unzipFiles);
    }

    /**
     * 指纹图片上传
     * 
     * @param unzipFiles
     * @return
     */
    private Map<String, Object> uploadFingerImgs(List<FileModel> unzipFiles) {
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (FileModel fileModel : unzipFiles) {
            String originalFilename = fileModel.getFileName();
            // 文件名（唯一标识_手指号）
            String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String fingerNo = fileName.substring(fileName.lastIndexOf("_") + 1);
            String uniqueId = fileName.substring(0, fileName.lastIndexOf("_"));
            // 查询此人信息是否存在
            BasePersonInfo existsPerson = getEnabledPersonInfo(uniqueId);
            if (StringUtils.isNull(existsPerson)) {
                failureNum++;
                String msg = MessageUtils.message("base.person.finger.upload.photo.failed.no.user", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg);
                continue;
            }
            // 查询此人指纹在数据库中存在与否(fileName是以用户唯一标识命名的)
            List<BasePersonFinger> existsFingerList = getEnabledPersonFingerByUniqueId(uniqueId, fingerNo);
            boolean existsFlag = CollectionUtils.isNotEmpty(existsFingerList);
            try {
                // 指纹数据不存在或是未知指号,执行文件上传，并新增保存指纹
                if (!existsFlag || DictConstants.UncertainFingerNo.uncertainFingerNoList.contains(fingerNo)) {
                    BasePersonFinger srcFinger = new BasePersonFinger();
                    srcFinger.setFingerNo(fingerNo);
                    srcFinger.setUniqueId(uniqueId);
                    srcFinger.setPersonId(existsPerson.getId());
                    srcFinger.setDatasource(DictConstants.DataSource.IMP);
                    BasePersonFinger destFinger = personFingerRecogLogicService.execCheckAndUploadFinger(fileModel,
                        srcFinger, null, false, false);
                    // 保存指纹到关系库和datamanager,此方法启用事务，确保关系库和datamanager数据一致性
                    basePersonFingerService.saveUploadFinger(destFinger, existsPerson.getName(), false);
                    successNum++;
                    continue;
                }
                BasePersonFinger existsFinger = existsFingerList.get(0);
                // 比较两张图片的base64的MD5是否一致，一致则略过认为更新成功
                String importImageBase64 = PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
                String stockImageBase64 = PlatformFileUtils.getImageBase64(existsFinger.getImageUrl());
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(existsFinger.getEncrypted());
                stockImageBase64 =
                    isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
                if (Md5Utils.hash(importImageBase64).equals(Md5Utils.hash(stockImageBase64))) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Uploading the fingerprint image is the same as the image in the library, no need to update!");
                    }
                    successNum++;
                    continue;
                }
                existsFinger.setDatasource(DictConstants.DataSource.IMP);
                BasePersonFinger destFinger = personFingerRecogLogicService.execCheckAndUploadFinger(importImageBase64,
                    fileModel.getFileName(), existsFinger, null, false, true);
                // 保存指纹到关系库和datamanager,此方法启用事务，确保关系库和datamanager数据一致性
                basePersonFingerService.saveUploadFinger(destFinger, existsPerson.getName(), true);
                successNum++;
            } catch (CustomException e) {
                log.error("Fingerprint image upload failed,uniqueId:{}", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.finger.upload.photo.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg + e.getMessage());
            } catch (Exception e) {
                log.error("Fingerprint image upload failed,uniqueId:{}", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.finger.upload.photo.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("base.person.finger.upload.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("base.person.finger.upload.success.sumary", successNum);
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
     * 查询有效指纹信息
     * 
     * @param personId 人员主键
     * @param fingerNo 手指编号
     * @return
     */
    private List<BasePersonFinger> getEnabledPersonFingerByPersonId(String personId, String fingerNo) {
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setPersonId(personId);
        fingerCondition.setFingerNo(fingerNo);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFinger> fingerList = basePersonFingerMapper.selectBasePersonFingerList(fingerCondition);
        return fingerList;
    }

    /**
     * 查询有效指纹信息
     * 
     * @param uniqueId 人员唯一标识
     * @param fingerNo 手指编号
     * @return
     */
    private List<BasePersonFinger> getEnabledPersonFingerByUniqueId(String uniqueId, String fingerNo) {
        BasePersonFinger fingerCondition = new BasePersonFinger();
        fingerCondition.setUniqueId(uniqueId);
        fingerCondition.setFingerNo(fingerNo);
        fingerCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonFinger> fingerList = basePersonFingerMapper.selectBasePersonFingerList(fingerCondition);
        return fingerList;
    }

    /**
     * 校验人员是否有指纹数据
     * 
     * @param personId
     * @return
     */
    @Override
    public boolean checkPersonHasFinger(String personId) {
        int count = basePersonFingerMapper.countEnabledFingerByPersonId(personId);
        return count > 0;
    }
}
