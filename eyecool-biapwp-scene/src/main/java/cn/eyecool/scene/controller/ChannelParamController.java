package cn.eyecool.scene.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.poi.ExcelUtil;
import cn.eyecool.scene.domain.ChannelInfo;
import cn.eyecool.scene.domain.ChannelParam;
import cn.eyecool.scene.enums.ChannelParamEnum;
import cn.eyecool.scene.service.IChannelInfoService;
import cn.eyecool.scene.service.IChannelParamService;

/**
 * 场景参数Controller
 * 
 * @author admin
 * @date 2021-03-22
 */
@RestController
@RequestMapping("/scene/channelParam")
public class ChannelParamController extends BaseController {
    @Autowired
    private IChannelParamService channelParamService;
    @Autowired
    private IChannelInfoService channelInfoService;

    /**
     * 查询场景参数列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChannelParam channelParam) {
        startPage();
        List<ChannelParam> list = channelParamService.selectChannelParamList(channelParam);
        list.stream().forEach(item -> {
            ChannelInfo channel = channelInfoService.selectChannelInfoById(item.getChannelId());
            item.setChannelName(channel.getChannelName());
        });
        return getDataTable(list);
    }

    /**
     * 导出场景参数列表
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:export')")
    @Log(title = "channel.scene.param.title", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(ChannelParam channelParam) {
        List<ChannelParam> list = channelParamService.selectChannelParamList(channelParam);
        list.stream().forEach(item -> {
            ChannelInfo channel = channelInfoService.selectChannelInfoById(item.getChannelId());
            item.setChannelName(channel.getChannelName());
        });
        ExcelUtil<ChannelParam> util = new ExcelUtil<ChannelParam>(ChannelParam.class);
        return util.exportExcel(list, "channelParam");
    }

    /**
     * 获取场景参数详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        ChannelParam param = channelParamService.selectChannelParamById(id);
        ChannelInfo channel = channelInfoService.selectChannelInfoById(param.getChannelId());
        param.setChannelName(channel.getChannelName());
        return AjaxResult.success(param);
    }

    /**
     * 新增场景参数
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:add')")
    @Log(title = "channel.scene.param.title", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChannelParam channelParam) {
        return toAjax(channelParamService.insertChannelParam(channelParam));
    }

    /**
     * 修改场景参数
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:edit')")
    @Log(title = "channel.scene.param.title", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChannelParam channelParam) {
        return toAjax(channelParamService.updateChannelParam(channelParam));
    }

    /**
     * 删除场景参数
     */
    @PreAuthorize("@ss.hasPermi('scene:channelParam:remove')")
    @Log(title = "channel.scene.param.title", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(channelParamService.deleteChannelParamByIds(ids));
    }

    /**
     * 查询场景参数列表
     */
    @GetMapping("/keyList")
    public AjaxResult keyList(String bioAttestType) {
        List<Map<String, Object>> list = ChannelParamEnum.list();
        if (StringUtils.isNotBlank(bioAttestType)) {
            list =
                list.stream().filter(it -> bioAttestType.equals(it.get("bioAttestType"))).collect(Collectors.toList());
        }
        return AjaxResult.success(list);
    }
}
