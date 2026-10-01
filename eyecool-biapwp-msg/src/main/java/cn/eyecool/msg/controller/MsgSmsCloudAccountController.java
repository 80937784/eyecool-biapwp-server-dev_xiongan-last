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
import cn.eyecool.msg.domain.MsgSmsCloudAccount;
import cn.eyecool.msg.service.IMsgSmsCloudAccountService;

/**
 * 短信云账户Controller
 * 
 * @author admin
 * @date 2021-04-15
 */
@RestController
@RequestMapping("/msg/smsCloudAccount")
public class MsgSmsCloudAccountController extends BaseController {
    @Autowired
    private IMsgSmsCloudAccountService msgSmsCloudAccountService;

    /**
     * 查询短信云账户列表
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:list')")
    @GetMapping("/list")
    public TableDataInfo list(MsgSmsCloudAccount msgSmsCloudAccount) {
        startPage();
        List<MsgSmsCloudAccount> list = msgSmsCloudAccountService.selectMsgSmsCloudAccountList(msgSmsCloudAccount);
        return getDataTable(list);
    }

    /**
     * 导出短信云账户列表
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:export')")
    @Log(title = "msg.send.clound.account.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MsgSmsCloudAccount msgSmsCloudAccount) {
        List<MsgSmsCloudAccount> list = msgSmsCloudAccountService.selectMsgSmsCloudAccountList(msgSmsCloudAccount);
        ExcelUtil<MsgSmsCloudAccount> util = new ExcelUtil<MsgSmsCloudAccount>(MsgSmsCloudAccount.class);
        return util.exportExcel(list, "smsCloudAccount");
    }

    /**
     * 获取短信云账户详细信息
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return AjaxResult.success(msgSmsCloudAccountService.selectMsgSmsCloudAccountById(id));
    }

    /**
     * 新增短信云账户
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:add')")
    @Log(title = "msg.send.clound.account.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MsgSmsCloudAccount msgSmsCloudAccount) {
        return toAjax(msgSmsCloudAccountService.insertMsgSmsCloudAccount(msgSmsCloudAccount));
    }

    /**
     * 修改短信云账户
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:edit')")
    @Log(title = "msg.send.clound.account.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MsgSmsCloudAccount msgSmsCloudAccount) {
        return toAjax(msgSmsCloudAccountService.updateMsgSmsCloudAccount(msgSmsCloudAccount));
    }

    /**
     * 删除短信云账户
     */
    @PreAuthorize("@ss.hasPermi('msg:smsCloudAccount:remove')")
    @Log(title = "msg.send.clound.account.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(msgSmsCloudAccountService.deleteMsgSmsCloudAccountByIds(ids));
    }

    /**
     * 查询所有短信云账户列表
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(MsgSmsCloudAccount msgSmsCloudAccount) {
        List<MsgSmsCloudAccount> list = msgSmsCloudAccountService.selectMsgSmsCloudAccountList(msgSmsCloudAccount);
        return AjaxResult.success(list);
    }

}
