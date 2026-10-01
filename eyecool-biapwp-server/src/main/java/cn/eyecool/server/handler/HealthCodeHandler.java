package cn.eyecool.server.handler;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.config.tenant.TenantProperties;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.healthcode.constant.HealthCodeConstants;
import cn.eyecool.healthcode.param.HealthCodeRequest;
import cn.eyecool.healthcode.service.IHealthCodeService;
import cn.eyecool.healthcode.service.impl.HealthCodeServiceImpl;

/**
 * 健康码查询控制类
 *
 * @author zfx
 * @since 2021/1/29 16:42
 **/
@Component
public class HealthCodeHandler {

    private static final Logger logger = LoggerFactory.getLogger(HealthCodeServiceImpl.class);
    private static final String REQUEST_PARAM_ERROR = MessageUtils.message("base.person.handler.request.param.format.wrong");

    @Autowired
    private IHealthCodeService healthCodeService;
    @Autowired
    private IDeviceInfoService deviceInfoService;
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * healthCodeSearch 健康码查询
     * 
     * @param bizContent 请求对象字符串
     * @return cn.eyecool.biapwp.common.core.domain.AjaxResult
     * @author zfx
     * @since 2021/1/30 17:25
     */
    public AjaxResult healthCodeSearch(String bizContent) {
        HealthCodeRequest healthCodeRequest;
        try {
            healthCodeRequest = JSONObject.parseObject(bizContent, HealthCodeRequest.class);
            DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, healthCodeRequest.getRequestTime());
        } catch (Exception e) {
            logger.error(REQUEST_PARAM_ERROR, e);
            return HttpAjaxResult.businessError(REQUEST_PARAM_ERROR + e.getMessage());
        }
        // 校验请求参数
        AjaxResult ajaxResult = checkRequest(healthCodeRequest);
        if (!HttpAjaxResult.HTTP_SUCC_CODE.equals(ajaxResult.get(AjaxResult.CODE_TAG))) {
            return ajaxResult;
        }
        return healthCodeService.healthCodeSearch(bizContent);

    }

    /**
     * 参数校验
     * 
     * @param request
     * @return
     */
    private AjaxResult checkRequest(HealthCodeRequest request) {
        if (StringUtils.isEmpty(request.getIdCarNo())) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_IDCARNO_ISNULL);
        }
        if (StringUtils.isEmpty(request.getName())) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_NAME_ISNULL);
        }
        String deviceCode = request.getDeviceCode();
        if (StringUtils.isEmpty(deviceCode)) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_DEVICECODE_ISNULL);
        }
        if (StringUtils.isEmpty(request.getRegion())) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_REGION_ISNULL);
        }
        if (StringUtils.isEmpty(request.getRequestSeq())) {
            return HttpAjaxResult.businessDataValidError(HealthCodeConstants.HTTP_REQUESTSEQ_ISNULL);
        }
        // 验证设备号是否合法
        DeviceInfo deviceCondition = new DeviceInfo();
        deviceCondition.setDeviceNo(deviceCode);
        List<DeviceInfo> deviceInfos = deviceInfoService.selectDeviceInfoList(deviceCondition);
        if (CollectionUtils.isEmpty(deviceInfos)) {
            return HttpAjaxResult.businessError(deviceCode + HealthCodeConstants.HTTP_DEVICECODE_NOTEXISTS);
        }
        // 验证appKey所属租户和设备所属租户是否一致
        if (tenantProperties.getEnabled()) {
            DeviceInfo deviceInfo = deviceInfos.get(0);
            String tenantId = TenantContextHolder.getTenantId();
            if (!deviceInfo.getTenantId().equals(tenantId)) {
                logger.error("The tenant ID of the device [{}] is [{}], which is inconsistent with the authorized tenant ID [{}]", deviceCode, deviceInfo.getTenantId(), tenantId);
                String msg = MessageUtils.message("base.person.handler.device.tenant.error", deviceCode);
                return HttpAjaxResult.businessError(msg);
            }
        }
        return HttpAjaxResult.httpSuccess();
    }
}
