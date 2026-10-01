package cn.eyecool.device.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.utils.IdWorker;
import cn.eyecool.device.constant.AdapterConstants;
import cn.eyecool.device.service.IDeviceAccessAdapterService;
import cn.eyecool.device.util.AdapterUtils;

/**
 * 203设备接入Controller
 * 
 * @author admin
 * @date 2021-04-25
 */
@RestController
@RequestMapping("/api/device/adapter")
public class DeviceAccessAdapterController extends BaseController {

    @Autowired
    private IDeviceAccessAdapterService deviceAccessAdapterService;

    /**
     * 获取图片
     *
     * @param id 人员id
     * @param response 响应
     * @return str null
     * @throws IOException IOException
     */
    @RequestMapping(value = "/getPersonFace", method = RequestMethod.GET,
        produces = {"application/vnd.ms-excel;charset=UTF-8"})
    public String getPic(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
        response.setContentType("image/webp");
        InputStream in = null;
        OutputStream os = null;
        File file = null;

        try {
            String denId = AdapterUtils.dencrypt(id, AdapterConstants.SECRET);
            boolean flag = deviceAccessAdapterService.checkValid(denId);
            if (!flag) {
                return null;
            }

            String base64Code = deviceAccessAdapterService.getPersonFaceBase64(denId);
            String fileName = IdWorker.getNextStringId() + AdapterConstants.JPG;
            String filePath = EyecoolConfig.getDownloadPath();
            file = deviceAccessAdapterService.base64ToFile(base64Code, filePath, fileName);
            String imgPath = filePath + File.separator + fileName;
            file = deviceAccessAdapterService.dealImage(imgPath);
            in = new FileInputStream(file);
            os = response.getOutputStream();
            byte[] b = new byte[1024];
            while (in.read(b) != -1) {
                os.write(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("There is an exception for getting person picture：[{}]", e.toString());
        } finally {
            if (in != null) {
                in.close();
            }
            if (os != null) {
                os.flush();
                os.close();
            }
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    logger.error("The device 203 pull the face pictures, delete the temp file failed.[{}]", file.getAbsolutePath());
                    
                }
            }
        }
        return null;
    }

    /**
     * 保存日志信息
     *
     * @param request 提交的数据
     * @return 查看或错误页面的地址
     */
    @ResponseBody
    @RequestMapping(value = "putData", method = RequestMethod.POST)
    public String putData(@RequestBody JSONObject jsonObj) {
        String ret = deviceAccessAdapterService.handleLog(JSON.toJSONString(jsonObj));
        return ret;
    }

}
