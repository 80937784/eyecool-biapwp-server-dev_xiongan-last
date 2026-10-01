package cn.eyecool.noninductive.web.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import cn.eyecool.common.constant.Constants;
import cn.eyecool.common.constant.DictConstants;
import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.model.LoginBody;
import cn.eyecool.common.core.redis.RedisCache;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.device.constant.RedisKeyConstants;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.noninductive.NonInductiveConstants;
import cn.eyecool.noninductive.service.INonInductivePersonFaceSearchLogService;
import cn.eyecool.noninductive.service.impl.NonInductiveFaceServiceImpl;
import cn.eyecool.system.service.ISysConfigService;
import cn.eyecool.tradelog.domain.PersonFaceSearchLog;

/**
 * 首页 业务处理
 *
 * @author Base
 */
@RestController
@RequestMapping("/api/show")
public class MessageShowController extends BaseController {

    @Autowired
    private RedisCache redisCache;

    @Value("${abis.bio.image.search.stranger.push:true}")
    private boolean strangerPush;

    @Autowired
    private IDeviceInfoService clientDeviceInfoService;

    @Autowired
    private INonInductivePersonFaceSearchLogService dfrsPersonFaceSearchLogService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private NonInductiveFaceServiceImpl loginService;

    @Value("${server.port:2}")
    private String serverPort;

    @Value("${biapwp.dfrs.hkTherm.enable:false}")
    private boolean hkThermEnable;

    @GetMapping(value = "/listdevice")
    public AjaxResult listDevices(String token) {
        String tenantId = AESUtils.decryptAES(token).split(":")[0];
        String cacheKey = RedisKeyConstants.NONINDUCTIVE_SOCKET_CACHE_KEY + ":" + tenantId;
        if (!redisCache.getCacheList(cacheKey).contains(token)) {
            return AjaxResult.error("用户token不存在。请重新登录；");
        }
        TenantContextHolder.setTenantId(tenantId);
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceType(DictConstants.DeviceType.NONINDUCTIVE_DEVICE);
        List<DeviceInfo> list = clientDeviceInfoService.selectDeviceInfoList(condition);
        String devicesInfo = JSONObject.toJSONString(list);
        logger.debug("设备列表信息 [{}]", devicesInfo);
        return AjaxResult.success(devicesInfo);
    }

    @GetMapping(value = "/listresult")
    public AjaxResult listRecogniseResults(@RequestParam(value = "deviceNo") String deviceNo,
        @RequestParam(value = "showTmpl") Boolean showTmpl, @RequestParam(value = "token") String token) {
        String tenantId = AESUtils.decryptAES(token).split(":")[0];
        String cacheKey = RedisKeyConstants.NONINDUCTIVE_SOCKET_CACHE_KEY + ":" + tenantId;
        if (!redisCache.getCacheList(cacheKey).contains(token)) {
            return AjaxResult.error("用户token不存在。请重新登录；");
        }
        TenantContextHolder.setTenantId(tenantId);
        String alarmTem = configService.selectConfigByKey(NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_ALARMTEM);

        // 根据设备IP 查询设备id
        logger.debug("查询设备 [{}]对应的识别记录", deviceNo);
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(deviceNo);
        List<DeviceInfo> list = clientDeviceInfoService.selectDeviceInfoList(condition);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        DeviceInfo device = list.get(0);
        if (!DictConstants.DeviceType.NONINDUCTIVE_DEVICE.equals(device.getDeviceType())) {
            String msg = "设备[{}]不是无感识别设备";
            logger.error(msg);
            throw new CustomException(msg);
        }

        List<PersonFaceSearchLog> recogLogList;
        List<PersonFaceSearchLog> recogLogAlarmList;
        String strangerPushStr =
            configService.selectConfigByKey(NonInductiveConstants.BIAPWP_NON_INDUCTIVE_STRANGER_PUSH);
        String hitPushStr = configService.selectConfigByKey(NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HIT_PUSH);
        strangerPush = StringUtils.isNotEmpty(strangerPushStr) ? Boolean.parseBoolean(strangerPushStr) : false;
        boolean hitPush = StringUtils.isNotEmpty(hitPushStr) ? Boolean.parseBoolean(hitPushStr) : true;
        if (strangerPush && !hitPush) {
            if (hkThermEnable) {
                recogLogList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.NOTPASS, NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_TEM,
                    null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.NOTPASS,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : alarmTem,
                    null);
            } else {
                recogLogList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.NOTPASS, null, null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.NOTPASS,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : null, null);
            }
        } else if (strangerPush) {
            if (hkThermEnable) {
                recogLogList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(), null,
                    NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_TEM, null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(), null,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : alarmTem,
                    null);
            } else {
                recogLogList =
                    dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(), null, null, null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(), null,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : null, null);
            }
        } else {
            if (hkThermEnable) {
                recogLogList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.PASS, NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_TEM, null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.PASS,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : alarmTem,
                    null);
            } else {
                recogLogList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.PASS, null, null);
                recogLogAlarmList = dfrsPersonFaceSearchLogService.selectLogByDeviceIdLimit(device.getDeviceNo(),
                    DictConstants.BioResult.PASS,
                    alarmTem == null ? NonInductiveConstants.BIAPWP_NON_INDUCTIVE_HCNET_DEFAULT_ALAEMTEM : null, null);
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("recogLogList", recogLogList);
        jsonObject.put("recogLogAlarmList", recogLogAlarmList);
        jsonObject.put("todayIdentifyNumber", 0);
        jsonObject.put("todayThermalNumber", 0);
        String logsJson = jsonObject.toJSONString();
        logger.debug("设备 [{}] 对应的识别记录 [{}]", deviceNo, logsJson);
        return AjaxResult.success(logsJson);
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody(required = false) LoginBody loginBody) throws UnsupportedEncodingException {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        JSONObject jsonObject = loginService.login(loginBody.getUsername(), loginBody.getPassword());
        String cacheKey = RedisKeyConstants.NONINDUCTIVE_SOCKET_CACHE_KEY + ":" + jsonObject.getString("tenantId");
        List<String> tenantCacheList = redisCache.getCacheList(cacheKey);
        if (CollectionUtils.isEmpty(tenantCacheList) || !tenantCacheList.contains(jsonObject.getString("token"))) {
            redisCache.setCacheList(cacheKey, Lists.newArrayList(jsonObject.getString("token")));
        }
        ajax.put(Constants.TOKEN, jsonObject.get("token"));
        return ajax;
    }

}
