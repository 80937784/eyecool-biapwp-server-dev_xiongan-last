package cn.eyecool.noninductive.web.controller;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;
import cn.eyecool.noninductive.disruptor.queue.FaceSearchDisruptorQueue;
import cn.eyecool.noninductive.domain.ZhenShiImageBean;
import cn.eyecool.noninductive.domain.ZhenShiImageBean.Body.Picture;

/**
 * 臻识人脸抓拍机图像http服务接口
 *
 * @date 2019-03-04
 */
@RestController
@RequestMapping("/api/recognition")
public class ZhenShiImageProcessController extends BaseController {

    @PostMapping({"/snapshot", "/snapshot.php"})
    public AjaxResult image(@RequestBody ZhenShiImageBean request) {
        if (logger.isTraceEnabled()) {
            logger.trace("zhenshi image search request [{}]", JSONObject.toJSONString(request));
        }
        if (request.getBody() == null) {
            return AjaxResult.error("zhenshi http body is null,ignore");
        }
        if (request.getBody().getPicture() == null || CollectionUtils.isEmpty(request.getBody().getPicture())) {
            return AjaxResult.error("zhenshi http picture is null or picture'size is 0");
        }
        logger.debug("request picture'size [{}] serialNo [{}]",
            request.getBody().getPicture() == null ? 0 : request.getBody().getPicture().size(),
            request.getBody().getSerialno());
        List<Picture> pictureList = request.getBody().getPicture();
        if (logger.isDebugEnabled()) {
            logger.debug("设备[{}]人脸个数 [{}]", request.getBody().getSerialno(), pictureList.size());
        }
        for (Picture pictureItem : pictureList) {
            String data = pictureItem.getData();
            logger.debug("face data'length [{}]", StringUtils.isEmpty(data) ? 0 : data.length());
            if (StringUtils.isEmpty(data)) {
                logger.warn("face data is null ,ignore");
                continue;
            }
            FaceSearchEvent.FaceSearchMessage faceSearchMessage =
                new FaceSearchEvent.FaceSearchMessage(Base64.decodeBase64(data), request.getBody().getSerialno(), null);
            // 将人脸搜索1：N的请求放入队列中
            FaceSearchDisruptorQueue.publishEvent(faceSearchMessage);
        }
        return AjaxResult.success();
    }
}
