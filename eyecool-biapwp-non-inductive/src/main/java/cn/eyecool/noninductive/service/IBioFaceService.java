package cn.eyecool.noninductive.service;

import cn.eyecool.noninductive.domain.Bio1nSearchResult;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 人脸图像搜索接口
 *
 * @author 李强
 * @version [版本号, 2019年5月8日]
 * @since [应用/版本]
 */
public interface IBioFaceService {


    public void search(byte[] imageContent, String deviceSerialNo, JSONObject comment);

    public List<Bio1nSearchResult> search(byte[] feature, String groupName, Integer topCount, Double threshold);

    public List<Bio1nSearchResult> search(byte[] feature, String groupName, Integer topCount, Double threshold, String image);

    public JSONObject login(String username, String password) throws UnsupportedEncodingException;
}
