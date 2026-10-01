package cn.eyecool.server.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.eyecool.common.context.TenantContextHolder;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.domain.http.HttpAjaxResult;
import cn.eyecool.common.core.domain.http.StandardHttpParam;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.mqtt.MqttConfigManager;
import cn.eyecool.common.utils.AESUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.ServletUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.ip.IpUtils;
import cn.eyecool.common.utils.sign.Md5Utils;
import cn.eyecool.device.domain.DeviceInfo;
import cn.eyecool.device.service.IDeviceInfoService;
import cn.eyecool.fox.security.CryptService;
import cn.eyecool.fox.security.ECS1CryptService;
import cn.eyecool.server.annotation.HttpApiLog;
import cn.eyecool.server.handler.RequestDistributeHandler;
import cn.eyecool.server.http.param.MqttAuthParam;
import cn.eyecool.system.service.IPlatformConcurrentService;

/**
 * 标准HTTP请求API接口服务
 *
 * @author admin
 * @date 2019年11月4日
 */
@RestController
@RequestMapping("/api/standard")
public class StandardHttpServer extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(StandardHttpServer.class);

    @Autowired
    private RequestDistributeHandler requestDistributeHandler;
    @Autowired
    private IPlatformConcurrentService platformConcurrentService;
    @Autowired
    private IDeviceInfoService deviceInfoService;

    /**
     * 标准HTTP请求处理器
     *
     * @param httpParam
     * @return
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
    @CrossOrigin(origins = "*", methods = {RequestMethod.POST})
    @HttpApiLog(isSaveRequestData = true, isSaveResponseData = true)
    public AjaxResult httpRequestHandler(StandardHttpParam httpParam, HttpServletRequest request,
        HttpServletResponse response) {
        try {
            // 并发控制信号
            boolean semaphore = platformConcurrentService.acquireSemaphore();
            if (!semaphore) {
                String msg = MessageUtils.message("http.ajax.result.system.busy");
                return HttpAjaxResult.systemBusyError(msg);
            }
            String clientIp = IpUtils.getIpAddr(ServletUtils.getRequest());
            if (LOG.isTraceEnabled()) {
                LOG.trace("HTTP request parameter information=> clientIp:[{}], httpParam:[{}]", clientIp, httpParam.toString());
            } else {
                LOG.info(
                    "HTTP request parameter information=> clientIp：[{}], appKey:[{}], nonce:[{}], timstamp:[{}], sign:[{}], transCode:[{}] ",
                    clientIp, httpParam.getAppKey(), httpParam.getNonce(), httpParam.getTimestamp(),
                    httpParam.getSign(), httpParam.getTransCode());
            }
            // 防止其他拦截器设置,先清空一下，然后后续方法根据appkey设置租户到上下文
            TenantContextHolder.clear();
            // 调用具体的接口处理请求
            return requestDistributeHandler.distributeHandler(httpParam, request, response);
        } finally {
            platformConcurrentService.releseSemaphore();
            // 如果不使用@HttpApiLog注解，请注意放开以下代码，否则请注释掉以下代码
            // TenantContextHolder.clear();
        }
    }

    /**
     * 根据照片所在路径（aes加密后的密文）获取照片流
     *
     * @param request
     * @param response
     * @author zfx
     * @since 2021/3/4 19:11
     */
    @RequestMapping(value = {"/getImgStreamByPath"}, method = {RequestMethod.GET},
        produces = "application/json;charset=UTF-8")
    public void getImgStreamByPath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getParameter("path");
        if (StringUtils.isEmpty(path)) {
            throw new CustomException(MessageUtils.message("standard.server.request.path.empty"));
        }
        OutputStream outputStream = null;
        FileInputStream in = null;
        String realPath = path.replace(" ", "+");
        try {
            realPath = AESUtils.decryptAES(realPath);
            LOG.info("getImgByFullPath realpath[{}]", realPath);
            File file = new File(realPath);
            String fileName = file.getName();
            String ext;
            if (fileName.contains(".")) {
                ext = file.getName().substring(fileName.indexOf("."));
            } else {
                ext = "jpg";
            }
            if (ext.toLowerCase().equals("jpg")) {
                response.setContentType("image/jpeg");
            } else if (ext.toLowerCase().equals("png")) {
                response.setContentType("image/png");
            }
            in = new FileInputStream(file);
            response.setContentType("image/jpeg");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] bs = new byte[in.available()];
            int size = in.read(bs);
            LOG.debug("Get photo stream, read size [{}]", size);
            CryptService cs = new ECS1CryptService();
            byte[] bb = cs.decrypt(bs);
            outputStream.write(bb);
        } catch (Exception e) {
            LOG.error("getImgStreamByPath aespath[{}]realPath[{}] error[{}]", path, realPath, e.getMessage(), e);
            throw new CustomException(e.getMessage());
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 根据图片路径 获取对应的base64字符串 path为加密后的参数
     *
     * @param path
     * @return 对应照片的base64
     * @author zfx
     * @since 2021/3/4 19:35
     */
    @RequestMapping(value = "/getImgBase64ByPath", method = {RequestMethod.GET},
        produces = "application/text;charset=UTF-8")
    public String getImgBase64ByPath(String path) {
        String realPath = "";
        try {
            if (StringUtils.isEmpty(path)) {
                throw new CustomException(MessageUtils.message("standard.server.request.path.empty"));
            }
            realPath = path.replace(" ", "+");
            realPath = AESUtils.decryptAES(realPath);
            String base64 = PlatformFileUtils.getImageBase64(realPath);
            if (StringUtils.isEmpty(base64)) {
                throw new CustomException(MessageUtils.message("standard.server.request.path.image.not.exists", realPath));
            }
            String decryptBase64 = PlatformCryptUtils.decryptImageBase64(base64);
            return decryptBase64;
        } catch (Exception e) {
            LOG.error("aespath[{}]realPath[{}]error[{}]", path, realPath, e.getMessage(), e);
            throw new CustomException(MessageUtils.message("standard.server.request.path.image.error", realPath));
        }
    }

    /**
     * 获取服务器时间
     * 
     * @return
     */
    @RequestMapping(value = "/serverTime", method = {RequestMethod.GET})
    public AjaxResult getServerTime() {
        long currTimestamp = System.currentTimeMillis();
        // 暂时只返回服务器时间戳，后期如有需要可以拓展其他内容
        Map<String, Object> map = Maps.newHashMap();
        map.put("currTimestamp", currTimestamp);
        return HttpAjaxResult.httpSuccess(map);
    }

    /**
     * 设备mqtt验证
     * 
     * @param clientid
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/mqtt/auth", method = {RequestMethod.POST})
    public AjaxResult mqttAuth(@Valid MqttAuthParam param, BindingResult bindingResult, HttpServletRequest request,
        HttpServletResponse response) {
        // 1、公共参数合法性校验
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            LOG.error(msg);
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return HttpAjaxResult.httpError();
        }
        String clientid = param.getClientid();
        String username = param.getUsername();
        String password = param.getPassword();
        // 园区平台连接校验, 平台clientId必须保证格式为（前缀_节点号），例如biapwp_platform_001、biapwp_platform_002
        String platFormClientId = MqttConfigManager.getItemValue("clientId");
        String platFormUsername = MqttConfigManager.getItemValue("username");
        String platFormPassword = MqttConfigManager.getItemValue("password");
        boolean mayBePlatform = false;
        if (clientid.lastIndexOf("_") != -1) {
            mayBePlatform = clientid.substring(0, clientid.lastIndexOf("_"))
                .equals(platFormClientId.subSequence(0, platFormClientId.lastIndexOf("_")));
        }
        if (mayBePlatform && username.equals(platFormUsername) && platFormPassword.equals(password)) {
            response.setStatus(HttpStatus.SC_OK);
            return HttpAjaxResult.httpSuccess();
        }
        DeviceInfo condition = new DeviceInfo();
        condition.setDeviceNo(username);
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(condition);
        if (CollectionUtils.isEmpty(list)) {
            LOG.error("device [sn = {} ] not exists, mqtt auth failed!", username);
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            return HttpAjaxResult.httpError();
        }
        DeviceInfo info = list.get(0);
        if (StringUtils.isBlank(info.getMqttPwd()) || StringUtils.isBlank(info.getMqttSalt())) {
            info = deviceInfoService.initMqttAuthInfo(info);
            // 重新更新mqtt连接信息
            deviceInfoService.updateMqttAuthInfo(info);
        }
        String mqttPwd = info.getMqttPwd();
        String mqttSalt = info.getMqttSalt();
        String encryptedPwd = Md5Utils.hash(username + password + mqttSalt);
        if (mqttPwd.equals(encryptedPwd)) {
            response.setStatus(HttpStatus.SC_OK);
            return HttpAjaxResult.httpSuccess();
        }
        LOG.error("device [sn = {} ] mqtt auth failed!", username);
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        return HttpAjaxResult.httpError();
    }

}
