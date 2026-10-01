package cn.eyecool.server.handler;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eyecool.abis.callmicroservice.IOcrDetectService;
import com.eyecool.abis.callmicroservice.common.ocr.BankCard;
import com.eyecool.abis.callmicroservice.common.ocr.BusinessLicense;
import com.eyecool.abis.callmicroservice.common.ocr.DriverLicense;
import com.eyecool.abis.callmicroservice.common.ocr.DrivingLicense;
import com.eyecool.abis.callmicroservice.common.ocr.HongAndMacaoPass;
import com.eyecool.abis.callmicroservice.common.ocr.IdentityCard;
import com.eyecool.abis.callmicroservice.common.ocr.Passport;

import cn.eyecool.ocr.OcrConstants;
import cn.eyecool.ocr.domain.OcrBankCardLog;
import cn.eyecool.ocr.domain.OcrBusiLicLog;
import cn.eyecool.ocr.domain.OcrDriverLicLog;
import cn.eyecool.ocr.domain.OcrDrivingLicLog;
import cn.eyecool.ocr.domain.OcrHkMacPassLog;
import cn.eyecool.ocr.domain.OcrIdcardBackLog;
import cn.eyecool.ocr.domain.OcrIdcardFrontLog;
import cn.eyecool.ocr.domain.OcrPassportLog;
import cn.eyecool.ocr.service.IOcrBankCardLogService;
import cn.eyecool.ocr.service.IOcrBusiLicLogService;
import cn.eyecool.ocr.service.IOcrDriverLicLogService;
import cn.eyecool.ocr.service.IOcrDrivingLicLogService;
import cn.eyecool.ocr.service.IOcrHkMacPassLogService;
import cn.eyecool.ocr.service.IOcrIdcardBackLogService;
import cn.eyecool.ocr.service.IOcrIdcardFrontLogService;
import cn.eyecool.ocr.service.IOcrPassportLogService;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUploadUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.system.service.ISysConfigService;

/**
 * Description Package cn.eyecool.server.handler
 *
 * @author sunhuayu Date on 2020/12/17
 */
