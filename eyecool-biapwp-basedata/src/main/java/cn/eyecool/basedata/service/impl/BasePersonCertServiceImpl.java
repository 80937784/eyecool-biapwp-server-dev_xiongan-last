package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.basedata.domain.BasePersonCert;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.mapper.BasePersonCertMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.service.IBasePersonCertService;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.exception.file.FileNameLengthLimitExceededException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileModel;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.file.ZipEntryFileModel;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 人员证件信息Service业务层处理
 * 
 * @author mawj
 * @date 2021-01-27
 */
@Service
@Slf4j
public class BasePersonCertServiceImpl implements IBasePersonCertService {

    @Autowired
    private BasePersonCertMapper basePersonCertMapper;
    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;

    /**
     * 查询人员证件信息
     * 
     * @param id 人员证件信息ID
     * @return 人员证件信息
     */
    @Override
    public BasePersonCert selectBasePersonCertById(String id) {
        return basePersonCertMapper.selectBasePersonCertById(id);
    }

    /**
     * 查询人员证件信息列表
     * 
     * @param basePersonCert 人员证件信息
     * @return 人员证件信息
     */
    @Override
    public List<BasePersonCert> selectBasePersonCertList(BasePersonCert basePersonCert) {
        return basePersonCertMapper.selectBasePersonCertList(basePersonCert);
    }

    /**
     * 新增人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBasePersonCert(BasePersonCert basePersonCert) {
        // 校验人员是否存在
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonCert.getUniqueId());
        if (null == basePersonInfo) {
            throw new CustomException(MessageUtils.message("base.person.cert.user.not.exists"));
        }
        // 查询证件信息是否已存在
        BasePersonCert certInfo = getCertInfo(basePersonCert.getUniqueId(), basePersonCert.getCertType());
        if (null != certInfo) {
            throw new CustomException(MessageUtils.message("base.person.cert.document.exists"));
        }
        basePersonCert.setId(IdWorker.getNextStringId());
        basePersonCert.setCreateTime(DateUtils.getNowDate());
        try {
            basePersonCert.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("SecurityUtils.getUsername() ,The interface call does not have a logged  user,error[{}] ",
                    e.toString());
            }
        }
        int row = basePersonCertMapper.insertBasePersonCert(uploadBasePersonCertImg(basePersonCert, true));
        // 发布人员信息改变事件
        personChangeEventPublishService.personCertUpdatePublish(basePersonInfo.getId(), basePersonCert.getUniqueId(),
            null);
        return row;
    }

    /**
     * 修改人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateBasePersonCert(BasePersonCert basePersonCert) {
        basePersonCert.setUpdateTime(DateUtils.getNowDate());
        try {
            basePersonCert.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("SecurityUtils.getUsername() ,The interface call does not have a logged  user. error[{}] ",
                    e.toString());
            }
        }
        int row = basePersonCertMapper.updateBasePersonCert(uploadBasePersonCertImg(basePersonCert, false));
        // 发布人员信息改变事件
        BasePersonInfo basePersonInfo = getEnabledPersonInfo(basePersonCert.getUniqueId());
        if (null != basePersonInfo) {
            personChangeEventPublishService.personCertUpdatePublish(basePersonInfo.getId(),
                basePersonCert.getUniqueId(), null);
        }
        return row;
    }

    /**
     * 批量删除人员证件信息
     * 
     * @param ids 需要删除的人员证件信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonCertByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonCertById(id);
        }
        return result;
    }

    /**
     * 删除人员证件信息信息
     * 
     * @param id 人员证件信息ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteBasePersonCertById(String id) {
        BasePersonCert cert = basePersonCertMapper.selectBasePersonCertById(id);
        List<String> imagePaths = new ArrayList<>();
        if (null != cert) {
            if (StringUtils.isNotBlank(cert.getCertImg())) {
                imagePaths.add(cert.getCertImg());
            }
            if (StringUtils.isNotBlank(cert.getEnterschoolImg())) {
                imagePaths.add(cert.getEnterschoolImg());
            }
            if (StringUtils.isNotBlank(cert.getInschoolImg())) {
                imagePaths.add(cert.getInschoolImg());
            }
            if (StringUtils.isNotBlank(cert.getGraduateImg())) {
                imagePaths.add(cert.getGraduateImg());
            }
            int result = basePersonCertMapper.deleteBasePersonCertById(id);
            this.asyncDelImages(imagePaths);
            personChangeEventPublishService.personCertUpdatePublish(null, cert.getUniqueId(), null);
            return result;
        }
        return 0;
    }

    /**
     * 保存批量导入
     * 
     * @param excelFile
     * @param zipFile
     * @param photoType
     * @param certType
     * @param updateSupport
     * @return
     * @throws IOException
     * @throws Exception
     */
    @Override
    public String saveImportData(MultipartFile excelFile, MultipartFile zipFile, String photoType, String certType,
        Boolean updateSupport) throws IOException, Exception {
        InputStream excelIStream = null == excelFile ? null : excelFile.getInputStream();
        InputStream zipIStream = null == zipFile ? null : zipFile.getInputStream();
        return saveImportData(excelIStream, zipIStream, photoType, certType, updateSupport);
    }

