//package cn.eyecool.noninductive.sdk.HCNetCamera.service.impl;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import cn.eyecool.common.constant.DictConstants;
//import cn.eyecool.common.core.redis.RedisCache;
//import cn.eyecool.device.domain.DeviceInfo;
//import cn.eyecool.device.service.IDeviceInfoService;
//import cn.eyecool.system.service.ISysConfigService;
//import org.apache.commons.collections4.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.jna.Pointer;
//
//import cn.eyecool.noninductive.sdk.HCNetCamera.FMSGCallBack_V31;
//import cn.eyecool.noninductive.sdk.HCNetCamera.service.HCNetSDK;
//import cn.eyecool.noninductive.sdk.HCNetCamera.service.IHCNetDeviceService;
//
///**
// * Description Package com.eyecool.abis.bio.face.video.HCNetCamera
// *
// * @author sunhuayu Date on 2020/3/16
// */
//// @ConditionalOnProperty(prefix = "biapwp.dfrs.hkTherm", name = "enable", havingValue = "false")
//// @ConditionalOnClass(SecurityManager.class)
//// @Service
//public class HCNetThermDeviceServiceImpl implements IHCNetDeviceService {
//    @Value("${biapwp.dfrs.hkTherm.teso_lib}")
//    private String hkthermLibPath;
//    @Value(("${biapwp.dfrs.hkTherm.libName:hcnetsdk}"))
//    private String hkThermLibName;
//    private static final Logger LOGGER = LoggerFactory.getLogger(HCNetThermDeviceServiceImpl.class);
//    public static final Map<String, Integer> ALARM_HANDLER_MAP = new ConcurrentHashMap<>();
//    private HCNetSDK hCNetSDK;
//    private RedisCache redisCache;
//    public HCNetThermDeviceServiceImpl( IDeviceInfoService deviceInfoService,
//        FMSGCallBack_V31 fmsgCallBack_v31, ISysConfigService configService,RedisCache redisCache) {
//        this.deviceInfoService = deviceInfoService;
//        this.fmsgCallBack_v31 = fmsgCallBack_v31;
//        this.configService = configService;
//        this.redisCache=redisCache;
//    }
//    private Map<String, Integer> hCNetDeviceIPUserIdCache;
//    private final IDeviceInfoService deviceInfoService;
//    private final FMSGCallBack_V31 fmsgCallBack_v31;
//    @SuppressWarnings("unused")
//    private final ISysConfigService configService;
//
//    // @PostConstruct
//    public void initHCNetSDK() {
//        this.hCNetSDK = HCNetSDK.INSTANCE;
//        hCNetDeviceIPUserIdCache = redisCache.getCacheMap("hCNetDeviceIPUserIdCache");
//        // 设置HCNetSDKCom组件库所在路径
//        // String strPathCom = "/usr/lib";
//        // HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
//        // System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
//        // struComPath.write();
//        // hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
//        boolean b = hCNetSDK.NET_DVR_Init();
//        // hCNetSDK.NET_DVR_SetSDKLocalCfg()
//        LOGGER.info("HCNetSDK init: [{}],NET_DVR_SetSDKInitCfg:{}", b ? "success" : "fail",
//            hCNetSDK.NET_DVR_GetLastError());
//        // 设置libcrypto.so所在路径
//        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
//        String strPathCrypto = hkthermLibPath + "/libcrypto.so";
//        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
//        ptrByteArrayCrypto.write();
//        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());
//
//        // 设置libssl.so所在路径
//        HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
//        String strPathSsl = hkthermLibPath + "/libssl.so";
//        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
//        ptrByteArraySsl.write();
//        hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());
//        HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG struGeneralCfg = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
//        struGeneralCfg.byAlarmJsonPictureSeparate = 1; // 控制JSON透传报警数据和图片是否分离，0-不分离，1-分离（分离后走COMM_ISAPI_ALARM回调返回）
//        struGeneralCfg.write();
//
//        if (!hCNetSDK.NET_DVR_SetSDKLocalCfg(17, struGeneralCfg.getPointer())) {
//            System.out.println("NET_DVR_SetSDKLocalCfg失败");
//        }
//        // http://222.222.141.60:8889/
//        // String hostEx="222.222.141.60";
//        // String m_sUsername = "admin";
//        // String m_sPassword = "eyecool2021!";
//        // initHCNetDevices(hostEx,m_sUsername,m_sPassword, (short) 8000);
//
//        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(new DeviceInfo());
//        List<DeviceInfo> deviceWithExtInfos =
//            list.stream().filter(l -> DictConstants.ClientDeviceType.FALLING_OBJECTS_DEVICE.equals(l.getDeviceType()))
//                .collect(Collectors.toList());
//
//        for (DeviceInfo deviceWithExtInfo : deviceWithExtInfos) {
//            String extInfoString = deviceWithExtInfo.getExtInfo();
//            JSONObject extInfo = JSON.parseObject(extInfoString);
//            if (extInfo.getBoolean("isThrownDetection") != null) {
//                String host = deviceWithExtInfo.getDeviceIp();
//                String username = extInfo.getString("username");
//                String password = extInfo.getString("password");
//                Short port = extInfo.getShort("port");
//                initHCNetDevices(host, username, password, port);
//            }
//        }
//    }
//
//    public boolean initHCNetDevices(String host, String m_sUsername, String m_sPassword, short port) {
//        DeviceInfo clientDeviceInfo = new DeviceInfo();
//        clientDeviceInfo.setDeviceType(DictConstants.ClientDeviceType.NONINDUCTIVE_DEVICE);
//        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(clientDeviceInfo);
//        if (CollectionUtils.isEmpty(list)) {
//            LOGGER.info("deviceType=[{}]无感设备不存在", DictConstants.ClientDeviceType.NONINDUCTIVE_DEVICE);
//            return false;
//        }
//        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
//        HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
//
//        for (int i = 0; i < host.length(); i++) {
//            m_strLoginInfo.sDeviceAddress[i] = (byte)host.charAt(i);
//        }
//        for (int i = 0; i < m_sPassword.length(); i++) {
//            m_strLoginInfo.sPassword[i] = (byte)m_sPassword.charAt(i);
//        }
//        for (int i = 0; i < m_sUsername.length(); i++) {
//            m_strLoginInfo.sUserName[i] = (byte)m_sUsername.charAt(i);
//        }
//        m_strLoginInfo.wPort = port;
//        // m_strLoginInfo.bUseAsynLogin = false;
//
//        m_strLoginInfo.write();
//
//        int lUserID = -1;
//        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, struDeviceInfo);
//        if (lUserID >= 0) {
//            this.hCNetDeviceIPUserIdCache.put(host, lUserID);
//        }
//        LOGGER.info("camera login_40 result: [{}], message: [{}]", lUserID, hCNetSDK.NET_DVR_GetLastError());
//        Pointer pUser = null;
//        boolean callBack_v31Result = hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fmsgCallBack_v31, pUser);
//        LOGGER.info("HCNetSDK setCallBack31: [{}],lastError: [{}]", callBack_v31Result ? "success" : "fail",
//            hCNetSDK.NET_DVR_GetLastError());
//        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
//        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
//        m_strAlarmInfo.write();
//        int lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
//        LOGGER.info("HCNetSDK setupAlarmChan_V41: [{}],lastError: [{}]", lAlarmHandle == 0 ? "success" : "fail",
//            hCNetSDK.NET_DVR_GetLastError());
//        ALARM_HANDLER_MAP.put(host, lAlarmHandle);
//        return lUserID < 0;
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public boolean unRegisterHCNetDevice(String host) {
//        Integer lUserId = hCNetDeviceIPUserIdCache.get(host);
//        if (lUserId == null) {
//            return false;
//        }
//        boolean b = false;
//        if (ALARM_HANDLER_MAP.get(host) != null) {
//            boolean b1 = hCNetSDK.NET_DVR_CloseAlarmChan_V30(ALARM_HANDLER_MAP.get(host));
//            if (b1) {
//                LOGGER.info("设备去除{}回调成功。", host);
//            } else {
//                LOGGER.warn("设备去除{}回调失败,errorCode:{}", host, hCNetSDK.NET_DVR_GetLastError());
//            }
//        }
//        b = hCNetSDK.NET_DVR_Logout(lUserId);
//        if (b) {
//            DeviceInfo condition = new DeviceInfo();
//            condition.setDeviceIp(host);
//            condition.setDeviceType(DictConstants.ClientDeviceType.NONINDUCTIVE_DEVICE);
//            List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
//            if (CollectionUtils.isNotEmpty(list)) {
//                DeviceInfo clientDeviceInfo = list.get(0);
//                clientDeviceInfo.setDeviceState(DictConstants.DeviceOnlineState.OFFLINE);
//                deviceInfoService.updateDeviceInfo(clientDeviceInfo);
//                LOGGER.info("设备[deviceNo={}][deviceIp={}]去除，修改设备在线状态为离线", clientDeviceInfo.getDeviceNo(), host);
//            }
//        }
//        return b;
//    }
//
//    @Override
//    public boolean registerHCNetDevice(String host, short port, String username, String password, String deviceNo) {
//        return initHCNetDevices(host, username, password, port);
//    }
//
//    // @Scheduled(cron = "0 */1 * * * ?")
//    public void checkHCNetDeviceAlive() {
//        ObjectMapper mapper = new ObjectMapper();
//        String pattern = "(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})\\:(\\d+)";
//        Pattern r = Pattern.compile(pattern);
//        DeviceInfo clientDeviceInfo = new DeviceInfo();
//        clientDeviceInfo.setDeviceType(DictConstants.ClientDeviceType.NONINDUCTIVE_DEVICE);
//        List<DeviceInfo> clientDeviceInfos = deviceInfoService.selectDeviceInfoList(clientDeviceInfo);
//        if (CollectionUtils.isEmpty(clientDeviceInfos)) {
//            return;
//        }
//        clientDeviceInfos.forEach(s -> {
//            HashMap hashMap = mapper.convertValue(s.getExtInfo(), HashMap.class);
//            String rtspUrl = String.valueOf(hashMap.get("rtspUrl"));
//            Matcher matcher = r.matcher(rtspUrl);
//            while (matcher.find()) {
//                String ip = matcher.group(1);
//                Integer port = Integer.parseInt(matcher.group(2));
//                // boolean alive = checkHCNetDeviceAlive(ip, port);
//                // if (alive) {
//                // s.setDeviceState(DictConstants.DeviceOnlineState.ONLINE);
//                // } else {
//                // s.setDeviceState(DictConstants.DeviceOnlineState.OFFLINE);
//                // }
//                deviceInfoService.updateDeviceInfo(s);
//                LOGGER.info("{} checkAlive complete,alive?:{} ", ip, false);
//            }
//        });
//    }
//}
