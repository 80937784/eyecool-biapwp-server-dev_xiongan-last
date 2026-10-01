package cn.eyecool.tool.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.file.PlatformFileUtils;
import cn.eyecool.common.utils.qrcode.QRCodeUtil;
import cn.eyecool.common.utils.uuid.UUID;
import cn.eyecool.tool.domain.QrCodeGenParam;

/**
 * 二维码生成Controller
 * 
 * @author mawj
 * @date 2021/06/01
 */
@RestController
@RequestMapping("/tool/qrcode")
public class QrCodeController {

    private static final Logger LOG = LoggerFactory.getLogger(QrCodeController.class);

    /**
     * 生成二维码
     * 
     * @param param
     * @return
     */
    @PreAuthorize("@ss.hasPermi('tool:sdkFile:list')")
    @PostMapping("/gen")
    public AjaxResult getUserIris(@RequestBody QrCodeGenParam param) {
        String content = param.getContent();
        String destPath = EyecoolConfig.getDownloadPath() + UUID.randomUUID() + ".jpg";
        try {
            QRCodeUtil.encode(content, null, destPath, true);
        } catch (Exception e) {
            LOG.error("Content[{}] generates QR code error:", content, e);
            throw new CustomException(MessageUtils.message("utils.qrcode.generate.error", e.getMessage()));
        }
        Map<String, Object> result = new HashMap<>();
        String imageBase64 = PlatformFileUtils.getImageBase64(destPath);
        result.put("qrcodeImgBase64", imageBase64);
        CompletableFuture.runAsync(() -> {
            PlatformFileUtils.deleteFile(destPath);
        });
        return AjaxResult.success(result);
    }

}