    /**
     * 保存批量导入
     * 
     * @param excelIStream
     * @param zipIStream
     * @param photoType
     * @param certType
     * @param updateSupport
     * @return
     * @throws IOException
     * @throws Exception
     */
    @Override
    public String saveImportData(InputStream excelIStream, InputStream zipIStream, String photoType, String certType,
        Boolean updateSupport) throws IOException, Exception {
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        Map<String, Object> importResMap = Collections.emptyMap();
        Map<String, Object> uploadImgResMap = Collections.emptyMap();
        // 导入数据
        if (excelIStream != null) {
            ExcelUtil<BasePersonCert> util = new ExcelUtil<BasePersonCert>(BasePersonCert.class);
            List<BasePersonCert> certList = util.importExcel(excelIStream);
            // 执行导入数据
            importResMap = importCertData(certList, updateSupport);
            Integer importFailNum = (Integer)importResMap.get("failureNum");
            if (null != importFailNum && importFailNum > 0) {
                failureMsg.append(importResMap.get("failureMsg")).append("<br/>");
            }
        }
        // 上传并更新照片
        if (zipIStream != null) {
            uploadImgResMap = saveImportCertImages(zipIStream, photoType, certType);
            Integer uploadFailNum = (Integer)uploadImgResMap.get("failureNum");
            if (null != uploadFailNum && uploadFailNum > 0) {
                failureMsg.append(uploadImgResMap.get("failureMsg"));
            }
        }
        if (failureMsg.length() > 0) {
            throw new CustomException(failureMsg.toString());
        }
        String importSuccMsg = (String)importResMap.get("successMsg");
        String uploadSuccMsg = (String)uploadImgResMap.get("successMsg");
        if (StringUtils.isNotBlank(importSuccMsg)) {
            successMsg = successMsg.append(StringUtils.nvl(importSuccMsg, "")).append("<br/>");
        }
        String succmsg = successMsg.append(StringUtils.nvl(uploadSuccMsg, "")).toString();
        return succmsg;
    }

    /**
     * 下载用户证件图片
     */
    @Override
    public String downloadImages(BasePersonCert basePersonCert, String photoType) {
        List<BasePersonCert> list = basePersonCertMapper.selectBasePersonCertList(basePersonCert);
        // zip包的名称
        String postfix = "_" + MessageUtils.message("base.person.cert.id.photo") + ".zip";
        String downloadFilename = System.currentTimeMillis() + new Random().nextInt() + postfix;
        List<ZipEntryFileModel> entryList = new ArrayList<>();
        list.stream().forEach(item -> {
            if (DictConstants.CertPhotoType.CERT_PHOTO.equals(photoType) && StringUtils.isNotBlank(item.getCertImg())) {
                String suffix = item.getCertImg().substring(item.getCertImg().lastIndexOf("."));
                entryList.add(new ZipEntryFileModel(new File(item.getCertImg()), item.getUniqueId() + suffix,
                    DictConstants.Encrypted.ENABLE.equals(item.getEncrypted())));
            } else if (DictConstants.CertPhotoType.ENTER_SCHOOL_PHOTO.equals(photoType)
                && StringUtils.isNotBlank(item.getEnterschoolImg())) {
                String suffix = item.getEnterschoolImg().substring(item.getEnterschoolImg().lastIndexOf("."));
                entryList.add(new ZipEntryFileModel(new File(item.getEnterschoolImg()), item.getUniqueId() + suffix,
                    DictConstants.Encrypted.ENABLE.equals(item.getEncrypted())));
            } else if (DictConstants.CertPhotoType.IN_SCHOOL_PHOTO.equals(photoType)
                && StringUtils.isNotBlank(item.getInschoolImg())) {
                String suffix = item.getInschoolImg().substring(item.getInschoolImg().lastIndexOf("."));
                entryList.add(new ZipEntryFileModel(new File(item.getInschoolImg()), item.getUniqueId() + suffix,
                    DictConstants.Encrypted.ENABLE.equals(item.getEncrypted())));
            } else if (DictConstants.CertPhotoType.GRADUATE_PHOTO.equals(photoType)
                && StringUtils.isNotBlank(item.getGraduateImg())) {
                String suffix = item.getGraduateImg().substring(item.getGraduateImg().lastIndexOf("."));
                entryList.add(new ZipEntryFileModel(new File(item.getGraduateImg()), item.getUniqueId() + suffix,
                    DictConstants.Encrypted.ENABLE.equals(item.getEncrypted())));
            }
        });
        String zipFilePath = getAbsoluteFile(downloadFilename);
        PlatformFileUtils.zipFiles(entryList, new File(zipFilePath));
        return downloadFilename;
    }

