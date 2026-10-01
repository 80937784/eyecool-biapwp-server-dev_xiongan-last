package cn.eyecool.device.vo;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 功能描述 :返回值信息
 *
 * @author 段存明
 * @date 2018-10-15 12:05
 **/
public class ResultMsgVO {
    private String code;
    private String msg;
    private String redirect;
    private Object data;
    private Object item;

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public ResultMsgVO() {
        this.code = "0000";
        this.msg = "成功";
    }

    public ResultMsgVO(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultMsgVO(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回OK，无数据
     *
     * @return ResultMsg
     */
    public static ResultMsgVO createOkMsg() {
        return new ResultMsgVO();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.PrettyFormat);
    }

    /**
     * 返回一个returnData为空对象的成功消息的json
     *
     * @return ResultMsg
     */
    public static JSONObject newJson() {
        return new JSONObject();
    }

    /**
     * 查询分页结果后的封装工具方法
     *
     * @param requestJson 请求参数json,此json在之前调用fillPageParam 方法时,已经将pageRow放入
     * @param list 查询分页对象list
     * @param totalCount 查询出记录的总条数
     */
    public static JSONObject successPage(final JSONObject requestJson, List<JSONObject> list, int totalCount) {
        int pageRow = requestJson.getIntValue("pageRow");
        int totalPage = getPageCounts(pageRow, totalCount);
        JSONObject result = newJson();
        JSONObject returnData = new JSONObject();
        returnData.put("list", list);
        returnData.put("totalCount", totalCount);
        returnData.put("totalPage", totalPage);
        result.put("pageData", returnData);
        return result;
    }

    /**
     * 获取总页数
     *
     * @param pageRow 每页行数
     * @param itemCount 结果的总条数
     * @return int
     */
    public static int getPageCounts(int pageRow, int itemCount) {
        if (itemCount == 0) {
            return 1;
        }
        return itemCount % pageRow > 0 ? itemCount / pageRow + 1 : itemCount / pageRow;
    }

}
