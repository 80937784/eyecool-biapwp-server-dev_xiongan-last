package cn.eyecool.basedata.manager;

import java.util.List;

import com.eyecool.abis.callmicroservice.common.datamanager.PersonData;

import cn.eyecool.basedata.domain.BasePersonFace;
import cn.eyecool.basedata.domain.BasePersonFinger;
import cn.eyecool.basedata.domain.BasePersonIris;

/**
 * 人员信息DataManager逻辑服务
 * 
 * @author admin
 * @date 2019年11月20日
 */
public interface IPersonDataManagerLogicService {

    /**
     * 新增人员信息到datamanager
     * 
     * @param uniqueId 人员唯一标识
     * @param personName 人员姓名
     * @param destFace 人脸信息
     * @param destFingerList 指纹信息列表
     * @param destIris 虹膜信息
     */
    public void insertPersonData(String uniqueId, String personName, BasePersonFace destFace,
        List<BasePersonFinger> destFingerList, BasePersonIris destIris);

    /**
     * 修改datamanager人员信息
     * 
     * @param uniqueId 人员唯一标识
     * @param personName 人员姓名
     * @param destFace 人脸信息
     * @param destFingerList 指纹信息列表
     * @param destIris 虹膜信息
     * @param deletedFeatureIds 需要删除的特征ID列表(生物信息主键)
     */
    public void updatePersonData(String uniqueId, String personName, BasePersonFace destFace,
        List<BasePersonFinger> destFingerList, BasePersonIris destIris, List<String> deletedFeatureIds);

    /**
     * 根据唯一标识删除人员信息
     * 
     * @param uniqueId 人员唯一标识
     */
    public void deletePersonInfo(String uniqueId);

    /**
     * 删除人脸信息
     * 
     * @param uniqueId 人员唯一标识
     * @param faceId 人脸主键（特征ID）
     */
    public void deletePersonFace(String uniqueId, String faceId);

    /**
     * 删除指纹信息
     * 
     * @param uniqueId 人员唯一标识
     * @param fingerNo 指纹编号
     * @param fingerId 指纹主键（特征ID）
     */
    public void deletePersonFinger(String uniqueId, String fingerNo, String fingerId);

    /**
     * 根据人员标识删除人员全部指纹信息
     * 
     * @param uniqueId 人员唯一标识
     */
    public void clearPersonFinger(String uniqueId);

    /**
     * 删除虹膜信息
     * 
     * @param uniqueId 人员唯一标识
     * @param irisId 虹膜主键（特征ID）
     */
    public void deletePersonIris(String uniqueId, String irisId);

    /**
     * 根据人员标识删除人员全部虹膜信息
     * 
     * @param uniqueId 人员唯一标识
     */
    public void clearPersonIris(String uniqueId);

    /**
     * 添加库人员（绑定人库关系）
     * 
     * @param libraryId 库ID
     * @param uniqueId 人员唯一标识
     */
    public void addLibraryPerson(String libraryId, String uniqueId);

    /**
     * 删除库人员（解绑人库关系）
     * 
     * @param libraryId 库ID
     * @param uniqueId 人员唯一标识
     */
    public void deleteLibraryPerson(String libraryId, String uniqueId);

    /**
     * 删除库
     * 
     * @param libraryId 库ID
     */
    public void deleteLibrary(String libraryId);

    /**
     * 生成人员特征ID列表
     * 
     * @param faceId 人脸ID
     * @param fingerList 指纹信息列表
     * @param irisId 虹膜信息ID
     * 
     * @return
     */
    public List<String> getPersonFeatureIds(String faceId, List<BasePersonFinger> fingerList, String irisId);

    /**
     * 根据唯一标识查询人员信息
     * 
     * @param uniqueId 人员唯一标识
     * @return
     */
    public PersonData getPersonData(String uniqueId);

    /**
     * 查询人库关系是否存在
     * 
     * @param libraryId 库ID
     * @param uniqueId 人员唯一标识
     * @return
     */
    public boolean queryLibraryPersonExists(String libraryId, String uniqueId);

}
