package cn.eyecool.noninductive.sdk.HCNetCamera.service;

/**
 * Description
 * Package com.eyecool.abis.bio.face.video.HCNetCamera
 *
 * @author sunhuayu
 * Date on 2020/4/17
 */
public interface IHCNetDeviceService {

    boolean registerHCNetDevice(String host, short port, String username, String password, String deviceNo);

    boolean unRegisterHCNetDevice(String host);

}
