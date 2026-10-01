package cn.eyecool.server.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;

import cn.eyecool.basedata.domain.BasePersonCert;
import cn.eyecool.basedata.service.IBasePersonCertService;
import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.bean.BeanUtils;
import cn.eyecool.common.utils.sql.SqlUtil;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.scene.domain.BasePersonLiveUpdateInfo;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;
import cn.eyecool.scene.trade.service.IChannelBusiCommonHttpService;
import cn.eyecool.scene.trade.vo.BasePersonLiveUpdateVO;

/**
 * 人员实时更新HTTP处理器
 * 
 * @author mawj
 * @date 2021/04/27
 */
@Component
public class PersonLiveUpdateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PersonLiveUpdateHandler.class);

    @Autowired
    private IChannelBusiCommonHttpService channelBusiCommonHttpService;
    @Autowired
    private IChannelInfoService channelInfoService;
    @Autowired
    private BasePersonInfoHttpHandler basePersonInfoHttpHandler;
    @Autowired
    private TenantProperties tenantProperties;
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;
    @Autowired
    private IBasePersonCertService basePersonCertService;

    /**
     * 实时更新人员信息接口
     * 
     * @param bizContent
     * @return
     */
    public AjaxResult liveUpdateBasePersonInfo(String bizContent) {
        // json转换
        JSONObject parseObject = null;
        try {
            parseObject = JSON.parseObject(bizContent);
        } catch (Exception e) {
            LOG.error("The request parameter collection [bizContent] parameter is malformed", e);
            return HttpAjaxResult.businessDataValidError(MessageUtils.message("base.person.handler.request.param.format.wrong"));
        }

        String msg = null;
        // 业务流水号不能为空, 且长度不大于48
        String receivedSeq = (String)parseObject.get("receivedSeq");
        if (StringUtils.isBlank(receivedSeq) || receivedSeq.length() > 48) {
            msg = MessageUtils.message("base.person.handler.receivedseq.max.length.limit");
            LOG.error(msg);
            return HttpAjaxResult.businessDataValidError(msg);
        }
        // 设备信息处理
        String deviceNo = (String)parseObject.get("deviceNo");
        // 是否是设备拉取数据进行实时同步操作
        boolean isDevicePullData = StringUtils.isNotBlank(deviceNo);
        // 子场景人员更新标识流水码
        String sbusiUpdateSeriaNumStr = (String)parseObject.get("sbusiUpdateSeriaNum");
        Long sbusiUpdateSeriaNum = null;
        // 设备绑定子场景编码数组
        String[] subtreasuryIds = null;
        // 设备子场景主库编码
        String primarySubId = null;
        // 设备所属场景
        String channelCode = null;
        // 设备拉取全量数据标志
        boolean isDevicePullAllData = false;
        if (isDevicePullData) {
            DeviceInfo deviceInfo = getDeviceInfoByDeviceNo(deviceNo);
            if (null == deviceInfo) {
                msg  =MessageUtils.message("bio.trade.handler.device.not.exists", deviceNo);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            // 验证设备租户和授权appKey所属租户是否一致
            if (!validateDeviceTenant(deviceInfo)) {
                msg  =MessageUtils.message("base.person.handler.device.tenant.error", deviceNo);
                return HttpAjaxResult.businessError(msg);
            }
            // 设备所属场景必须存在
            channelCode = deviceInfo.getChannelCode();

            // 设备子场景人员更新标识流水码在设备拉取数据时不能为空，且必须是整数
            if (StringUtils.isBlank(sbusiUpdateSeriaNumStr)) {
                msg  =MessageUtils.message("person.lu.handler.sub.serial.empty");
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            try {
                sbusiUpdateSeriaNum = Long.valueOf(sbusiUpdateSeriaNumStr);
            } catch (Exception e) {
                LOG.error("Sub-scene personnel update serial code identifier [{}] is not an integer: [{}]", sbusiUpdateSeriaNumStr, e.getMessage());
                msg  =MessageUtils.message("person.lu.handler.sub.serial.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            if (StringUtils.isBlank(channelCode)) {
                msg  =MessageUtils.message("person.lu.handler.device.not.configure.scene",deviceNo);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            if (StringUtils.isBlank(deviceInfo.getSubtreasuryCode())) {
                msg  =MessageUtils.message("person.lu.handler.device.not.configure.subscene",deviceNo);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            String[] subtreasuryCodes = Convert.toStrArray(deviceInfo.getSubtreasuryCode());
            String primarySubCode = deviceInfo.getPrimarySubCode();
            // 查询子场景信息
            ChannelSubtreasuryInfo subCondition = new ChannelSubtreasuryInfo();
            subCondition.getParams().put("subtreasuryCodes", subtreasuryCodes);
            List<ChannelSubtreasuryInfo> subtreasuryInfoList =
                channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(subCondition);
            if (CollectionUtils.isEmpty(subtreasuryInfoList)) {
                msg =  MessageUtils.message("person.lu.handler.device.not.configure.subscene.not.exists", deviceNo);
                LOG.error(msg);
                return HttpAjaxResult.businessError(msg);
            }
            subtreasuryIds = subtreasuryInfoList.stream().map(ChannelSubtreasuryInfo::getId).toArray(String[]::new);
            primarySubId = subtreasuryInfoList.stream().filter(item -> item.getSubTreasuryCode().equals(primarySubCode))
                .map(ChannelSubtreasuryInfo::getId).findFirst()
                .orElseThrow(() -> new CustomException(MessageUtils.message("person.lu.handler.device.primary.lib.invalid",deviceNo)));
            // 判断设备本次拉取是否需要重新全量拉取
            isDevicePullAllData = Constants.STATUS_ONE.equals(deviceInfo.getPullAllFlag());
            if (isDevicePullAllData && sbusiUpdateSeriaNum.compareTo(0L) > 0) {
                Map<String, Object> resultMap = Maps.newHashMap();
                resultMap.put("total", Integer.MAX_VALUE);
                resultMap.put("list", Collections.emptyList());
                resultMap.put("sbusiUpdateSeriaNum", "0");
                resultMap.put("nextPullAllData", true);
                resultMap.put("primarySubId", primarySubId);
                LOG.info(
                    "Personnel update parameters in real time=> deviceNo:[{}], channelCode:[{}], sbusiUpdateSeriaNum:[{}];结果=> nextPullAllData:[{}], primarySubId:[{}], sbusiUpdateSeriaNum:[{}]",
                    deviceNo, channelCode, sbusiUpdateSeriaNum, true, primarySubId, 0);
                return HttpAjaxResult.httpSuccess(resultMap);
            }
        }

        // 场景更新时场景编码传入不能为空
        if (!isDevicePullData) {
            channelCode = (String)parseObject.get("channelCode");
            if (StringUtils.isBlank(channelCode)) {
                msg = MessageUtils.message("bio.trade.handler.scenecode.empty");
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }
        // 查询场景是否存在
        ChannelInfo channelCondition = new ChannelInfo();
        channelCondition.setChannelCode(channelCode);
        List<ChannelInfo> channelInfoList = channelInfoService.selectChannelInfoList(channelCondition);
        if (CollectionUtils.isEmpty(channelInfoList)) {
            msg = MessageUtils.message("channel.common.service.channelcode.not.exists", channelCode);
            LOG.error(msg);
            return HttpAjaxResult.businessError(msg);
        }

        // 场景人员更新标识流水码在场景更新时不能为空，且必须是整数
        String busiUpdateSeriaNumStr = (String)parseObject.get("busiUpdateSeriaNum");
        Long busiUpdateSeriaNum = null;
        if (!isDevicePullData) {
            if (StringUtils.isBlank(busiUpdateSeriaNumStr)) {
                msg  =MessageUtils.message("person.lu.handler.scene.serial.empty");
                LOG.error(msg);
                return HttpAjaxResult.businessDataValidError(msg);
            }
            try {
                busiUpdateSeriaNum = Long.valueOf(busiUpdateSeriaNumStr);
            } catch (Exception e) {
                LOG.error("The scene personnel update the serial code identifier [{}] is not an integer: {}", busiUpdateSeriaNumStr, e.getMessage());
                msg  =MessageUtils.message("person.lu.handler.scene.serial.format");
                return HttpAjaxResult.businessDataValidError(msg);
            }
        }

        // 是否需要查询历史照
        String needHisImage = (String)parseObject.get("needHisImage");
        boolean isOnlyEnabled =
            StringUtils.isBlank(needHisImage) ? true : DictConstants.YesOrNoState.NO.equals(needHisImage);
        // 是否需要证件信息
        String needCertInfo = (String)parseObject.get("needCertInfo");
        boolean isNeedCertInfo =
            StringUtils.isBlank(needCertInfo) ? false : DictConstants.YesOrNoState.YES.equals(needCertInfo);
        // 查询模态
        String bioType = (String)parseObject.get("bioType");
        // 不传入则不返回模态数据
        List<String> bioTypeList = Collections.emptyList();
        List<String> bioTypeParamList = Collections.emptyList();
        if (StringUtils.isBlank(bioType)) {
            LOG.warn("If the bioType is empty, there is no modal information that needs to be updated.tenantId:[{}],channelCode:[{}], deviceNo:[{}]",
                TenantContextHolder.getTenantId(), channelCode, deviceNo);
        } else {
            String[] bioTypeArray = Convert.toStrArray(bioType);
            bioTypeParamList = Arrays.asList(bioTypeArray);
            boolean hasIllegalBioType =
                bioTypeParamList.stream().anyMatch(it -> !DictConstants.BioAttestType.bioAttestTypeList.contains(it));
            if (hasIllegalBioType) {
                msg  =MessageUtils.message("base.person.handler.biotype.invalid");
                return HttpAjaxResult.businessDataValidError(msg);
            }
            bioTypeList = bioTypeParamList;
        }
        if (CollectionUtils.isNotEmpty(bioTypeParamList)
            && bioTypeParamList.contains(DictConstants.BioAttestType.FVEIN)) {
            msg  =MessageUtils.message("person.lu.handler.fvein.not.open");
            return HttpAjaxResult.businessError(msg);
        }
        ChannelInfo channelInfo = channelInfoList.get(0);
        boolean openFace = bioTypeList.contains(DictConstants.BioAttestType.FACE)
            && DictConstants.BioModeStatus.ENABLE.equals(channelInfo.getFaceMode());
        boolean openFinger = bioTypeList.contains(DictConstants.BioAttestType.FINGER)
            && DictConstants.BioModeStatus.ENABLE.equals(channelInfo.getFingerMode());
        boolean openIris = bioTypeList.contains(DictConstants.BioAttestType.IRIS)
            && DictConstants.BioModeStatus.ENABLE.equals(channelInfo.getIrisMode());
        boolean openFaceIris = bioTypeList.contains(DictConstants.BioAttestType.FACE_IRIS)
            && DictConstants.BioModeStatus.ENABLE.equals(channelInfo.getFaceIrisMode());
        // TODO 后期拓展指静脉验证
        if (CollectionUtils.isNotEmpty(bioTypeParamList) && !openFace && !openFinger && !openIris && !openFaceIris) {
            msg = MessageUtils.message("person.lu.handler.face.finger.iris.multimodal.not.open", channelCode);
            return HttpAjaxResult.businessError(msg);
        }

        int pageNum = 1;// 分页
        int pageSize = 10;// 分页数量， 一次更新10条
        String orderBy = null;
        if (isDevicePullData) {
            orderBy = SqlUtil.escapeOrderBySql("sbusi.update_seria_num asc");
        } else {
            orderBy = SqlUtil.escapeOrderBySql("busi.update_seria_num asc");
        }
        PageHelper.startPage(pageNum, pageSize, orderBy);
        Map<String, Object> conditionMap = Maps.newHashMap();
        conditionMap.put("channelId", channelInfo.getId());
        if (isDevicePullData) {
            conditionMap.put("sbusiUpdateSeriaNum", sbusiUpdateSeriaNum);
            conditionMap.put("subtreasuryIds", subtreasuryIds);
        } else {
            conditionMap.put("busiUpdateSeriaNum", busiUpdateSeriaNum);
        }
        List<BasePersonLiveUpdateInfo> personInfoList =
            channelBusiCommonHttpService.selectLiveUpdateBasePersonInfoList(conditionMap);
        long total = new PageInfo<BasePersonLiveUpdateInfo>(personInfoList).getTotal();// 数据总量
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("total", total);
        if (total == 0) {
            resultMap.put("list", Collections.emptyList());
            if (isDevicePullData) {
                resultMap.put("sbusiUpdateSeriaNum", sbusiUpdateSeriaNumStr);
                resultMap.put("nextPullAllData", false);
                resultMap.put("primarySubId", primarySubId);
            } else {
                resultMap.put("busiUpdateSeriaNum", busiUpdateSeriaNumStr);
            }
            LOG.info(
                "Personnel update parameters in real time => deviceNo:[{}],channelCode:[{}], busiUpdateSeriaNum:[{}], sbusiUpdateSeriaNum:[{}];结果=> total:[{}], nextPullAllData:[{}], primarySubId:[{}]",
                deviceNo, channelCode, busiUpdateSeriaNum, sbusiUpdateSeriaNum, total, false, primarySubId);
            return HttpAjaxResult.httpSuccess(resultMap);
        }
        String tenantId = TenantContextHolder.getTenantId();
        List<BasePersonLiveUpdateVO> list = personInfoList.parallelStream().map(info -> {
            TenantContextHolder.setTenantId(tenantId);
            BasePersonLiveUpdateVO vo = new BasePersonLiveUpdateVO();
            BeanUtils.copyBeanProp(vo, info);
            if (DictConstants.Status.DISABLE.equals(info.getStatus())) {
                return vo;
            }
            if (isNeedCertInfo) {
                // 查询证件信息，目前默认只返回身份证号信息
                Map<String, Object> idCardInfo = queryPersonIdCardInfo(info.getUniqueId());
                vo.setIdCardInfo(idCardInfo);
            }
            // 生物特征开通状态取为场景和场景人员的与逻辑，判断某个人是否可以拉取对应特征类型数据
            boolean busiOpenFace = DictConstants.BioModeStatus.ENABLE.equals(info.getFaceMode()) && openFace;
            boolean busiOpenFinger = DictConstants.BioModeStatus.ENABLE.equals(info.getFingerMode()) && openFinger;
            boolean busiOpenIris = DictConstants.BioModeStatus.ENABLE.equals(info.getIrisMode()) && openIris;
            boolean busiOpenFaceIris =
                DictConstants.BioModeStatus.ENABLE.equals(info.getFaceIrisMode()) && openFaceIris;
            // TODO 后期拓展指静脉
            basePersonInfoHttpHandler.handleSetBioInfo(info.getId(), vo, isOnlyEnabled, busiOpenFace, busiOpenFinger,
                busiOpenIris, busiOpenFaceIris);
            return vo;
        }).collect(Collectors.toList());
        BasePersonLiveUpdateInfo lastInfo = personInfoList.get(personInfoList.size() - 1);
        Long lastBusiUpdateSeriaNum = lastInfo.getBusiUpdateSeriaNum();
        Long lastSbusiUpdateSeriaNum = lastInfo.getSbusiUpdateSeriaNum();
        resultMap.put("list", list);
        if (isDevicePullData) {
            resultMap.put("sbusiUpdateSeriaNum", String.valueOf(lastSbusiUpdateSeriaNum));
            resultMap.put("nextPullAllData", false);
            resultMap.put("primarySubId", primarySubId);
        } else {
            resultMap.put("busiUpdateSeriaNum", String.valueOf(lastBusiUpdateSeriaNum));
        }
        String uniqueIds = personInfoList.stream().map(BasePersonLiveUpdateInfo::getUniqueId)
            .reduce((e1, e2) -> e1 + "," + e2).orElse(StringUtils.EMPTY);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Personnel real-time update equipment code: [{}], scene code: [{}], personnel real-time update parameters: [{}], personnel real-time update result: [{}]", deviceNo, channelCode,
                JSON.toJSONString(conditionMap), JSON.toJSONString(resultMap));
        } else {
            LOG.info(
                "Personnel update parameters in real time=> deviceNo:[{}], channelCode:[{}],busiUpdateSeriaNum:[{}], sbusiUpdateSeriaNum:[{}];result=> total:[{}], nextPullAllData:[{}], lastBusiUpdateSeriaNum:[{}], lastSbusiUpdateSeriaNum:[{}], uniqueIds:[{}]",
                deviceNo, channelCode, busiUpdateSeriaNum, sbusiUpdateSeriaNum, total, false, lastBusiUpdateSeriaNum,
                lastSbusiUpdateSeriaNum, uniqueIds);
        }
        // 更新设备全量拉取标志，下次不再全量拉取
        if (isDevicePullAllData) {
            deviceInfoService.cancelPullAllDataFlagByDeviceNo(deviceNo);
        }
        return HttpAjaxResult.httpSuccess(resultMap);
    }

    /**
     * 查询人员身份证信息
     * 
     * @param uniqueId
     * @return
     */
    private Map<String, Object> queryPersonIdCardInfo(String uniqueId) {
        // 从证件表查询身份证信息
        BasePersonCert certCondition = new BasePersonCert();
        certCondition.setUniqueId(uniqueId);
        certCondition.setCertType(DictConstants.CertType.ID_CARD);
        List<BasePersonCert> certList = basePersonCertService.selectBasePersonCertList(certCondition);
        if (CollectionUtils.isEmpty(certList)) {
            return null;
        }
        BasePersonCert cert = certList.get(0);
        Map<String, Object> map = Maps.newHashMap();
        map.put("certName", cert.getCertName());
        map.put("certNum", cert.getCertNum());
        return map;
    }

    /**
     * 查询设备信息
     * 
     * @param deviceNo
     * @return
     */
    private DeviceInfo getDeviceInfoByDeviceNo(String deviceNo) {
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
            LOG.error("Device [{}] does not exist", deviceNo);
            return null;
        }
        return list.get(0);
    }

    /**
     * 校验设备所属租户和接口授权租户是否一致
     *
     * @param deviceInfo
     * @return
     */
    private boolean validateDeviceTenant(DeviceInfo deviceInfo) {
        if (!tenantProperties.getEnabled()) {
            return true;
        }
        String deviceNo = deviceInfo.getDeviceNo();
        LOG.info("Device [{}] belongs to tenant [{}]", deviceNo, deviceInfo.getTenantId());
        if (StringUtils.isBlank(deviceInfo.getTenantId())) {
            LOG.error("Device [{}] does not maintain tenant information", deviceNo);
            return false;
        }
        // 验证appKey所属租户和设备所属租户是否一致
        String tenantId = TenantContextHolder.getTenantId();
        if (!deviceInfo.getTenantId().equals(tenantId)) {
            LOG.error("The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]", deviceNo, deviceInfo.getTenantId(), tenantId);
            return false;
        }
        return true;
    }
}
