//package cn.eyecool.noninductive.sdk.HCNetCamera.service.impl;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.stereotype.Service;
//
//import cn.eyecool.noninductive.sdk.HCNetCamera.service.IHCNetDeviceService;
//
///**
// * Description
// * Package com.eyecool.abis.bio.face.video.HCNetCamera.service.impl
// *
// * @author sunhuayu
// * Date on 2020/4/17
// */
//@ConditionalOnMissingBean(value = HCNetThermDeviceServiceImpl.class)
//@Service
//public class HCNetCommonThermDeviceService implements IHCNetDeviceService {
//    @Override
//    public boolean registerHCNetDevice(String host, short port, String username, String password, String deviceNo) {
//        throw new BusinessException("未进行温控相关配置，请重新选择设备类型");
//    }
//
//    @Override
//    public boolean unRegisterHCNetDevice(String host) {
//        throw new BusinessException("未进行温控相关配置，请重新寻则设备类型");
//    }
//}
