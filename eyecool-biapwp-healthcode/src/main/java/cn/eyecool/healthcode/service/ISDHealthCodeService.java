/*******************************************************************************
 * 系统名称 ： 后台管理系统 开发部门 ： 山东眼神智能科技有限公司 文件名称 : ISDHealthCodeService
 ******************************************************************************/
package cn.eyecool.healthcode.service;

import cn.eyecool.common.core.domain.AjaxResult;
import cn.eyecool.healthcode.param.HealthCodeRequest;

/**
 * 山东健康码查询类
 *
 * @author zfx
 * @since 2021/3/5 17:28
 **/
public interface ISDHealthCodeService {
    /**
     * querySddzjkm 查询济南健康码
     * 
     * @param healthCodeRequest
     * @return cn.eyecool.biapwp.common.core.domain.http.AjaxResult
     * @author zfx
     * @since 2021/3/5 17:25
     */
    AjaxResult querySddzjkm(HealthCodeRequest healthCodeRequest, String url);
}
