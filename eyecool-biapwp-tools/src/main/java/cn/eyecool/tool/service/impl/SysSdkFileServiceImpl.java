package cn.eyecool.tool.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.SysConfigConstants;
import cn.eyecool.common.constant.UserConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tool.domain.SysSdkFile;
import cn.eyecool.tool.mapper.SysSdkFileMapper;
import cn.eyecool.tool.service.ISysSdkFileService;

/**
 * SDK文件上传Service业务层处理
 *
 * @author admin
 * @date 2021-03-31
 */
@Service
public class SysSdkFileServiceImpl implements ISysSdkFileService {
    @Autowired
    private SysSdkFileMapper sysSdkFileMapper;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private TenantProperties tenantProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(SysSdkFileServiceImpl.class);

    /**
     * 查询SDK文件上传
     *
     * @param id SDK文件上传ID
     * @return SDK文件上传
     */
    @Override
    public SysSdkFile selectSysSdkFileById(String id) {
        return sysSdkFileMapper.selectSysSdkFileById(id);
    }

    /**
     * 查询SDK文件上传列表
     *
     * @param sysSdkFile SDK文件上传
     * @return SDK文件上传
     */
    @Override
    public List<SysSdkFile> selectSysSdkFileList(SysSdkFile sysSdkFile) {
        return sysSdkFileMapper.selectSysSdkFileList(sysSdkFile);
    }

    /**
     * 新增SDK文件上传
     *
     * @param sysSdkFile SDK文件上传
     * @return 结果
     */
    @Override
    public int insertSysSdkFile(SysSdkFile sysSdkFile) {
        sysSdkFile.setCreateTime(DateUtils.getNowDate());
        return sysSdkFileMapper.insertSysSdkFile(sysSdkFile);
    }

    /**
     * 修改SDK文件上传
     *
     * @param sysSdkFile SDK文件上传
     * @return 结果
     */
    @Override
    public int updateSysSdkFile(SysSdkFile sysSdkFile) {
        String sdkType = sysSdkFile.getSdkType();
        Long sortedNo = sysSdkFile.getSortedNo();
        SysSdkFile condition = new SysSdkFile();
        condition.setSdkType(sdkType);
        condition.setSortedNo(sortedNo);
        List<SysSdkFile> sysSdkFiles = sysSdkFileMapper.selectSysSdkFileList(condition);
        if (CollectionUtils.isNotEmpty(sysSdkFiles)) {
            throw new CustomException("sdkType: [" + sdkType + "],sortNo: [" + sortedNo + "]已经存在，请更改。");
        } else {
            sysSdkFile.setUpdateTime(DateUtils.getNowDate());
            return sysSdkFileMapper.updateSysSdkFile(sysSdkFile);
        }
    }

    /**
     * 批量删除SDK文件上传
     *
     * @param ids 需要删除的SDK文件上传ID
     * @return 结果
     */
    @Override
    public int deleteSysSdkFileByIds(String[] ids) {
        for (String id : ids) {
            String filePath = sysSdkFileMapper.selectSysSdkFileById(id).getFilePath();
            FileUtils.deleteFile(filePath);
        }
        return sysSdkFileMapper.deleteSysSdkFileByIds(ids);
    }

    /**
     * 删除SDK文件上传信息
     *
     * @param id SDK文件上传ID
     * @return 结果
     */
    @Override
    public int deleteSysSdkFileById(String id) {
        return sysSdkFileMapper.deleteSysSdkFileById(id);
    }

    @Override
    public int addFileForm(MultipartFile sdkFile, SysSdkFile sysSdkFile) {
        String sdkType = sysSdkFile.getSdkType();
        Long sortedNo = sysSdkFile.getSortedNo();
        String id = sysSdkFile.getId();
        SysSdkFile condition = new SysSdkFile();
        condition.setSdkType(sdkType);
        condition.setSortedNo(sortedNo);
        //禅道bug：4325
        condition.setCreateBy(sysSdkFile.getCreateBy());
        List<SysSdkFile> sysSdkFiles = sysSdkFileMapper.selectSysSdkFileList(condition);
        if (StringUtils.isBlank(id)) {
            if (CollectionUtils.isNotEmpty(sysSdkFiles)) {
                throw new CustomException("sdkType: [" + sdkType + "],sortNo: [" + sortedNo + "]已经存在，请更改。");
            } else {
                String uid = IdWorker.getNextStringId();
                condition.setId(uid);
                boolean tenantDisabled = Boolean.FALSE.equals(tenantProperties.getEnabled());
                if (tenantDisabled) {
                    condition.setTenantId(TenantContextHolder.getTenantId() == null ? TenantContextHolder.getTenantId()
                            : UserConstants.SUPER_TENANT);
                }
            }
        } else {
            condition = sysSdkFileMapper.selectSysSdkFileById(id);
            if (!sdkType.equals(condition.getSdkType())) {
                if (CollectionUtils.isNotEmpty(sysSdkFiles)) {
                    throw new CustomException("sdkType: [" + sdkType + "],sortNo: [" + sortedNo + "]已经存在，请更改。");
                }
            }
            FileUtils.deleteFile(condition.getFilePath());
        }
        if (sdkFile != null) {
            try {
                condition.setMd5(DigestUtils.md5Hex(sdkFile.getBytes()));
                condition.setFileName(sdkFile.getOriginalFilename());
                String uploadPath = PlatformFileUploadUtils
                        .upload(configService.selectConfigByKey(SysConfigConstants.SYS_TOOL_SDK_FILE_DIR_KEY), sdkFile);
                condition.setFilePath(uploadPath);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new CustomException(MessageUtils.message("util.sdk.file.upload",e.getMessage()));
            }
        }
        if (StringUtils.isNoneBlank(id)) {
            condition.setUpdateTime(DateUtils.getNowDate());
            return sysSdkFileMapper.updateSysSdkFile(condition);
        } else {
            condition.setCreateTime(DateUtils.getNowDate());
            return sysSdkFileMapper.insertSysSdkFile(condition);
        }
    }

    @Override
    public void sdkDownload(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        SysSdkFile sysSdkFile = sysSdkFileMapper.selectSysSdkFileById(id);
        if (sysSdkFile == null) {
            throw new CustomException("file [" + id + "] is not exists！");
        }
        String filePath = sysSdkFile.getFilePath();
            String filename = sysSdkFile.getFileName();
            byte[] data = FileUtils.readFileToByteArray(new File(filePath));
            response.reset();
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", "attachment; filename=" + FileUtils.setFileDownloadHeader(request, filename));
            response.addHeader("Content-Length", "" + data.length);
            response.setContentType("application/octet-stream; charset=UTF-8");
            IOUtils.write(data, response.getOutputStream());
    }

    @Override
    public List<SysSdkFile> selectLastSdkUploads() {
        return sysSdkFileMapper.selectLastSdkUploads();
    }
}