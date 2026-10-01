package cn.eyecool.msg.trade.handler;

import org.springframework.stereotype.Service;
import org.weixin4j.model.message.OutputMessage;
import org.weixin4j.model.message.normal.ImageInputMessage;
import org.weixin4j.model.message.normal.LinkInputMessage;
import org.weixin4j.model.message.normal.LocationInputMessage;
import org.weixin4j.model.message.normal.ShortVideoInputMessage;
import org.weixin4j.model.message.normal.TextInputMessage;
import org.weixin4j.model.message.normal.VideoInputMessage;
import org.weixin4j.model.message.normal.VoiceInputMessage;
import org.weixin4j.spi.INormalMessageHandler;

/**
 * 微信公众平台接受消息处理器
 * 
 * @author mawenjun
 * @version 1.0
 * @date 2020年3月25日
 *
 */
@Service
public class WeixinNormalMessageHandler implements INormalMessageHandler {

    @Override
    public OutputMessage textTypeMsg(TextInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage imageTypeMsg(ImageInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage voiceTypeMsg(VoiceInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage videoTypeMsg(VideoInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage shortvideoTypeMsg(ShortVideoInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage locationTypeMsg(LocationInputMessage msg) {
        return null;
    }

    @Override
    public OutputMessage linkTypeMsg(LinkInputMessage msg) {
        return null;
    }

}
