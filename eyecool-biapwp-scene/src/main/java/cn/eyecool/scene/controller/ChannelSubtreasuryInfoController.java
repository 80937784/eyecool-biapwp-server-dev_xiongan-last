package cn.eyecool.scene.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelSubtreasuryInfo;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelSubtreasuryInfoService;

/**
 * 子场景信息Controller
 * 
 * @author admin
 * @date 2021-03-22
 */
@RestController
@RequestMapping("/scene/subtreasury")
public class ChannelSubtreasuryInfoController extends BaseController {

    @Autowired
    private IChannelSubtreasuryInfoService channelSubtreasuryInfoService;
    @Autowired
    private IChannelInfoService channelInfoService;

    /**
     * 查询子场景信息列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        startPage();
        List<ChannelSubtreasuryInfo> list =
            channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(channelSubtreasuryInfo);
        list.stream().forEach(item -> {
            ChannelInfo channel = channelInfoService.selectChannelInfoById(item.getChannelId());
            item.setChannelName(channel.getChannelName());
        });
        return getDataTable(list);
    }

    /**
     * 导出子场景信息列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:export')")
    @Log(title = "channel.sub.scene.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        List<ChannelSubtreasuryInfo> list =
            channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(channelSubtreasuryInfo);
        list.stream().forEach(item -> {
            ChannelInfo channel = channelInfoService.selectChannelInfoById(item.getChannelId());
            item.setChannelName(channel.getChannelName());
        });
        ExcelUtil<ChannelSubtreasuryInfo> util = new ExcelUtil<ChannelSubtreasuryInfo>(ChannelSubtreasuryInfo.class);
        return util.exportExcel(list, "subtreasury");
    }

    /**
     * 获取子场景信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ChannelSubtreasuryInfo subtreasuryInfo = channelSubtreasuryInfoService.selectChannelSubtreasuryInfoById(id);
        ChannelInfo channel = channelInfoService.selectChannelInfoById(subtreasuryInfo.getChannelId());
        subtreasuryInfo.setChannelName(channel.getChannelName());
        return AjaxResult.success(subtreasuryInfo);
    }

    /**
     * 新增子场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:add')")
    @Log(title = "channel.sub.scene.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        return toAjax(channelSubtreasuryInfoService.insertChannelSubtreasuryInfo(channelSubtreasuryInfo));
    }

    /**
     * 修改子场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:edit')")
    @Log(title = "channel.sub.scene.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChannelSubtreasuryInfo channelSubtreasuryInfo) {
        return toAjax(channelSubtreasuryInfoService.updateChannelSubtreasuryInfo(channelSubtreasuryInfo));
    }

    /**
     * 删除子场景信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channel:remove')")
    @Log(title = "channel.sub.scene.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(channelSubtreasuryInfoService.deleteChannelSubtreasuryInfoByIds(ids));
    }

    /**
     * 查询子场景信息列表
     */
    @GetMapping("/listByChannel/{channelId}")
    public AjaxResult list(@PathVariable String channelId) {
        ChannelSubtreasuryInfo info = new ChannelSubtreasuryInfo();
        info.setChannelId(channelId);
        List<ChannelSubtreasuryInfo> list = channelSubtreasuryInfoService.selectChannelSubtreasuryInfoList(info);
        return AjaxResult.success(list);
    }

    /**
     * 生成场景编码
     * 
     * @return
     */
    @GetMapping("/genSubsceneCode/{sceneCode}")
    public AjaxResult genSubsceneCode(@PathVariable("sceneCode") String sceneCode) {
        String subSceneCode = channelSubtreasuryInfoService.generateSubsceneCode(sceneCode);
        Map<String, String> map = new HashMap<>();
        map.put("subSceneCode", subSceneCode);
        return AjaxResult.success(map);
    }
}