@Component
public class OcrLogHttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcrLogHttpHandler.class);
    @Autowired
    private IOcrDetectService ocrDetectService;
    @Autowired
    private IOcrBankCardLogService ocrBankCardLogService;
    @Autowired
    private IOcrBusiLicLogService ocrBusinessLicLogService;
    @Autowired
    private IOcrDriverLicLogService ocrDriverLicLogService;
    @Autowired
    private IOcrDrivingLicLogService ocrDrivingLicLogService;
    @Autowired
    private IOcrHkMacPassLogService ocrHkMacPassLogService;
    @Autowired
    private IOcrIdcardFrontLogService ocrIdcardFrontLogService;
    @Autowired
    private IOcrIdcardBackLogService ocrIdcardBackLogService;
    @Autowired
    private IOcrPassportLogService ocrPassportLogService;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IChannelInfoService channelInfoService;

    /**
     * OCR识别身份证正面信息
     *
     * @param bizContent
     * @param ocrType
     * @return
     */
    private AjaxResult getIdcardFrontAttr(String bizContent, String ocrType) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrIdcardFrontLog idcardFrontLog = new OcrIdcardFrontLog();
        idcardFrontLog.setChannelCode(channelCode);
        idcardFrontLog.setReceivedTime(new Date());
        idcardFrontLog.setSceneImageUrl(sceneImagePath);
        idcardFrontLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            IdentityCard identityCard;
            if (ocrType.equals(OcrConstants.OCR_TYPE.OCR_LOG_IDCARD_TEMP)) {
                identityCard = ocrDetectService.detectIdCardTemp(imageBase64);
            } else {
                identityCard = ocrDetectService.detectIdCardFront(imageBase64);
            }
            if (StringUtils.isNotBlank(identityCard.getHeadImage())) {
                String orcHeadImagePath = this.uploadImage(Boolean.TRUE, null, identityCard.getHeadImage(),
                    DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
                idcardFrontLog.setIdcardHeadimage(orcHeadImagePath);
            }
            idcardFrontLog.setIdcardAddress(identityCard.getAddress());
            idcardFrontLog.setReceivedSeq(identityCard.getAbisReqNo());
            idcardFrontLog.setIdcardBirth(identityCard.getBirth());
            idcardFrontLog.setIdcardEthnicity(identityCard.getEthnicity());
            idcardFrontLog.setIdcardName(identityCard.getName());
            idcardFrontLog.setIdcardNumber(identityCard.getIdNumber());
            idcardFrontLog.setIdcardGender(identityCard.getGender());
            idcardFrontLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(identityCard);
        } catch (Exception e) {
            idcardFrontLog.setReceivedSeq(IdWorker.getNextStringId());
            idcardFrontLog.setResult(DictConstants.BioResult.NOTPASS);
            LOGGER.error(e.getMessage(), e);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrIdcardFrontLogService.insertOcrIdcardFrontLog(idcardFrontLog);
        }
    }

    /**
     * OCR识别身份证反面信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getIdcardBackAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrIdcardBackLog idcardBackLog = new OcrIdcardBackLog();
        idcardBackLog.setChannelCode(channelCode);
        idcardBackLog.setReceivedTime(new Date());
        idcardBackLog.setSceneImageUrl(sceneImagePath);
        idcardBackLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            IdentityCard identityCard = ocrDetectService.detectIdCardBack(imageBase64);
            idcardBackLog.setIdcardDateIssue(identityCard.getIssueDate());
            idcardBackLog.setIdcardAuthorityIssue(identityCard.getIssuingAuthority());
            idcardBackLog.setIdcardLimit(identityCard.getLimits());
            idcardBackLog.setIdcardExpiryDate(identityCard.getExpiratDate());
            idcardBackLog.setReceivedSeq(identityCard.getAbisReqNo());
            idcardBackLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(identityCard);
        } catch (Exception e) {
            e.printStackTrace();
            idcardBackLog.setReceivedSeq(IdWorker.getNextStringId());
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrIdcardBackLogService.insertOcrIdcardBackLog(idcardBackLog);
        }
    }

    /**
     * OCR识别银行卡信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getBankCardAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrBankCardLog bankCardLog = new OcrBankCardLog();
        bankCardLog.setChannelCode(channelCode);
        bankCardLog.setReceivedTime(new Date());
        bankCardLog.setSceneImageUrl(sceneImagePath);
        bankCardLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            BankCard bankCard = ocrDetectService.detectBankCard(imageBase64);
            bankCardLog.setReceivedSeq(bankCard.getAbisReqNo());
            bankCardLog.setBankCardExpiryDate(bankCard.getExpiryDate());
            bankCardLog.setBankCardBankName(bankCard.getBankName());
            bankCardLog.setBankCardBankCode(bankCard.getBankCode());
            bankCardLog.setBankCardName(bankCard.getCardName());
            bankCardLog.setBankCardType(bankCard.getCardType());
            bankCardLog.setBankCardNumber(bankCard.getCardNumber());
            bankCardLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(bankCard);
        } catch (Exception e) {
            e.printStackTrace();
            bankCardLog.setReceivedSeq(IdWorker.getNextStringId());
            bankCardLog.setReceivedSeq(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrBankCardLogService.insertOcrBankCardLog(bankCardLog);
        }
    }

    /**
     * OCR识别营业执照信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getBusinessLicAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrBusiLicLog businessLicLog = new OcrBusiLicLog();
        businessLicLog.setChannelCode(channelCode);
        businessLicLog.setReceivedTime(new Date());
        businessLicLog.setSceneImageUrl(sceneImagePath);
        businessLicLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            BusinessLicense businessLicense = ocrDetectService.detectBusinessLicense(imageBase64);
            if (StringUtils.isNotEmpty(businessLicense.getqRCode())) {
                String orcQRCode = this.uploadImage(Boolean.TRUE, null, businessLicense.getqRCode(),
                    DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
                businessLicLog.setBusiLicQrCode(orcQRCode);
            }
            businessLicLog.setReceivedSeq(businessLicense.getAbisReqNo());
            businessLicLog.setBusiLicRegisteredNo(businessLicense.getRegisteredNO());
            businessLicLog.setBusiLicOriginationNo(businessLicense.getOriginationNO());
            businessLicLog.setBusiLicTaxNo(businessLicense.getTaxNO());
            businessLicLog.setBusiLicSocialInsuranceNo(businessLicense.getSocialInsuranceNO());
            businessLicLog.setBusiLicStatisticNo(businessLicense.getStatisticNO());
            businessLicLog.setBusiLicName(businessLicense.getName());
            businessLicLog.setBusiLicType(businessLicense.getType());
            businessLicLog.setBusiLicAddress(businessLicense.getAddress());
            businessLicLog.setBusiLicOwner(businessLicense.getOwner());
            businessLicLog.setBusiLicForm(businessLicense.getForm());
            businessLicLog.setBusiLicRegisteredCapital(businessLicense.getRegisteredCapital());
            businessLicLog.setBusiLicRegistryDate(businessLicense.getRegistryDate());
            businessLicLog.setBusiLicExpiryDate(businessLicense.getExpireDate());
            businessLicLog.setBusiLicScope(businessLicense.getScope());
            businessLicLog.setBusiLicIssureAuthority(businessLicense.getIssuingAuthority());
            businessLicLog.setBusiLicIssureDate(businessLicense.getIssuingDate());
            businessLicLog.setBusiLicQrCode(businessLicense.getqRCode());
            businessLicLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(businessLicense);
        } catch (Exception e) {
            e.printStackTrace();
            businessLicLog.setReceivedSeq(IdWorker.getNextStringId());
            businessLicLog.setResult(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrBusinessLicLogService.insertOcrBusiLicLog(businessLicLog);
        }
    }

    /**
     * OCR识别驾驶证信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getDriverLicAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrDriverLicLog ocrDriverLicLog = new OcrDriverLicLog();
        ocrDriverLicLog.setChannelCode(channelCode);
        ocrDriverLicLog.setReceivedTime(new Date());
        ocrDriverLicLog.setSceneImageUrl(sceneImagePath);
        ocrDriverLicLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            DriverLicense driverLicense = ocrDetectService.detectDriverLicense(imageBase64);
            ocrDriverLicLog.setReceivedSeq(driverLicense.getAbisReqNo());
            ocrDriverLicLog.setDriverLicNumber(driverLicense.getIdNumber());
            ocrDriverLicLog.setDriverLicAddress(driverLicense.getAddress());
            String headImagePath = this.uploadImage(Boolean.TRUE, null, driverLicense.getHeadImage(),
                DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
            ocrDriverLicLog.setDriverLicHeadImage(headImagePath);
            ocrDriverLicLog.setDriverLicBirth(driverLicense.getBirth());
            ocrDriverLicLog.setDriverLicGender(driverLicense.getGender());
            ocrDriverLicLog.setDriverLicName(driverLicense.getName());
            ocrDriverLicLog.setDriverLicDriverType(driverLicense.getDriverType());
            ocrDriverLicLog.setDriverLicFirstIssue(driverLicense.getFirstIssue());
            ocrDriverLicLog.setDriverLicValidFrom(driverLicense.getValidFrom());
            ocrDriverLicLog.setDriverLicValidFor(driverLicense.getValidFor());
            ocrDriverLicLog.setDriverLicExpiryDate(driverLicense.getExpiratDate());
            ocrDriverLicLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(driverLicense);
        } catch (Exception e) {
            e.printStackTrace();
            ocrDriverLicLog.setReceivedSeq(IdWorker.getNextStringId());
            ocrDriverLicLog.setResult(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrDriverLicLogService.insertOcrDriverLicLog(ocrDriverLicLog);
        }
    }

    /**
     * OCR识别行驶证信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getDrivingLicAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrDrivingLicLog ocrDrivingLicLog = new OcrDrivingLicLog();
        ocrDrivingLicLog.setChannelCode(channelCode);
        ocrDrivingLicLog.setReceivedTime(new Date());
        ocrDrivingLicLog.setSceneImageName(parseObject.getString("imageName"));
        ocrDrivingLicLog.setSceneImageUrl(sceneImagePath);
        try {
            DrivingLicense drivingLicense = ocrDetectService.detectDrivingLicense(imageBase64);
            ocrDrivingLicLog.setDrivingLicModel(drivingLicense.getModel());
            ocrDrivingLicLog.setDrivingLicAddress(drivingLicense.getAddress());
            ocrDrivingLicLog.setDrivingLicCarNumber(drivingLicense.getCarNumber());
            ocrDrivingLicLog.setDrivingLicVehicleType(drivingLicense.getVehicleType());
            ocrDrivingLicLog.setDrivingLicIssueDate(drivingLicense.getIssueDate());
            ocrDrivingLicLog.setDrivingLicEngineNo(drivingLicense.getEngineNO());
            ocrDrivingLicLog.setDrivingLicVin(drivingLicense.getVin());
            ocrDrivingLicLog.setDrivingLicRegisterDate(drivingLicense.getRegisterDate());
            ocrDrivingLicLog.setDrivingLicUseType(drivingLicense.getUseType());
            ocrDrivingLicLog.setDrivingLicOwner(drivingLicense.getOwner());
            ocrDrivingLicLog.setReceivedSeq(drivingLicense.getAbisReqNo());
            ocrDrivingLicLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(drivingLicense);
        } catch (Exception e) {
            e.printStackTrace();
            ocrDrivingLicLog.setReceivedSeq(IdWorker.getNextStringId());
            ocrDrivingLicLog.setResult(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrDrivingLicLogService.insertOcrDrivingLicLog(ocrDrivingLicLog);
        }
    }

    /**
     * OCR识别港澳通行证信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getHkMacPassAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrHkMacPassLog ocrHkMacPassLog = new OcrHkMacPassLog();
        ocrHkMacPassLog.setChannelCode(channelCode);
        ocrHkMacPassLog.setReceivedTime(new Date());
        ocrHkMacPassLog.setSceneImageUrl(sceneImagePath);
        ocrHkMacPassLog.setSceneImageName(parseObject.getString("imageName"));
        try {
            HongAndMacaoPass hongAndMacaoPass = ocrDetectService.detectHongAndMacaoPass(imageBase64);
            ocrHkMacPassLog.setHkMacPassType(hongAndMacaoPass.getType());
            ocrHkMacPassLog.setHkMacPassMrzNumber(hongAndMacaoPass.getMrzNumber());
            ocrHkMacPassLog.setHkMacPassNationalName(hongAndMacaoPass.getNationalName());
            ocrHkMacPassLog.setHkMacPassEnglishName(hongAndMacaoPass.getEnglishName());
            ocrHkMacPassLog.setHkMacPassGender(hongAndMacaoPass.getGrader());
            ocrHkMacPassLog.setHkMacPassBirth(hongAndMacaoPass.getBirth());
            ocrHkMacPassLog.setHkMacPassExpiryDate(hongAndMacaoPass.getExpiryDate());
            ocrHkMacPassLog.setHkMacPassIssueCountry(hongAndMacaoPass.getIssuingCountry());
            ocrHkMacPassLog.setHkMacPassEnglishSurName(hongAndMacaoPass.getEnglishSurName());
            ocrHkMacPassLog.setReceivedSeq(hongAndMacaoPass.getAbisReqNo());
            ocrHkMacPassLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(hongAndMacaoPass);
        } catch (Exception e) {
            e.printStackTrace();
            ocrHkMacPassLog.setReceivedSeq(IdWorker.getNextStringId());
            ocrHkMacPassLog.setResult(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrHkMacPassLogService.insertOcrHkMacPassLog(ocrHkMacPassLog);
        }
    }

    /**
     * OCR识别护照信息
     *
     * @param bizContent
     * @return
     */
    private AjaxResult getPassportAttr(String bizContent) {
        JSONObject parseObject = JSON.parseObject(bizContent);
        String channelCode = parseObject.getString("channelCode");
        String imageBase64 = parseObject.getString("imageBase64");

        String sceneImagePath =
            this.uploadImage(Boolean.TRUE, null, imageBase64, DictConstants.HttpInterfaceTransCode.OCR_ATTR_DETECT_LOG);
        OcrPassportLog ocrPassportLog = new OcrPassportLog();
        ocrPassportLog.setChannelCode(channelCode);
        ocrPassportLog.setReceivedTime(new Date());
        ocrPassportLog.setSceneImageName(parseObject.getString("imageName"));
        ocrPassportLog.setSceneImageUrl(sceneImagePath);
        try {
            Passport passport = ocrDetectService.detectPassport(imageBase64);
            ocrPassportLog.setPassportMrzFir(passport.getMrzFir());
            ocrPassportLog.setPassportMrzSec(passport.getMrzSec());
            ocrPassportLog.setPassportNationalityCode(passport.getNationalityCode());
            ocrPassportLog.setPassportNumber(passport.getNumber());
            ocrPassportLog.setPassportBirthPlace(passport.getBirthPlace());
            ocrPassportLog.setPassportIssuePlace(passport.getIssuePlace());
            ocrPassportLog.setPassportIssueDate(passport.getIssueDate());
            ocrPassportLog.setPassportRfidMrz(passport.getRfidMrz());
            ocrPassportLog.setPassportOcrMrz(passport.getOcrMrz());
            ocrPassportLog.setPassportBirthPlacePinyin(passport.getBirthPlacePinyin());
            ocrPassportLog.setPassportIssuePlacePinyin(passport.getIssuePlacePinyin());
            ocrPassportLog.setPassportIdNumber(passport.getIdNumber());
            ocrPassportLog.setPassportOcrNationalName(passport.getNationalName());
            ocrPassportLog.setPassportOcrGender(passport.getGrader());
            ocrPassportLog.setPassportOcrNationalityCode(passport.getNationalityCode());
            ocrPassportLog.setPassportOcrBirthDate(passport.getBirth());
            ocrPassportLog.setPassportOcrExpiryDate(passport.getExpiryDate());
            ocrPassportLog.setPassportOcrAuthority(passport.getOcrAuthority());
            ocrPassportLog.setPassportNationalSurname(passport.getNationalSurname());
            ocrPassportLog.setPassportNationalGivenName(passport.getNationalGivenName());
            ocrPassportLog.setPassportHeight(passport.getHeight());
            ocrPassportLog.setReceivedSeq(passport.getAbisReqNo());
            ocrPassportLog.setResult(DictConstants.BioResult.PASS);
            return HttpAjaxResult.httpSuccess(passport);
        } catch (Exception e) {
            e.printStackTrace();
            ocrPassportLog.setReceivedSeq(IdWorker.getNextStringId());
            ocrPassportLog.setResult(DictConstants.BioResult.NOTPASS);
            return HttpAjaxResult.businessError(e.getMessage());
        } finally {
            ocrPassportLogService.insertOcrPassportLog(ocrPassportLog);
        }
    }

    /**
     * OCR相关图片上传工具类
     *
     * @param encrypted
     * @param fileName
     * @param base64
     * @param type
     * @return
     */
    public String uploadImage(Boolean encrypted, String fileName, String base64, String type) {
        String baseDir = sysConfigService.selectConfigByKey(OcrConstants.OCR_LOG_IMAGE_DIR_KEY);
        if (StringUtils.isEmpty(base64)) {
            throw new CustomException(MessageUtils.message("ocr.handler.base84.empty"));
        }
        if (encrypted) {
            base64 = PlatformCryptUtils.encryptImageBase64(base64);
        }
        if (StringUtils.isBlank(baseDir)) {
            throw new CustomException( MessageUtils.message("ocr.handler.folder.need", OcrConstants.OCR_LOG_IMAGE_DIR_KEY));
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        if (StringUtils.isNotEmpty(type)) {
            baseDir = baseDir + type + File.separator;
        }
        try {
            if (StringUtils.isBlank(fileName)) {
                String imageFileExtendName = PlatformFileUtils.getImageFileExtendName(base64);
                fileName = System.currentTimeMillis() + UUID.randomUUID().hashCode() + imageFileExtendName;
            }
            String filePathName = PlatformFileUploadUtils.upload(baseDir, fileName, base64);
            return filePathName;
        } catch (Exception e) {
            LOGGER.error("OCR image upload exception", e);
            throw new CustomException(MessageUtils.message("ocr.handler.image.upload.error"));
        }
    }

    public AjaxResult getOcrAttr(String bizContent) {
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOGGER.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String channelCode = parseObject.getString("channelCode");
        if (StringUtils.isNotEmpty(channelCode)) {
            ChannelInfo channelInfo = new ChannelInfo();
            channelInfo.setChannelCode(channelCode);
            List<ChannelInfo> channelInfos = channelInfoService.selectChannelInfoList(channelInfo);
            if (channelInfos == null || CollectionUtils.isEmpty(channelInfos)) {
                LOGGER.error("channel code {} The corresponding scene does not exist, please confirm.", channelCode);
                return HttpAjaxResult.businessDataValidError(MessageUtils.message("ocr.handler.privide.channelcode"));
            }
        }
        String ocrType = parseObject.getString("ocrType");
        if (StringUtils.isEmpty(ocrType)) {
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("ocr.handler.ocrtype.empty"));
        }
        String imageName = parseObject.getString("imageName");
        if (StringUtils.isBlank(imageName) || imageName.length() > 48) {
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("ocr.handler.image.max.limit"));
        }
        String imageBase64 = parseObject.getString("imageBase64");

        if (StringUtils.isEmpty(imageBase64)) {
            LOGGER.error("Please provide image BASE64 data");
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("ocr.handler.privide.imageb64"));
        }
        AjaxResult ajaxResult;
        switch (ocrType) {
            case OcrConstants.OCR_TYPE.OCR_LOG_IDCARD_FRONT:
                ajaxResult = this.getIdcardFrontAttr(bizContent, ocrType);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_IDCARD_TEMP:
                ajaxResult = this.getIdcardFrontAttr(bizContent, ocrType);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_IDCARD_BACK:
                ajaxResult = this.getIdcardBackAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_BANK_CARD:
                ajaxResult = this.getBankCardAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_BUSINESS_LIC:
                ajaxResult = this.getBusinessLicAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_DRIVER_LIC:
                ajaxResult = this.getDriverLicAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_DRIVING_LIC:
                ajaxResult = this.getDrivingLicAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_HK_MAC_PASS:
                ajaxResult = this.getHkMacPassAttr(bizContent);
                break;
            case OcrConstants.OCR_TYPE.OCR_LOG_PASSPORT:
                ajaxResult = this.getPassportAttr(bizContent);
                break;
            default:
                ajaxResult = HttpAjaxResult.httpError(MessageUtils.message("ocr.handler.privide.ocrtype"));
        }
        return ajaxResult;
    }
}
