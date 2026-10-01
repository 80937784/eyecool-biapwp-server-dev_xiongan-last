package cn.eyecool.statistic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eyecool.common.core.controller.BaseController;
import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.statistic.service.ISysUserStatisticService;

/**
 * 用户信息统计请求处理
 * 
 * @author mawj
 * @date 2021/08/26
 */
@RestController
@RequestMapping("/statistic/user")
public class SysUserStatisticController extends BaseController {

    @Autowired
    private ISysUserStatisticService sysUserStatisticService;

    /**
     * 统计用户数量
     * 
     * @return
     */
    @GetMapping("/countAll")
    public AjaxResult countAllUser() {
        int count = sysUserStatisticService.countSysUser();
        return AjaxResult.success(count);
    }
}
