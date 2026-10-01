package cn.eyecool.basedata.service;

import cn.eyecool.basedata.domain.BasePersonInfo;
import cn.eyecool.basedata.vo.BaseVisitorInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 访客基础信息Service接口
 * 
 * @author zgy
 * @date 2021-11-01
 */
public interface IBaseVisitorInfoService {


    /**
     * 查询访客基础信息列表
     *
     * @param baseVisitorInfoVO 访客信息
     * @return 访客基础信息集合
     */
    List<BasePersonInfo> selectBaseVisitorInfoList(BaseVisitorInfoVO baseVisitorInfoVO);


    /**
     * 批量删除人员基础信息
     *
     * @param ids 需要删除的人员基础信息ID
     * @return 结果
     */
    int deleteBasePersonInfoByIds(String[] ids);

    /**
     * 批量删除人员基础信息
     *
     * @param id 需要删除的人员基础信息ID
     * @return 结果
     */
    int deleteBasePersonInfoById(String id);

    /**
     * 下载访客H5二维码
     *
     * @param request  response
     * @return 结果
     */
    void downloadQrCode(HttpServletRequest request, HttpServletResponse response)throws IOException;
}