    /**
     * 导入证件照数据
     * 
     * @param basePersonCertList 用户证件信息数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    private Map<String, Object> importCertData(List<BasePersonCert> basePersonCertList, Boolean isUpdateSupport) {
        isUpdateSupport = null == isUpdateSupport ? true : isUpdateSupport;
        if (CollectionUtils.isEmpty(basePersonCertList)) {
            log.error("The imported document data is empty!");
            throw new CustomException(MessageUtils.message("base.person.cert.imported.document.empty"));
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (BasePersonCert basePersonCert : basePersonCertList) {
            try {
                // 验证是否存在本用户证件照信息
                boolean existsFlag = false;
                BasePersonCert cert = new BasePersonCert();
                cert.setUniqueId(basePersonCert.getUniqueId());
                cert.setCertType(basePersonCert.getCertType());
                List<BasePersonCert> certList = basePersonCertMapper.selectBasePersonCertList(cert);
                existsFlag = CollectionUtils.isNotEmpty(certList);
                String loginName = null;
                try {
                    loginName = SecurityUtils.getUsername();
                } catch (Exception e) {
                }
                Date now = new Date();
                if (!existsFlag) {// 不存在, 直接入库保存
                    basePersonCert.setId(IdWorker.getNextStringId());
                    basePersonCert.setCreateBy(loginName);
                    basePersonCert.setCreateTime(now);
                    basePersonCert.setEncrypted(DictConstants.Encrypted.ENABLE);// 默认加密
                    basePersonCertMapper.insertBasePersonCert(basePersonCert);
                    personChangeEventPublishService.personCertUpdatePublish(null, basePersonCert.getUniqueId(), null);
                    successNum++;
                } else if (isUpdateSupport) {
                    basePersonCert.setUpdateBy(loginName);
                    basePersonCert.setUpdateTime(now);
                    basePersonCert.setId(certList.get(0).getId());
                    basePersonCertMapper.updateBasePersonCert(basePersonCert);
                    personChangeEventPublishService.personCertUpdatePublish(null, basePersonCert.getUniqueId(), null);
                    successNum++;
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + MessageUtils
                        .message("base.person.cert.import.document.exists", basePersonCert.getUniqueId()));
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum
                    + MessageUtils.message("base.person.cert.import.failed", basePersonCert.getUniqueId());
                failureMsg.append(msg);
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            String msg = MessageUtils.message("base.person.cert.import.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("base.person.cert.import.success.sumary", successNum);
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
     * 导入保存上传照片文件
     * 
     * @param zipFile
     * @param photoType
     * @return
     */
    private Map<String, Object> saveImportCertImages(InputStream zipFileIStream, String photoType, String certType) {
        if (null == zipFileIStream)
            return Collections.emptyMap();
        // 解压缩
        List<FileModel> unzipFiles = PlatformFileUtils.unzip(zipFileIStream);
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        // 旧图片路径集合,用于异步删除旧的图片文件
        List<String> oldImgPaths = new ArrayList<>();
        String baseDir = sysConfigService.selectConfigByKey(SysConfigConstants.BASEDATA_CERT_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {

            throw new CustomException(
                MessageUtils.message("base.person.cert.upload.folder.need", SysConfigConstants.BASEDATA_CERT_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        for (FileModel fileModel : unzipFiles) {
            String originalFilename = fileModel.getFileName();
            // 文件名（唯一标识）
            String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            // 查询此人在数据库中存在与否(fileName是以用户唯一标识命名的)
            boolean existsFlag = false;
            BasePersonCert cert = new BasePersonCert();
            cert.setUniqueId(fileName);
            cert.setCertType(certType);
            List<BasePersonCert> certList = basePersonCertMapper.selectBasePersonCertList(cert);
            existsFlag = CollectionUtils.isNotEmpty(certList);
            if (!existsFlag) {
                failureNum++;
                String msg = MessageUtils.message("base.person.cert.upload.failed.document.not.exist", fileName);
                failureMsg.append("<br/>" + failureNum + msg);
                continue;
            }
            BasePersonCert basePersonCert = certList.get(0);
            try {
                String importImageBase64 = PlatformFileUtils.getImageBase64(fileModel.getFileInputstream());
                boolean isEncrypted = DictConstants.Encrypted.ENABLE.equals(basePersonCert.getEncrypted());
                if (isEncrypted) {
                    importImageBase64 = PlatformCryptUtils.encryptImageBase64(importImageBase64);
                }
                String filePathName =
                    PlatformFileUploadUtils.upload(baseDir, fileModel.getFileName(), importImageBase64);
                String oldImgPath = "";// 旧照片的存储路径
                if (DictConstants.CertPhotoType.CERT_PHOTO.equals(photoType)) {// 证件照片
                    oldImgPath = basePersonCert.getCertImg();
                    basePersonCert.setCertImg(filePathName);
                } else if (DictConstants.CertPhotoType.ENTER_SCHOOL_PHOTO.equals(photoType)) {// 入学照片
                    oldImgPath = basePersonCert.getEnterschoolImg();
                    basePersonCert.setEnterschoolImg(filePathName);
                } else if (DictConstants.CertPhotoType.IN_SCHOOL_PHOTO.equals(photoType)) {// 在校照片
                    oldImgPath = basePersonCert.getInschoolImg();
                    basePersonCert.setInschoolImg(filePathName);
                } else if (DictConstants.CertPhotoType.GRADUATE_PHOTO.equals(photoType)) {// 毕业照片
                    oldImgPath = basePersonCert.getGraduateImg();
                    basePersonCert.setGraduateImg(filePathName);
                } else {
                    continue;
                }
                basePersonCertMapper.updateBasePersonCert(basePersonCert);
                oldImgPaths.add(oldImgPath);
                successNum++;
            } catch (FileNameLengthLimitExceededException | IOException e) {
                log.error("Passport upload failed,uniqueId:{}, photoType:{}", fileName, photoType, e);
                failureNum++;
                String msg = MessageUtils.message("base.person.cert.upload.photo.failed", fileName);
                failureMsg.append("<br/>" + failureNum + msg);
            }
        }
        this.asyncDelImages(oldImgPaths);
        if (failureNum > 0) {
            String msg = MessageUtils.message("base.person.cert.upload.failed.sumary", failureNum);
            failureMsg.insert(0, msg);
            log.info(failureMsg.toString());
        } else {
            String msg = MessageUtils.message("base.person.cert.upload.success.sumary", successNum);
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
     * 查询证件信息
     * 
     * @param uniqueId 唯一标识
     * @param certType 证件类型
     * @return
     */
    private BasePersonCert getCertInfo(String uniqueId, String certType) {
        BasePersonCert certCondition = new BasePersonCert();
        certCondition.setUniqueId(uniqueId);
        certCondition.setCertType(certType);
        List<BasePersonCert> certList = basePersonCertMapper.selectBasePersonCertList(certCondition);
        return CollectionUtils.isEmpty(certList) ? null : certList.get(0);
    }

    /**
     * 上传证件照
     * 
     * @param basePersonCert 证件信息
     * @param isInsert 是否是新增证件
     * @return
     */
    private BasePersonCert uploadBasePersonCertImg(BasePersonCert basePersonCert, boolean isInsert) {
        String imgBase64 = basePersonCert.getImgBase64();
        if (StringUtils.isBlank(imgBase64)) {
            return basePersonCert;
        }
        // 上传证件照
        String baseDir = sysConfigService.selectConfigByKey(SysConfigConstants.BASEDATA_CERT_DIR_KEY);
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException(
                MessageUtils.message("base.person.cert.upload.folder.need", SysConfigConstants.BASEDATA_CERT_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        // 默认加密
        if (StringUtils.isBlank(basePersonCert.getEncrypted())) {
            basePersonCert.setEncrypted(DictConstants.Encrypted.ENABLE);// 是否加密，默认加密
        }
        try {
            // 拓展名的获取需要在加密之前获取
            String extName = PlatformFileUtils.getImageFileExtendName(imgBase64);
            // 上传人脸图片
            boolean encrypted = DictConstants.Encrypted.ENABLE.equals(basePersonCert.getEncrypted());
            if (encrypted) {
                imgBase64 = PlatformCryptUtils.encryptImageBase64(imgBase64);
            }
            String filePathName =
                PlatformFileUploadUtils.upload(baseDir, IdWorker.getNextStringId() + extName, imgBase64);
            basePersonCert.setCertImg(filePathName);
        } catch (IOException e) {
            log.error("ID photo upload failed，uniqueId:[{}]", basePersonCert.getUniqueId(), e);
            throw new CustomException(
                MessageUtils.message("base.person.cert.upload.photo.failed", basePersonCert.getUniqueId()));
        }
        return basePersonCert;
    }

    /**
     * 异步删除无用照片文件
     * 
     * @param srcImgPaths
     */
    private void asyncDelImages(List<String> srcImgPaths) {
        CompletableFuture.runAsync(() -> {
            srcImgPaths.stream().forEach(item -> {
                PlatformFileUtils.deleteFile(item);
            });
        });
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
}
