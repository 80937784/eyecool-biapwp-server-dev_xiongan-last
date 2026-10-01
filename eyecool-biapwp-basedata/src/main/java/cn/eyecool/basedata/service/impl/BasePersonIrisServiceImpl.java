package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.eyecool.abis.callmicroservice.common.IrisExtractResult;
import com.eyecool.abis.callmicroservice.common.IrisSearchResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.constant.OperateLock;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.domain.BasePersonIris;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.manager.IPersonDataManagerLogicService;
import cn.eyecool.basedata.manager.IPersonIrisRecogLogicService;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.mapper.BasePersonIrisMapper;
import cn.eyecool.basedata.service.IBasePersonIrisService;
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
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.file.ZipEntryFileModel;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.common.utils.sql.SqlUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 虹膜图像信息Service业务层处理
 * 
 * @author mawj
 * @date 2021-01-27
 */
@Service
@Slf4j
public class BasePersonIrisServiceImpl implements IBasePersonIrisService {

    @Autowired
    private BasePersonIrisMapper basePersonIrisMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private IPersonIrisRecogLogicService personIrisRecogLogicService;
    @Autowired
    private IPersonDataManagerLogicService personDataManagerLogicService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    /** 自己注入自己实现同一个类内部方法调用事务起作用，spring框架解决了循环注入问题，但注入自己这种方式要慎用 */
    @Autowired
    private IBasePersonIrisService basePersonIrisService;

    /**
     * 查询虹膜图像信息
     * 
     * @param id 虹膜图像信息ID
     * @return 虹膜图像信息
     */
    @Override
    public BasePersonIris selectBasePersonIrisById(String id) {
        return basePersonIrisMapper.selectBasePersonIrisById(id);
    }

