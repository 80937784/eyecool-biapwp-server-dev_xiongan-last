package cn.eyecool.msg.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.eyecool.msg.domain.MsgLogAnnex;
import cn.eyecool.msg.service.IMsgLogAnnexService;

/**
 * 消息日志附件Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/logannex")
public class MsgLogAnnexController extends BaseController {
    @Autowired
    private IMsgLogAnnexService msgLogAnnexService;

    /**
     * 查询消息日志附件列表
     */
    @PreAuthorize("@ss.hasPermi('msg:logannex:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgLogAnnex msgLogAnnex) {
        startPage();
        List<MsgLogAnnex> list = msgLogAnnexService.selectMsgLogAnnexList(msgLogAnnex);
        return getDataTable(list);
    }

    /**
     * 导出消息日志附件列表
     */
    @PreAuthorize("@ss.hasPermi('msg:logannex:export')")
    @Log(title = "msg.log.annex.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgLogAnnex msgLogAnnex) {
        List<MsgLogAnnex> list = msgLogAnnexService.selectMsgLogAnnexList(msgLogAnnex);
        ExcelUtil<MsgLogAnnex> util = new ExcelUtil<MsgLogAnnex>(MsgLogAnnex.class);
        return util.exportExcel(list, "logannex");
    }

    /**
     * 获取消息日志附件详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:logannex:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgLogAnnexService.selectMsgLogAnnexById(id));
    }

    /**
     * 下载附件
     */
    @Log(title = "msg.log.version.download", businessType = BusinessType.OTHER)
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        msgLogAnnexService.downloadLogAnnex(id, request, response);
    }
}
