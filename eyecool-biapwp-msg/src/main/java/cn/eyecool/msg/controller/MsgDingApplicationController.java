package cn.eyecool.msg.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.annotation.Log;
import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.common.core.page.TableDataInfo;
import cn.eyecool.common.enums.BusinessType;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.msg.domain.MsgDingApplication;
import cn.eyecool.msg.service.IMsgDingApplicationService;

/**
 * 钉钉微应用Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/dingApplication")
public class MsgDingApplicationController extends BaseController {
    @Autowired
    private IMsgDingApplicationService msgDingApplicationService;

    /**
     * 查询钉钉微应用列表
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgDingApplication msgDingApplication) {
        startPage();
        List<MsgDingApplication> list = msgDingApplicationService.selectMsgDingApplicationList(msgDingApplication);
        return getDataTable(list);
    }

    /**
     * 导出钉钉微应用列表
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:export')")
    @Log(title = "ding.app.name", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgDingApplication msgDingApplication) {
        List<MsgDingApplication> list = msgDingApplicationService.selectMsgDingApplicationList(msgDingApplication);
        ExcelUtil<MsgDingApplication> util = new ExcelUtil<MsgDingApplication>(MsgDingApplication.class);
        return util.exportExcel(list, "dingApplication");
    }

    /**
     * 获取钉钉微应用详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgDingApplicationService.selectMsgDingApplicationById(id));
    }

    /**
     * 新增钉钉微应用
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:add')")
    @Log(title = "ding.app.name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgDingApplication msgDingApplication) {
        return toAjax(msgDingApplicationService.insertMsgDingApplication(msgDingApplication));
    }

    /**
     * 修改钉钉微应用
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:edit')")
    @Log(title = "ding.app.name", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgDingApplication msgDingApplication) {
        return toAjax(msgDingApplicationService.updateMsgDingApplication(msgDingApplication));
    }

    /**
     * 删除钉钉微应用
     */
    @PreAuthorize("@ss.hasPermi('msg:dingApplication:remove')")
    @Log(title = "ding.app.name", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(msgDingApplicationService.deleteMsgDingApplicationByIds(ids));
    }

    /**
     * 查询所有钉钉微应用列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(MsgDingApplication msgDingApplication) {
        List<MsgDingApplication> list = msgDingApplicationService.selectMsgDingApplicationList(msgDingApplication);
        return AjaxResult.success(list);
    }
}
