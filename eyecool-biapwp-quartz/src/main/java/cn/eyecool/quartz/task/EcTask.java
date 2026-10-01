package cn.eyecool.quartz.task;

import org.springframework.stereotype.Component;

import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 定时任务调度测试
 * 
 * @author admin
 */
@Component("ecTask")
public class EcTask {
    public void ecMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
        String msg = MessageUtils.message("ec.task.multi.params.msg", s, b, l, d, i);
        System.out.println(msg);
    }

    public void ecParams(String params) {
        System.out.println(MessageUtils.message("ec.task.params.msg"));
    }

    public void ecNoParams() {
        System.out.println(MessageUtils.message("ec.task.noparams.msg"));
    }
}
