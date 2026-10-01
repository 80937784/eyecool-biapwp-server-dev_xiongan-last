package cn.eyecool.basedata.mapper;

import java.util.List;
import cn.eyecool.basedata.domain.BasePersonCert;

/**
 * 人员证件信息Mapper接口
 * 
 * @author mawj
 * @date 2021-01-27
 */
public interface BasePersonCertMapper 
{
    /**
     * 查询人员证件信息
     * 
     * @param id 人员证件信息ID
     * @return 人员证件信息
     */
    public BasePersonCert selectBasePersonCertById(String id);

    /**
     * 查询人员证件信息列表
     * 
     * @param basePersonCert 人员证件信息
     * @return 人员证件信息集合
     */
    public List<BasePersonCert> selectBasePersonCertList(BasePersonCert basePersonCert);

    /**
     * 新增人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    public int insertBasePersonCert(BasePersonCert basePersonCert);

    /**
     * 修改人员证件信息
     * 
     * @param basePersonCert 人员证件信息
     * @return 结果
     */
    public int updateBasePersonCert(BasePersonCert basePersonCert);

    /**
     * 删除人员证件信息
     * 
     * @param id 人员证件信息ID
     * @return 结果
     */
    public int deleteBasePersonCertById(String id);

    /**
     * 批量删除人员证件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBasePersonCertByIds(String[] ids);
}
