package cn.eyecool.msg.configure.ding;

import java.io.Serializable;

/**
 * 钉钉消息实体
 * 
 * @author admin
 * @date 2020年3月31日
 */
public class DingTalkMsgInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 钉钉链接消息实体类 */
    public static class DingTalkLinkMsgInfo {
        private String title;
        private String text;
        private String messageUrl;
        private String picUrl;

        public DingTalkLinkMsgInfo() {
            super();
        }

        public DingTalkLinkMsgInfo(String title, String text, String messageUrl, String picUrl) {
            super();
            this.title = title;
            this.text = text;
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public void setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }
    }

    /** 钉钉markdow消息实体类 */
    public static class DingTalkMarkdownMsgInfo {
        private String title;
        private String text;

        public DingTalkMarkdownMsgInfo() {
            super();
        }

        public DingTalkMarkdownMsgInfo(String title, String text) {
            super();
            this.title = title;
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
