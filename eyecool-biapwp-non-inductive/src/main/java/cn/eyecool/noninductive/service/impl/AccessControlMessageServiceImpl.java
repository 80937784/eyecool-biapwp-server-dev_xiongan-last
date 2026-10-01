package cn.eyecool.noninductive.service.impl;

import java.io.File;
import java.io.IOException;

import cn.eyecool.noninductive.disruptor.event.FaceSearchResultNoticeEvent;
import cn.eyecool.common.core.text.Convert;
import cn.eyecool.common.utils.http.HttpUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.eyecool.noninductive.service.IMessageService;

/**
 * <h1>门禁消息处理类
 * <p>
 * 暂时使用发送到独立的门禁控制系统的方式实现
 * <p>
 * <TODO>集成
 *
 * @author 李强
 * @version [版本号, 2019年5月9日]
 * @since [应用/版本]
 */
@Service
public class AccessControlMessageServiceImpl implements IMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlMessageServiceImpl.class);

    @Value("${biapwp.dfrs.message.send.url:http://localhost:8765/biapwp/recognition/snapshot}")
    private String url;

    @Override
    public String sendMsg(FaceSearchResultNoticeEvent.FaceSearchResultMessage message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] 比对通过，发送门禁指令 ", message.getPersonId());
        }
        JSONObject paramMap = new JSONObject();
        paramMap.put("personCode", message.getPersonId());
        paramMap.put("result", message.getResult());
        paramMap.put("score", String.valueOf(message.getMatchScore()));
        paramMap.put("matchTime", message.getMatchTime());
        paramMap.put("deviceNo", message.getDeviceNo());
        paramMap.put("deviceAddr", message.getDeviceAddr());

        LOGGER.info("personCode:{},result:{},score:{},matchTime:{},deviceNo:{},deviceAddr:{}", message.getPersonId(),
            message.getResult(), message.getMatchScore(), message.getMatchTime(), message.getDeviceNo(),
            message.getDeviceAddr());
        File file = new File(message.getLiveFaceDataUrl());
        if (!file.exists()) {
            paramMap.put("liveFaceDataB64", null);
        } else {
            byte[] liveFaceData;
            try {
                liveFaceData = FileUtils.readFileToByteArray(file);
                if (liveFaceData == null) {
                    paramMap.put("liveFaceDataB64", null);
                } else {
                    paramMap.put("liveFaceDataB64", Base64.encodeBase64String(liveFaceData));
                }
            } catch (IOException e1) {
                LOGGER.error(e1.getMessage(), e1);
                paramMap.put("liveFaceDataB64", null);
            }
        }
        File tmplFile = new File(message.getTmplImageUrl());
        LOGGER.info("tmplFile:{}", tmplFile.getAbsolutePath());
        if (!tmplFile.exists()) {
            paramMap.put("tmplImageDataB64", null);
        } else {
            byte[] tmplImageData;
            try {
                tmplImageData = FileUtils.readFileToByteArray(tmplFile);
                if (tmplImageData == null) {
                    paramMap.put("tmplImageDataB64", null);
                    LOGGER.info("tmpl data :{}", 0);
                } else {
                    paramMap.put("tmplImageDataB64", Base64.encodeBase64String(tmplImageData));
                    LOGGER.info("tmpl data :{}", tmplImageData.length);
                }
            } catch (IOException e1) {
                LOGGER.error(e1.getMessage(), e1);
                paramMap.put("tmplImageDataB64", null);
            }
        }
        // 输出
        try {
            StringBuilder result = new StringBuilder();
            for (String s : Convert.toStrArray(url)) {
                result.append(HttpUtils.sendPost(s, paramMap.toJSONString()));
                LOGGER.info("result content [{}]", result);
            }
            return result.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}