    /**
     * 查询虹膜图像信息列表
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 虹膜图像信息
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonIris> selectBasePersonIrisList(BasePersonIris basePersonIris) {
        return basePersonIrisMapper.selectBasePersonIrisList(basePersonIris);
    }

    /**
     * 新增虹膜图像信息
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBasePersonIris(BasePersonIris basePersonIris) {
        // 校验人员和图片是存在
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonIris.getUniqueId());
        if (null == basePersonInfo) {
            throw new CustomException(MessageUtils.message("base.person.iris.valid.user.not.exists"));
        }
        basePersonIris.setPersonId(basePersonInfo.getId());;
        checkIrisExists(basePersonIris);
        // 虹膜上传
        BasePersonIris destIris = checkAndUploadIris(basePersonIris);

        // 校验是否是本人虹膜
        boolean irisAddIsValidateN = personIrisRecogLogicService.getIrisAddIsValidateN();
        if (irisAddIsValidateN) {
            irisSearchNValidate(destIris);
        }
        int result = basePersonIrisMapper.insertBasePersonIris(destIris);
        // 保存人员信息到datamanager
        personDataManagerLogicService.updatePersonData(basePersonInfo.getUniqueId(), basePersonInfo.getName(), null,
            null, destIris, null);
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisChangePublish(basePersonInfo.getId(), basePersonInfo.getUniqueId(),
            null);
        return result;
    }

    /**
     * 修改虹膜图像信息(强制更新)
     * 
     * @param basePersonIris 虹膜图像信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonIris(BasePersonIris basePersonIris) {
        String imgBase64 = basePersonIris.getImgBase64();
        BasePersonIris destIris =
            personIrisRecogLogicService.execCheckAndUploadIris(imgBase64, null, basePersonIris, null, false, true);
        // 查询更新之前的信息
        BasePersonIris iris = basePersonIrisMapper.selectBasePersonIrisById(basePersonIris.getId());
        String oldImgUrl = iris.getImageUrl();
        int result = basePersonIrisMapper.updateBasePersonIris(destIris);
        // 保存虹膜信息到datamanager, 无效的虹膜信息之前被逻辑删除时datamanager已经删除，此处不需要处理
        if (DictConstants.Status.ENABLE.equals(basePersonIris.getStatus())
            && StringUtils.isNotBlank(destIris.getFeature())) {
            List<String> deletedPersonFeatureIds =
                personDataManagerLogicService.getPersonFeatureIds(null, null, basePersonIris.getId());
            personDataManagerLogicService.updatePersonData(iris.getUniqueId(), null, null, null, destIris,
                deletedPersonFeatureIds);
        }
        // 删除旧的图片
        CompletableFuture.runAsync(() -> {
            if (StringUtils.isNotBlank(destIris.getImageUrl())) {
                FileUtils.deleteFile(oldImgUrl);
            }
        });
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisChangePublish(iris.getPersonId(), iris.getUniqueId(), null);
        return result;
    }

    /**
     * 批量删除虹膜图像信息
     * 
     * @param ids 需要删除的虹膜图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonIrisByIds(String[] ids) {
        // 使用逻辑删除
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonIrisById(id);
        }
        return result;
    }

    /**
     * 删除虹膜图像信息信息
     * 
     * @param id 虹膜图像信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonIrisById(String id) {
        BasePersonIris basePersonIris = new BasePersonIris();
        basePersonIris.setId(id);
        basePersonIris.setStatus(DictConstants.Status.DISABLE);
        basePersonIris.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonIris.setUpdateBy(loginName);
        int result = basePersonIrisMapper.updateBasePersonIris(basePersonIris);
        // 删除DataManager中人员虹膜信息
        BasePersonIris iris = basePersonIrisMapper.selectBasePersonIrisById(id);
        personDataManagerLogicService.deletePersonIris(iris.getUniqueId(), id);
        // 发布人员信息改变事件
        personChangeEventPublishService.personIrisChangePublish(iris.getPersonId(), iris.getUniqueId(), null);
        return result;
    }

    /**
     * 一键更新所有虹膜特征
     * 
     * @param algsVersion
     * @return
     */
    @Override
    public AjaxResult batchUpdateIrisFeature(String algsVersion) {
        boolean tryLock = OperateLock.updateIrisFeatureLock.tryLock();
        if (!tryLock) {
            throw new CustomException(MessageUtils.message("base.person.iris.feature.updating"));
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
                BasePersonIris basePersonIris = new BasePersonIris();
                // 查询指定版本，对指定算法版本进行更新，重新提取特征
                basePersonIris.setAlgsVersion(algsVersion);
                List<BasePersonIris> list = basePersonIrisMapper.selectBasePersonIrisList(basePersonIris);
                list.stream().forEach(iris -> {
                    try {
                        // 判断虹膜是否加密
                        String imageBase64 = PlatformFileUtils.getImageBase64(iris.getImageUrl());
                        if (DictConstants.Encrypted.ENABLE.equals(iris.getEncrypted())) {
                            imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                        }
                        String newFeature = null;
                        String newArgsVersion = null;
                        // 获取特征
                        IrisExtractResult irisExtractResult =
                            personIrisRecogLogicService.getIrisExtractResult(imageBase64);
                        FeatureBean featureBean =
                            personIrisRecogLogicService.getFeatureBean(irisExtractResult, null, null);
                        newFeature = featureBean.getFeature();
                        newArgsVersion = irisExtractResult.getAlgVersion();
                        iris.setFeature(newFeature);
                        iris.setFeatureMd5(Md5Utils.hash(newFeature));
                        iris.setAlgsVersion(newArgsVersion);
                        iris.setUpdateTime(DateUtils.getNowDate());
                        // 同时更新虹膜特征到关系库和datamanager，启用事务，确保一致性
                        basePersonIrisService.updateIrisFeature(iris);
                        successNum.getAndIncrement();
                    } catch (Exception e) {
                        log.error("Update iris feature exception", e);
                        failNum.getAndIncrement();
                    }
                });
                total = new PageInfo<BasePersonIris>(list).getTotal();
                pageNum++;
            } while ((pageNum - 1) * pageSize < total);
            Map<String, Integer> map = Maps.newHashMap();
            map.put("failNum", failNum.get());
            map.put("successNum", successNum.get());
            String msg = "";
            if (failNum.get() > 0) {
                msg = MessageUtils.message("base.person.iris.update.feature.sumary", successNum.get(), failNum.get());
                return AjaxResult.error(msg, map);
            } else {
                msg = MessageUtils.message("base.person.iris.update.feature.success.sumary");
                return AjaxResult.success(msg, map);
            }
        } finally {
            OperateLock.updateIrisFeatureLock.unlock();
        }

    }

    /**
     * 更新虹膜特征
     * 
     * @param iris
     */
    @Override
    @Transactional
    public void updateIrisFeature(BasePersonIris iris) {
        try {
            // 更新虹膜信息到数据库
            basePersonIrisMapper.updateBasePersonIris(iris);
            // 更新虹膜信息到datamanager, 无效的虹膜信息之前被逻辑删除时datamanager已经删除，此处不需要处理
            if (DictConstants.Status.ENABLE.equals(iris.getStatus())) {
                List<String> deletedPersonFeatureIds =
                    personDataManagerLogicService.getPersonFeatureIds(null, null, iris.getId());
                personDataManagerLogicService.updatePersonData(iris.getUniqueId(), null, null, null, iris,
                    deletedPersonFeatureIds);
            }
        } catch (Exception e) {
            log.error("Update iris feature exception", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 下载虹膜图像
     * 
     * @param basePersonIris
     * @return
     */
    @Override
    public String downloadImages(BasePersonIris basePersonIris) {
        List<ZipEntryFileModel> entryList = new ArrayList<>();
        String zipNameSuffix = "_iris.zip";
        List<BasePersonIris> personIrisList = basePersonIrisMapper.selectBasePersonIrisList(basePersonIris);
        personIrisList.stream().filter(it -> StringUtils.isNotBlank(it.getImageUrl())).forEach(iris -> {
            String extension = iris.getImageUrl().substring(iris.getImageUrl().lastIndexOf("."));
            String fileName = iris.getUniqueId()
                + (DictConstants.Status.ENABLE.equals(iris.getStatus()) ? "" : "_" + iris.getId()) + extension;
            entryList.add(new ZipEntryFileModel(new File(iris.getImageUrl()), fileName,
                DictConstants.Encrypted.ENABLE.equals(iris.getEncrypted())));
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
        Map<String, Object> uploadImgResMap = uploadIrisImgs(zipFile);
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
        Map<String, Object> uploadImgResMap = uploadIrisImgs(zipFileIStream);
        Integer uploadFailNum = (Integer)uploadImgResMap.get("failureNum");
        if (null != uploadFailNum && uploadFailNum > 0) {
            String failureMsg = (String)uploadImgResMap.get("failureMsg");
            throw new CustomException(failureMsg);
        }
        return (String)uploadImgResMap.get("successMsg");
    }

    /**
     * 保存上传虹膜数据到关系库和datamanager
     * 
     * @param destIris
     * @param personName
     * @param isUpdate
     */
    @Override
    @Transactional
    public void saveUploadIris(BasePersonIris destIris, String personName, boolean isUpdate) {
        try {
            if (isUpdate) {
                basePersonIrisMapper.updateBasePersonIris(destIris);
            } else {
                basePersonIrisMapper.insertBasePersonIris(destIris);
            }
            // 保存虹膜信息到dataManager
            List<String> deletedPersonFeatureIds =
                personDataManagerLogicService.getPersonFeatureIds(null, null, destIris.getId());
            personDataManagerLogicService.updatePersonData(destIris.getUniqueId(), personName, null, null, destIris,
                deletedPersonFeatureIds);
            // 发布人员信息改变事件
            personChangeEventPublishService.personFingerChangePublish(destIris.getPersonId(), destIris.getUniqueId(),
                null);
        } catch (Exception e) {
            log.error("Saving and uploading iris data is abnormal", e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * 虹膜图片上传
     * 
     * @param zipFile
     * @return
     */
    private Map<String, Object> uploadIrisImgs(MultipartFile zipFile) {
        // 解压缩
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFile);
        return uploadIrisImgs(unzipFiles);
    }

    /**
     * 虹膜图片上传
     * 
     * @param zipFileIStream
     * @return
     */
    private Map<String, Object> uploadIrisImgs(InputStream zipFileIStream) {
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFileIStream);
        return uploadIrisImgs(unzipFiles);
    }

    /**
     * 虹膜图片上传
     * 
     * @param unzipFiles
     * @return
     */
    private Map<String, Object> uploadIrisImgs(List<FileModel> unzipFiles) {
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (FileModel fileModel : unzipFiles) {
            String originalFilename = fileModel.getFileName();
            // 文件名（唯一标识）
            String uniqueId = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            // 查询此人信息是否存在
            BasePersonInfo existsPerson = getEnabledPersonInfo(uniqueId);
            if (StringUtils.isNull(existsPerson)) {
                failureNum++;
                failureMsg.append("<br/>" + failureNum
                    + MessageUtils.message("base.person.iris.upload.failed.valid.user.not.exists", uniqueId));
                continue;
            }
            // 查询此人虹膜在数据库中存在与否(fileName是以用户唯一标识命名的)
            BasePersonIris existsIris = getEnabledPersonIrisByUniqueId(uniqueId);
            boolean existsFlag = StringUtils.isNotNull(existsIris);
            try {
                // 人员信息存在，但是虹膜信息不存在， 执行文件上传，并保存虹膜
                if (!existsFlag) {
                    BasePersonIris srcIris = new BasePersonIris();
                    srcIris.setUniqueId(uniqueId);
                    srcIris.setPersonId(existsPerson.getId());
                    srcIris.setDatasource(DictConstants.DataSource.IMP);
                    BasePersonIris destIris =
                        personIrisRecogLogicService.execCheckAndUploadIris(fileModel, srcIris, null, false, false);
                    // 保存虹膜信息到关系库和dataManager,此方法启用事务，确保关系库和datamanager数据一致性
                    basePersonIrisService.saveUploadIris(destIris, existsPerson.getName(), false);
                    successNum++;
                    continue;
                }
                // 比较两张图片的base64的MD5是否一致，一致则略过认为更新成功
                String importImageBase64 = PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
                String stockImageBase64 = PlatformFileUtils.getImageBase64(existsIris.getImageUrl());
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(existsIris.getEncrypted());
                stockImageBase64 =
                    isEncrypted ? PlatformCryptUtils.decryptImageBase64(stockImageBase64) : stockImageBase64;
                if (Md5Utils.hash(importImageBase64).equals(Md5Utils.hash(stockImageBase64))) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Uploading the iris picture is the same as the picture in the library, no need to update!");
                    }
                    successNum++;
                    continue;
                }
                existsIris.setDatasource(DictConstants.DataSource.IMP);
                BasePersonIris destIris = personIrisRecogLogicService.execCheckAndUploadIris(importImageBase64,
                    fileModel.getFileName(), existsIris, null, false, true);
                // 保存虹膜信息到关系库和dataManager,此方法启用事务，确保关系库和datamanager数据一致性
                basePersonIrisService.saveUploadIris(destIris, existsPerson.getName(), true);
                successNum++;
            } catch (CustomException e) {
                log.error("Iris image upload failed,uniqueId:[{}]", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.iris.upload.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg + e.getMessage());
            } catch (Exception e) {
                log.error("Iris image upload failed,uniqueId:[{}]", uniqueId, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.iris.upload.failed", uniqueId);
                failureMsg.append("<br/>" + failureNum + msg);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("base.person.iris.upload.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("base.person.iris.upload.success.sumary", successNum);
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
     * 查询有效虹膜信息
     * 
     * @param uniqueId 人员唯一标识
     * @return
     */
    private BasePersonIris getEnabledPersonIrisByUniqueId(String uniqueId) {
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setUniqueId(uniqueId);
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisMapper.selectBasePersonIrisList(irisCondition);
        return CollectionUtils.isEmpty(irisList) ? null : irisList.get(0);
    }

    /**
     * 新增虹膜校验虹膜图片是否已存在
     * 
     * @param basePersonIris
     */
    private void checkIrisExists(BasePersonIris basePersonIris) {
        // 查询虹膜图片信息是否存在
        BasePersonIris irisCondition = new BasePersonIris();
        irisCondition.setPersonId(basePersonIris.getPersonId());
        irisCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonIris> irisList = basePersonIrisMapper.selectBasePersonIrisList(irisCondition);

        if (CollectionUtils.isNotEmpty(irisList)) {
            throw new CustomException(MessageUtils.message("base.person.iris.image.exists"));
        }
    }

    /**
     * 新增上传虹膜
     * 
     * @param basePersonIris
     * @return
     */
    private BasePersonIris checkAndUploadIris(BasePersonIris basePersonIris) {
        String imgBase64 = basePersonIris.getImgBase64();
        String feature = basePersonIris.getFeature();
        if (StringUtils.isBlank(imgBase64) && StringUtils.isBlank(feature)) {
            return null;
        }
        // 考虑到可能出现abis搜索结果有误（关系库不存在的人被搜索出来），因为校验不通过会抛出异常，此处调用不进行1：N校验，后边单独进行1：N校验并查询关系库排除误搜索
        return personIrisRecogLogicService.execCheckAndUploadIris(imgBase64, null, basePersonIris, null, false, false);
    }

    /**
     * 新增虹膜进行1:N校验是否本人
     * 
     * @param destIris
     */
    private void irisSearchNValidate(BasePersonIris destIris) {
        if (null == destIris) {
            throw new CustomException(MessageUtils.message("base.person.iris.1n.iris.isnull"));
        }
        List<IrisSearchResult> searchNResult =
            personIrisRecogLogicService.irisSearchN(destIris.getFeature(), StringUtils.EMPTY, 1, null);
        if (CollectionUtils.isNotEmpty(searchNResult)
            && !searchNResult.get(0).getUserId().equals(destIris.getUniqueId())) {
            // 查询虹膜是否存在
            String featureId = searchNResult.get(0).getFeatureId();
            String irisId = featureId.substring(featureId.indexOf("_") + 1);
            BasePersonIris iris = basePersonIrisMapper.selectBasePersonIrisById(irisId);
            if (null != iris && DictConstants.Status.ENABLE.equals(iris.getStatus())) {
                log.error(
                    "The iris storage 1-N check failed, the search result:[uniqueId:{},irisId:{}],The iris already exists and is not the person [uniqueId:{}]",
                    searchNResult.get(0).getUserId(), irisId, destIris.getUniqueId());
                // TODO 删除上传的虹膜图片
                throw new CustomException(MessageUtils.message("base.person.iris.1n.check.failed.not.me"));
            } else {
                log.error(
                    "[FOX_MINISEARCH] Iris 1-N search results are wrong, the iris does not exist (or the status is invalid) in the relation library.[uniqueId:{}, irisId:{}]",
                    searchNResult.get(0).getUserId(), irisId);
            }
        }
    }

    /**
     * 校验人员是否有虹膜数据
     * 
     * @param personId
     * @return
     */
    @Override
    public boolean checkPersonHasIris(String personId) {
        int count = basePersonIrisMapper.countEnabledIrisByPersonId(personId);
        return count > 0;
    }

}
