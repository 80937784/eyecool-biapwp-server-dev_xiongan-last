package cn.eyecool.server.handler;

import cn.eyecool.basedata.domain.*;
import cn.eyecool.basedata.enums.MultiRegistOptionTypeEnum;
import cn.eyecool.basedata.service.*;
import cn.eyecool.basedata.vo.*;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.entity.SysDept;
import cn.eyecool.common.core.domain.entity.SysDictData;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.system.service.ISysDeptService;
import cn.eyecool.system.service.ISysDictTypeService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人员基本信息HTTP请求处理器
 *
 * @author admin
 * @date 2019年11月7日
 */
@Component
@Slf4j
public class BasePersonInfoHttpHandler {

    @Autowired
    private IBasePersonInfoService basePersonInfoService;
    @Autowired
    private IBasePersonFaceService basePersonFaceService;
    @Autowired
    private IBasePersonFingerService basePersonFingerService;
    @Autowired
    private IBasePersonIrisService basePersonIrisService;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private IBasePersonIrisFaceService basePersonIrisFaceService;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceInfoService deviceInfoService;

    /**
     * 新增人员信息
     *
     * @param bizContent
     * @return
     */
    public AjaxResult insertPersonInfo(String bizContent) {
        // json转换
        BasePersonPutInfo basePersonInfo = null;
        try {
            basePersonInfo = JSONObject.parseObject(bizContent, BasePersonPutInfo.class);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validatePersonInfoParam(basePersonInfo, true);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            basePersonInfoService.insertHttpBasePersonInfo(basePersonInfo);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("HTTP save personnel information failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("HTTP save personnel information failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 参数校验
     *
     * @param basePersonInfo
     * @param isAdd
     * @return
     */
    private AjaxResult validatePersonInfoParam(BasePersonPutInfo basePersonInfo, boolean isAdd) {
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = basePersonInfo.getUniqueId();
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 部门编码不为空时，长度不能大于48
        String deptCode = basePersonInfo.getDeptCode();
        if (StringUtils.isNotBlank(deptCode) && deptCode.length() > 48) {
            msg = MessageUtils.message("base.person.handler.deptcode.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = basePersonInfo.getReceivedSeq();
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 新增人员时姓名不能为空，人员姓名不为空时，长度不能大于60
        String name = basePersonInfo.getName();
        if (isAdd && StringUtils.isBlank(name)) {
            msg = MessageUtils.message("base.person.handler.person.name.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        if (StringUtils.isNotBlank(name) && name.length() > 60) {
            msg = MessageUtils.message("base.person.handler.person.name.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 性别不为空时，长度为1，且必须是数字
        String sex = basePersonInfo.getSex();
        if (StringUtils.isNotBlank(sex) && (sex.length() != 1 || !LongValidator.getInstance().isValid(sex))) {
            msg = MessageUtils.message("base.person.handler.sex.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 手机号不为空时，长度必须为11，且符合手机号校验规则
        String phone = basePersonInfo.getPhone();
        if (StringUtils.isNotBlank(phone) && (phone.length() != 11 || !LongValidator.getInstance().isValid(phone))) {
            msg = MessageUtils.message("base.person.handler.phone.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 卡号不为空时，长度不能超过48
        String cardNo = basePersonInfo.getCardNo();
        if (StringUtils.isNotBlank(cardNo) && cardNo.length() > 48) {
            msg = MessageUtils.message("base.person.handler.cardno.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 账号不为空时，长度不能超过48
        String account = basePersonInfo.getAccount();
        if (StringUtils.isNotBlank(account) && account.length() > 48) {
            msg = MessageUtils.message("base.person.handler.account.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 邮箱不为空时，进行邮箱格式校验
        String email = basePersonInfo.getEmail();
        if (StringUtils.isNotBlank(email) && !EmailValidator.getInstance().isValid(email)) {
            msg = "邮箱[email]格式错误";
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员标记不为空时，只能是1,2,3
        String flag = basePersonInfo.getFlag();
        List<SysDictData> flagType = dictTypeService.selectDictDataByType(DictConstants.PERSON_FLAG_DICT_TYPE);
        List<String> flagList = flagType.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        if (StringUtils.isNotBlank(flag) && !flagList.contains(flag)) {
            msg = MessageUtils.message("base.person.handler.person.flag.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 指纹图片校验, 图片存在则手指编号不能为空
        List<BasePersonFingerPutInfo> fingerPutInfoList = basePersonInfo.getFingerPutInfoList();
        if (CollectionUtils.isNotEmpty(fingerPutInfoList)) {
            if (fingerPutInfoList.size() > 3) {
                msg = MessageUtils.message("base.person.handler.finger.upload.max");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            List<SysDictData> type = dictTypeService.selectDictDataByType(DictConstants.BIO_FINGER_NO_DICT_TYPE);
            List<String> fingerNoList = type.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
            for (BasePersonFingerPutInfo info : fingerPutInfoList) {
                String fingerNo = info.getFingerNo();
                if (StringUtils.isBlank(fingerNo) || !fingerNoList.contains(fingerNo)) {
                    msg = MessageUtils.message("base.person.handler.fingerno.invalid");
                    return HttpAjaxResult.businessDataValidError(msg);
                }
            }
        }
        BasePersonIrisPutInfo irisPutInfo = basePersonInfo.getIrisPutInfo();
        if (null != irisPutInfo) {
            if (StringUtils.isNotBlank(irisPutInfo.getFeature()) && StringUtils.isBlank(irisPutInfo.getImageBase64())) {
                msg = MessageUtils.message("base.person.handler.iris.feature.image.empty");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 修改人员信息
     *
     * @param bizContent
     * @return
     */
    public AjaxResult updatePersonInfo(String bizContent) {
        // json转换
        BasePersonPutInfo basePersonInfo = null;
        try {
            basePersonInfo = JSONObject.parseObject(bizContent, BasePersonPutInfo.class);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 参数校验
        AjaxResult ajaxResult = validatePersonInfoParam(basePersonInfo, false);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        try {
            basePersonInfoService.updateHttpBasePersonInfo(basePersonInfo);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("HTTP modification of personnel information failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("HTTP modification of personnel information failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 删除人员信息(逻辑删除)
     *
     * @param bizContent
     * @return
     */
    public AjaxResult deletePersonInfo(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = (String)parseObject.get("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            BasePersonInfo infoCondition = new BasePersonInfo();
            infoCondition.setUniqueId(uniqueId);
            infoCondition.setStatus(DictConstants.Status.ENABLE);
            List<BasePersonInfo> infoList = basePersonInfoService.selectBasePersonInfoList(infoCondition);
            if (CollectionUtils.isEmpty(infoList)) {
                return HttpAjaxResult
                    .businessError(MessageUtils.message("base.person.handler.person.not.exists", uniqueId));
            }
            basePersonInfoService.deleteBasePersonInfoById(infoList.get(0).getId());
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("HTTP deletion of personnel information failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("HTTP deletion of personnel information failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 查询人员基本信息
     *
     * @param bizContent
     * @return
     */
    public AjaxResult getBasePersonInfo(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 人员标识不能为空, 且长度不大于48
        String uniqueId = (String)parseObject.get("uniqueId");
        if (StringUtils.isBlank(uniqueId) || uniqueId.length() > 48) {
            msg = MessageUtils.message("base.person.handler.uniqueid.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = (String)parseObject.get("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 是否需要查询历史照
        String needHisImage = (String)parseObject.get("needHisImage");
        boolean isOnlyEnabled =
            StringUtils.isBlank(needHisImage) ? true : DictConstants.YesOrNoState.YES.equals(needHisImage);
        // 查询模态
        String bioType = (String)parseObject.get("bioType");
        // 不传入则返回全部模态生物数据
        List<String> bioTypeList = DictConstants.BioAttestType.bioAttestTypeList;
        if (StringUtils.isNotBlank(bioType)) {
            String[] bioTypeArray = Convert.toStrArray(bioType);
            List<String> list = Arrays.asList(bioTypeArray);
            boolean hasIllegalBioType =
                list.stream().anyMatch(it -> !DictConstants.BioAttestType.bioAttestTypeList.contains(it));
            if (hasIllegalBioType) {
                msg = MessageUtils.message("base.person.handler.biotype.invalid");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            bioTypeList = list;
        }
        try {
            BasePersonInfoVO personVO = selectBasePersonInfo(uniqueId, isOnlyEnabled, bioTypeList);
            return HttpAjaxResult.httpSuccess(personVO);
        } catch (CustomException e) {
            log.error("HTTP query for personnel information failed", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("HTTP query for personnel information failed", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 查询人员基础信息详情
     *
     * @param uniqueId
     * @param isOnlyEnabled
     * @return
     */
    protected BasePersonInfoVO selectBasePersonInfo(String uniqueId, boolean isOnlyEnabled, List<String> bioTypeList) {
        BasePersonInfo infoCondition = new BasePersonInfo();
        infoCondition.setUniqueId(uniqueId);
        infoCondition.setStatus(DictConstants.Status.ENABLE);
        List<BasePersonInfo> infoList = basePersonInfoService.selectBasePersonInfoList(infoCondition);
        if (CollectionUtils.isEmpty(infoList)) {
            throw new CustomException(MessageUtils.message("base.person.handler.person.not.exists", uniqueId));
        }
        BasePersonInfo info = infoList.get(0);
        BasePersonInfoVO personVO = new BasePersonInfoVO();
        BeanUtils.copyBeanProp(personVO, info);
        // 查询部门信息
        if (null != info.getDeptId()) {
            SysDept sysDept = deptService.selectDeptById(info.getDeptId());
            personVO.setDeptCode(sysDept.getDeptCode());
            personVO.setDeptName(sysDept.getDeptName());
        }
        boolean openFace = bioTypeList.contains(DictConstants.BioAttestType.FACE);
        boolean openFinger = bioTypeList.contains(DictConstants.BioAttestType.FINGER);
        boolean openIris = bioTypeList.contains(DictConstants.BioAttestType.IRIS);
        boolean openFaceIris = bioTypeList.contains(DictConstants.BioAttestType.FACE_IRIS);
        // 暂时没有开发指静脉业务
        // boolean openFvein = bioTypeList.contains(DictConstants.BioAttestType.FVEIN);
        handleSetBioInfo(info.getId(), personVO, isOnlyEnabled, openFace, openFinger, openIris, openFaceIris);
        return personVO;
    }

    /**
     * 处理人员生物特征信息数据查询
     *
     * @param personId
     * @param personVO
     * @param isOnlyEnabled
     * @param openFace
     * @param openFinger
     * @param openIris
     * @param busiOpenFaceIris
     */
    protected void handleSetBioInfo(String personId, BasePersonInfoVO personVO, boolean isOnlyEnabled, boolean openFace,
        boolean openFinger, boolean openIris, boolean busiOpenFaceIris) {
        // 查询人脸信息
        if (openFace) {
            BasePersonFace faceCondition = new BasePersonFace();
            faceCondition.setPersonId(personId);
            if (isOnlyEnabled) {
                faceCondition.setStatus(DictConstants.Status.ENABLE);
            }
            List<BasePersonFace> faceList = basePersonFaceService.selectBasePersonFaceList(faceCondition);
            if (CollectionUtils.isNotEmpty(faceList)) {
                List<BasePersonFaceVO> faceVOList = faceList.stream().map(face -> {
                    BasePersonFaceVO faceVO = new BasePersonFaceVO();
                    BeanUtils.copyBeanProp(faceVO, face);
                    String imageBase64 = PlatformFileUtils.getImageBase64(face.getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(face.getEncrypted())) {
                        imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                    }
                    faceVO.setImageBase64(imageBase64);
                    faceVO.setSuffix(face.getImageUrl().substring(face.getImageUrl().lastIndexOf(".")));
                    return faceVO;
                }).collect(Collectors.toList());
                personVO.setBasePersonFaceVOList(faceVOList);
            }
        }
        // 查询指纹信息
        if (openFinger) {
            BasePersonFinger fingerCondition = new BasePersonFinger();
            fingerCondition.setPersonId(personId);
            if (isOnlyEnabled) {
                fingerCondition.setStatus(DictConstants.Status.ENABLE);
            }
            List<BasePersonFinger> fingerList = basePersonFingerService.selectBasePersonFingerList(fingerCondition);
            if (CollectionUtils.isNotEmpty(fingerList)) {
                List<BasePersonFingerVO> fingerVOList = fingerList.stream().map(it -> {
                    BasePersonFingerVO fingerVO = new BasePersonFingerVO();
                    BeanUtils.copyBeanProp(fingerVO, it);
                    String imageBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                        imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                    }
                    fingerVO.setImageBase64(imageBase64);
                    fingerVO.setSuffix(it.getImageUrl().substring(it.getImageUrl().lastIndexOf(".")));
                    return fingerVO;
                }).collect(Collectors.toList());
                personVO.setBasePersonFingerVOList(fingerVOList);
            }
        }
        // 查询虹膜信息
        if (openIris) {
            BasePersonIris irisCondition = new BasePersonIris();
            irisCondition.setPersonId(personId);
            if (isOnlyEnabled) {
                irisCondition.setStatus(DictConstants.Status.ENABLE);
            }
            List<BasePersonIris> irisList = basePersonIrisService.selectBasePersonIrisList(irisCondition);
            if (CollectionUtils.isNotEmpty(irisList)) {
                List<BasePersonIrisVO> irisVOList = irisList.stream().map(it -> {
                    BasePersonIrisVO irisVO = new BasePersonIrisVO();
                    BeanUtils.copyBeanProp(irisVO, it);
                    String imageBase64 = PlatformFileUtils.getImageBase64(it.getImageUrl());
                    if (DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                        imageBase64 = PlatformCryptUtils.decryptImageBase64(imageBase64);
                    }
                    irisVO.setImageBase64(imageBase64);
                    irisVO.setSuffix(it.getImageUrl().substring(it.getImageUrl().lastIndexOf(".")));
                    return irisVO;
                }).collect(Collectors.toList());
                personVO.setBasePersonIrisVOList(irisVOList);
            }
        }
        // 查询多模态信息
        if (busiOpenFaceIris) {
            BasePersonIrisFace irisFaceCondition = new BasePersonIrisFace();
            irisFaceCondition.setPersonId(personId);
            if (isOnlyEnabled) {
                irisFaceCondition.setStatus(DictConstants.Status.ENABLE);
            }
            List<BasePersonIrisFace> irisFaceList =
                basePersonIrisFaceService.selectBasePersonIrisFaceList(irisFaceCondition);
            if (CollectionUtils.isNotEmpty(irisFaceList)) {
                List<BasePersonFaceIrisVO> irisFaceVOList = irisFaceList.stream().map(it -> {
                    BasePersonFaceIrisVO irisFaceVO = new BasePersonFaceIrisVO();
                    BeanUtils.copyBeanProp(irisFaceVO, it);
                    if (null != it.getValidityDate()) {
                        irisFaceVO.setExpireTime(
                            DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, it.getValidityDate()));
                    }
                    if (StringUtils.isNotBlank(it.getFaceImageUrl())) {
                        String stringBase64 = PlatformFileUtils.getImageBase64(it.getFaceImageUrl());
                        if (DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                            stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                        }
                        irisFaceVO.setFaceImageBase64(stringBase64);
                    }
                    if (StringUtils.isNotBlank(it.getIrisImageUrl())) {
                        String stringBase64 = PlatformFileUtils.getImageBase64(it.getIrisImageUrl());
                        if (DictConstants.Encrypted.ENABLE.equals(it.getEncrypted())) {
                            stringBase64 = PlatformCryptUtils.decryptImageBase64(stringBase64);
                        }
                        irisFaceVO.setIrisImageBase64(stringBase64);
                    }
                    return irisFaceVO;
                }).collect(Collectors.toList());
                personVO.setBasePersonFaceIrisVOList(irisFaceVOList);
            }
        }
    }

    /**
     * 虹膜人脸多模态注册
     *
     * @param bizContent
     * @return
     */
    public AjaxResult irisFaceRegister(String bizContent) {
        IrisFaceRegister irisFaceRegister;
        try {
            irisFaceRegister = JSONObject.parseObject(bizContent, IrisFaceRegister.class);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 注册参数校验
        AjaxResult ajaxResult = validateIrisFaceRegister(irisFaceRegister);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        String deviceCode = irisFaceRegister.getDeviceCode();
        // 如果有设备编码，那么是设备日志回传，根据设备编码查询并记录场景编码和子场景信息，防止设备传输有误
        DeviceInfo deviceInfo = getClientByDeviceNo(deviceCode);
        if (null == deviceInfo) {
            String msg = MessageUtils.message("base.person.handler.device.not.exists", deviceCode);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备租户和授权appKey所属租户是否一致
        if (!validateDeviceTenant(deviceInfo)) {
            String msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备是否绑定场景
        if (StringUtils.isBlank(deviceInfo.getChannelCode())) {
            String msg = MessageUtils.message("base.person.handler.device.not.bind.scene", deviceCode);
            return HttpAjaxResult.businessError(msg);
        }
        // 验证设备是否绑定子场景
        if (StringUtils.isBlank(deviceInfo.getSubtreasuryCode())) {
            String msg = MessageUtils.message("base.person.handler.device.not.bind.subscene", deviceCode);
            return HttpAjaxResult.businessError(msg);
        }
        try {
            basePersonIrisFaceService
                .irisFaceRegister(irisFaceRegister, deviceInfo.getChannelCode(), deviceInfo.getPrimarySubCode());
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("Failed to register iris face multimodal information", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to register iris face multimodal information", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸虹膜多模态注册参数校验
     *
     * @param irisFaceRegister
     * @return
     */
    private AjaxResult validateIrisFaceRegister(IrisFaceRegister irisFaceRegister) {
        if (StringUtils.isEmpty(irisFaceRegister.getDeviceCode())) {
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.devicecdoe.empty"));
        }
        String uniqueId = irisFaceRegister.getUniqueId();
        if (StringUtils.isEmpty(uniqueId) || uniqueId.length() > 48) {
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.uniqueid.max.length.limit"));
        }
        if (StringUtils.isEmpty(irisFaceRegister.getName())) {
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.person.name.empty"));
        }
        if (StringUtils.isEmpty(irisFaceRegister.getOptionType())) {
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.optiontype.empty"));
        }
        MultiRegistOptionTypeEnum[] types = MultiRegistOptionTypeEnum.values();
        boolean typeValid = false;
        for (MultiRegistOptionTypeEnum type : types) {
            if (type.value().equals(irisFaceRegister.getOptionType())) {
                typeValid = true;
                break;
            }
        }
        if (!typeValid) {
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.optiontype.invalid"));
        }
        if (!MultiRegistOptionTypeEnum.DELETE.value().equals(irisFaceRegister.getOptionType()) && StringUtils
            .isEmpty(irisFaceRegister.getFaceBase64Img()) && StringUtils
            .isEmpty(irisFaceRegister.getFaceFeatureBase64())) {
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.face.feature.image.all.empty"));
        }
        if (!MultiRegistOptionTypeEnum.DELETE.value().equals(irisFaceRegister.getOptionType()) && StringUtils
            .isEmpty(irisFaceRegister.getIrisBase64Img()) && StringUtils
            .isEmpty(irisFaceRegister.getIrisFeatureBase64())) {
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.iris.feature.image.all.empty"));
        }
        return HttpAjaxResult.httpSuccess();
    }

    /**
     * 查询设备信息
     *
     * @param deviceNo
     * @return
     */
    private DeviceInfo getClientByDeviceNo(String deviceNo) {
        // 清空在此之前设备之的TenantContextHolder上下文信息，不携带租户隔离查询设备信息
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
        // 重新设置租户信息
        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(tenantId);
        }
        if (CollectionUtils.isEmpty(list)) {
            log.error("Device [{}] does not exist", deviceNo);
            return null;
        }
        return list.get(0);
    }

    /**
     * 校验设备所属租户和接口授权租户是否一致
     *
     * @param clientDeviceInfo
     * @return
     */
    private boolean validateDeviceTenant(DeviceInfo clientDeviceInfo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        String deviceNo = clientDeviceInfo.getDeviceNo();
        log.info("Device [{}] belongs to tenant [{}]", deviceNo, clientDeviceInfo.getTenantId());
        if (StringUtils.isBlank(clientDeviceInfo.getTenantId())) {
            log.error("Device [{}] does not maintain tenant information", deviceNo);
            return false;
        }
        // 验证appKey所属租户和设备所属租户是否一致
        String tenantId = TenantContextHolder.getTenantId();
        if (!clientDeviceInfo.getTenantId().equals(tenantId)) {
            log.error(
                "The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]",
                deviceNo, clientDeviceInfo.getTenantId(), tenantId);
            return false;
        }
        return true;
    }

    /**
     * 修改人员标记
     *
     * @param bizContent
     * @return
     */
    public AjaxResult updatePersonFlag(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        String msg = null;
        // 业务流水号不能为空
        String receivedSeq = (String)parseObject.get("receivedSeq");
        if (StringUtils.isBlank(receivedSeq)) {
            msg = MessageUtils.message("base.person.handler.receivedseq.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员标识不能为空
        String uniqueIds = (String)parseObject.get("uniqueIds");
        if (StringUtils.isBlank(uniqueIds)) {
            msg = MessageUtils.message("base.person.handler.uniqueIds.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 人员标记
        String flag = (String)parseObject.get("flag");
        if (StringUtils.isBlank(flag)) {
            msg = MessageUtils.message("base.person.handler.person.flag.empty");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        List<SysDictData> flagType = dictTypeService.selectDictDataByType(DictConstants.PERSON_FLAG_DICT_TYPE);
        List<String> flagList = flagType.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        if (!flagList.contains(flag)) {
            msg = MessageUtils.message("base.person.handler.person.flag.invalid");
            return HttpAjaxResult.businessDataValidError(msg);
        }
        try {
            basePersonInfoService.updatePersonFlagByUniqueIds(uniqueIds, flag);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("Failed to update person tag", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update person tag", e);
            return HttpAjaxResult.httpError();
        }

    }

    /**
     * 人脸注册
     *
     * @param bizContent
     * @return
     */
    public AjaxResult faceRegister(String bizContent) {
        FaceRegister faceRegister;
        try {
            faceRegister = JSONObject.parseObject(bizContent, FaceRegister.class);
        } catch (Exception e) {
            log.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult
                .businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }
        // 注册参数校验
        AjaxResult ajaxResult = validateFaceRegister(faceRegister);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        String deviceCode = faceRegister.getDeviceCode();
        String channelCode = faceRegister.getChannelCode();
        String primarySubCode = null;
        if (StringUtils.isNotBlank(deviceCode)) {
            // 如果有设备编码，那么是设备日志回传，根据设备编码查询并记录场景编码和子场景信息，防止设备传输有误
            DeviceInfo deviceInfo = getClientByDeviceNo(deviceCode);
            if (null == deviceInfo) {
                String msg = MessageUtils.message("base.person.handler.device.not.exists", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备租户和授权appKey所属租户是否一致
            if (!validateDeviceTenant(deviceInfo)) {
                String msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备是否绑定场景
            if (StringUtils.isBlank(deviceInfo.getChannelCode())) {
                String msg = MessageUtils.message("base.person.handler.device.not.bind.scene", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备是否绑定子场景
            if (StringUtils.isBlank(deviceInfo.getSubtreasuryCode())) {
                String msg = MessageUtils.message("base.person.handler.device.not.bind.subscene", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
            channelCode = deviceInfo.getChannelCode();
            primarySubCode = deviceInfo.getPrimarySubCode();
        }
        try {
            //进行人脸注册
            basePersonFaceService.faceRegister(faceRegister, channelCode, primarySubCode);
            return HttpAjaxResult.httpSuccess();
        } catch (CustomException e) {
            log.error("人脸信息注册失败", e);
            return HttpAjaxResult.businessError(e.getMessage());
        } catch (Exception e) {
            log.error("人脸信息注册失败", e);
            return HttpAjaxResult.httpError();
        }
    }

    /**
     * 人脸注册参数校验
     *
     * @param faceRegister
     * @return
     */
    private AjaxResult validateFaceRegister(FaceRegister faceRegister) {
        if (StringUtils.isEmpty(faceRegister.getDeviceCode()) && StringUtils.isBlank(faceRegister.getChannelCode())) {
            return HttpAjaxResult.businessDataValidError("设备编码[deviceCode]和[channelCode]不能都为空");
        }
        if (StringUtils.isEmpty(faceRegister.getUniqueId())) {
            return HttpAjaxResult.businessDataValidError("人员唯一编号[uniqueId]不能为空");
        }
        if (StringUtils.isEmpty(faceRegister.getName())) {
            return HttpAjaxResult.businessDataValidError("人员姓名[name]不能为空");
        }
        if (StringUtils.isEmpty(faceRegister.getOptionType())) {
            return HttpAjaxResult.businessDataValidError("操作类型[optionType]不能为空");
        }
        MultiRegistOptionTypeEnum[] types = MultiRegistOptionTypeEnum.values();
        boolean typeValid = false;
        for (MultiRegistOptionTypeEnum type : types) {
            if (type.value().equals(faceRegister.getOptionType())) {
                typeValid = true;
                break;
            }
        }
        if (!typeValid) {
            return HttpAjaxResult.businessDataValidError("操作类型[optionType]不合法");
        }
        if (!MultiRegistOptionTypeEnum.DELETE.value().equals(faceRegister.getOptionType()) && StringUtils
            .isEmpty(faceRegister.getFaceBase64Img())) {
            return HttpAjaxResult.businessDataValidError("人脸照片不能为空");
        }
        return HttpAjaxResult.httpSuccess();
    }

}
