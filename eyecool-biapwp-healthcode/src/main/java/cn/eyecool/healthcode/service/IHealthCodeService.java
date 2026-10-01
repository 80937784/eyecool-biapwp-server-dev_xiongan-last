/*******************************************************************************
 * 系统名称 ： 系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : IHealthCodeService
 ******************************************************************************/
package cn.eyecool.healthcode.service;

import cn.eyecool.common.core.domain.AjaxResult;

/**
 * 健康码查询接口
 *
 * @author zhangfuxun
 * @since 2021/1/29 16:47.
 **/
public interface IHealthCodeService {
    /**
     * healthCodeSearch 健康码查询
     *
     * @param bizContent 请求对象
     * @return cn.eyecool.biapwp.common.core.domain.AjaxResult
     * @author zfx
     * @since 2021/1/30 17:25
     */
    AjaxResult healthCodeSearch(String jsonContent);
}
