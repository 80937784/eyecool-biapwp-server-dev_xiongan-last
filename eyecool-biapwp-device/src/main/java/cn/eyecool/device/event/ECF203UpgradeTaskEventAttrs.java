package cn.eyecool.device.event;

import lombok.Data;

/**
 * 203设备升级发布事件属性
 * 
 * @author mawj
 * @date 2021/10/29
 */
@Data
public class ECF203UpgradeTaskEventAttrs {

    /** 升级任务Id */
    private String taskId;
    /** 设备编码 */
    private String deviceNo;
    /** 文件路径 */
    private String filePath;
    /** 文件大小 */
    private Long fileSize;
    /** 文件MD5 */
    private String fileMd5;
    /** 源文件名 */
    private String originalFileName;
    /** 版本名称 */
    private String appName;
    /** 版本号 */
    private String version;

    public ECF203UpgradeTaskEventAttrs() {
        super();
    }

    public ECF203UpgradeTaskEventAttrs(String taskId, String deviceNo, String filePath, Long fileSize, String fileMd5,
        String originalFileName, String appName, String version) {
        super();
        this.taskId = taskId;
        this.deviceNo = deviceNo;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileMd5 = fileMd5;
        this.originalFileName = originalFileName;
        this.appName = appName;
        this.version = version;
    }

}
