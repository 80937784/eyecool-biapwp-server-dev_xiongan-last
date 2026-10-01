package cn.eyecool.noninductive.web.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;
import cn.eyecool.noninductive.disruptor.queue.FaceSearchDisruptorQueue;
import cn.eyecool.noninductive.domain.AtlasImageBean;
import cn.eyecool.noninductive.domain.AtlasImageBean.DATA.imgFace;

/**
 * Description Package com.eyecool.abis.business.interfaces.controller
 *
 * @author sunhuayu Date on 2020/2/27
 */
@RestController
@RequestMapping("/api/recognition")
public class AtlasImageProcessController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtlasImageProcessController.class);

    @PostMapping(value = "/snapshot-atlas")
    public AjaxResult image(@RequestBody String jsonString) throws IOException {
        AtlasImageBean atlasImageBean = JSON.parseObject(URLDecoder.decode(jsonString, "utf-8"), AtlasImageBean.class);

        LOGGER.info("---------- get request from device {} ----------", atlasImageBean.getStrTESn());
        if (atlasImageBean.getDATA() == null) {
            LOGGER.error("atlas http body is null,ignore");
            return AjaxResult.error("atlas http body is null,ignore");
        }
        if (atlasImageBean.getDATA().getImgFace() == null
            || CollectionUtils.isEmpty(atlasImageBean.getDATA().getImgFace())) {
            LOGGER.error("atlas http picture is null or picture'size is 0");
            return AjaxResult.error("atlas http picture is null or picture'size is 0");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("request picture'size [{}] serialNo [{}]",
                atlasImageBean.getDATA().getImgFace() == null ? 0 : atlasImageBean.getDATA().getImgFace().size(),
                atlasImageBean.getStrTESn());
        }
        //
        List<imgFace> pictureList = atlasImageBean.getDATA().getImgFace();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("设备[{}]人脸个数 [{}]", atlasImageBean.getStrTESn(), pictureList.size());
        }
        for (imgFace pictureItem : pictureList) {
            String data = pictureItem.getImg().replace(" ", "+");
            LOGGER.debug("face data'length [{}]", StringUtils.isEmpty(data) ? 0 : data.length());
            if (StringUtils.isEmpty(data)) {
                LOGGER.warn("face data is null ,ignore");
                continue;
            }
            FaceSearchEvent.FaceSearchMessage faceSearchMessage =
                new FaceSearchEvent.FaceSearchMessage(Base64.decodeBase64(data), atlasImageBean.getStrTESn(), null);
            // 将人脸搜索1：N的请求放入队列中
            FaceSearchDisruptorQueue.publishEvent(faceSearchMessage);
            LOGGER.info("put searchData [{}] into queue done", faceSearchMessage.getDeviceSerialNo());
        }
        return AjaxResult.success();
    }
}
