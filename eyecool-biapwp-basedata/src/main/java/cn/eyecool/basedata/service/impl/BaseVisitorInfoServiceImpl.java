package cn.eyecool.basedata.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.event.PersonChangeEventPublishService;
import cn.eyecool.basedata.mapper.BasePersonFaceMapper;
import cn.eyecool.basedata.mapper.BasePersonInfoMapper;
import cn.eyecool.basedata.service.IBaseVisitorInfoService;
import cn.eyecool.basedata.vo.BaseVisitorInfoVO;
import cn.eyecool.common.annotation.DataScope;
import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.SecurityUtils;
import cn.eyecool.common.utils.file.FileUtils;
import cn.eyecool.common.utils.qrcode.QRCodeUtil;
import cn.eyecool.common.utils.uuid.UUID;

/**
 * 人员基础信息Service业务层处理
 * 
 * @author mawj
 * @date 2021-01-27
 */
@Service
public class BaseVisitorInfoServiceImpl implements IBaseVisitorInfoService {

    @Autowired
    private BasePersonInfoMapper basePersonInfoMapper;
    @Autowired
    private PersonChangeEventPublishService personChangeEventPublishService;
    @Autowired
    private BasePersonFaceMapper basePersonFaceMapper;
    @Autowired
    private TenantProperties tenantProperties;
    @Value("${visit.front.address}")
    private String frontAddress;

    /**
     * 查询访客基础信息列表
     *
     * @param baseVisitorInfoVO 访客基础信息
     * @return 人员基础信息
     */
    @Override
    @DataScope(deptAlias = "info")
    public List<BasePersonInfo> selectBaseVisitorInfoList(BaseVisitorInfoVO baseVisitorInfoVO) {
        return basePersonInfoMapper.selectBaseVisitorInfoList(baseVisitorInfoVO);
    }

    @Override
    @Transactional
    public int deleteBasePersonInfoByIds(String[] ids) {
        int result = 0;
        for (String id : ids) {
            result += deleteBasePersonInfoById(id);
        }
        return result;
    }

    /**
     * 删除人员基础信息信息
     *
     * @param id 人员基础信息ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBasePersonInfoById(String id) {
        // 删除人脸信息
        deleteBasePersonFaceByPersonId(id);
        // 删除访客基本信息
        BasePersonInfo basePersonInfo = new BasePersonInfo();
        basePersonInfo.setId(id);
        basePersonInfo.setStatus(DictConstants.Status.DISABLE);
        basePersonInfo.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonInfo.setUpdateBy(loginName);
        int result = basePersonInfoMapper.updateBasePersonInfo(basePersonInfo);
        // 发布人员信息改变事件
        BasePersonInfo info = basePersonInfoMapper.selectBasePersonInfoById(id);
        personChangeEventPublishService.personDelPublish(id, info.getUniqueId(), null);
        return result;
    }

    @Override
    public void downloadQrCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String destPath = EyecoolConfig.getDownloadPath() + UUID.randomUUID() + ".jpg";
        // 查询是否开启多租户，开启了的话就要在二维码中加上该参数,不能直接加上，要加密一下
        if (tenantProperties.getEnabled()) {
            String tenantId = TenantContextHolder.getTenantId();
            String encryptTenantId = new String(Base64.getEncoder().encode(AESUtils.encryptAES(tenantId).getBytes()));
            frontAddress += "?key=" + encryptTenantId;
        }
        try {
            QRCodeUtil.encode(frontAddress, null, destPath, true);
        } catch (Exception e) {
            throw new CustomException(MessageUtils.message("base.visitor.info.qrcode.error", e.getMessage()));
        }

        String filename = "qrCode.jpg";
        byte[] data = FileUtils.readFileToByteArray(new File(destPath));
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition",
            "attachment; filename=" + FileUtils.setFileDownloadHeader(request, filename));
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 根据人员ID删除人脸信息(逻辑删除)
     *
     * @param personId
     * @return
     */
    private int deleteBasePersonFaceByPersonId(String personId) {
        BasePersonFace basePersonFace = new BasePersonFace();
        basePersonFace.setPersonId(personId);
        basePersonFace.setStatus(DictConstants.Status.DISABLE);
        basePersonFace.setUpdateTime(DateUtils.getNowDate());
        String loginName = null;
        try {
            loginName = SecurityUtils.getUsername();
        } catch (Exception e) {
        }
        basePersonFace.setUpdateBy(loginName);
        return basePersonFaceMapper.updateBasePersonFaceByPersonId(basePersonFace);
    }

}
