package cn.eyecool.msg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.msg.domain.MsgLog;
import cn.eyecool.msg.service.IMsgLogService;

/**
 * 消息日志Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/log")
public class MsgLogController extends BaseController {
    @Autowired
    private IMsgLogService msgLogService;

    /**
     * 查询消息日志列表
     */
    @PreAuthorize("@ss.hasPermi('msg:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgLog msgLog) {
        startPage();
        List<MsgLog> list = msgLogService.selectMsgLogList(msgLog);
        return getDataTable(list);
    }

    /**
     * 导出消息日志列表
     */
    @PreAuthorize("@ss.hasPermi('msg:log:export')")
    @Log(title = "msg.log.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgLog msgLog) {
        List<MsgLog> list = msgLogService.selectMsgLogList(msgLog);
        ExcelUtil<MsgLog> util = new ExcelUtil<MsgLog>(MsgLog.class);
        return util.exportExcel(list, "log");
    }

    /**
     * 获取消息日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgLogService.selectMsgLogById(id));
    }

}
