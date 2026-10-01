package cn.eyecool.common.utils.file;

import java.io.File;
import java.io.Serializable;

/**
 * zip文件压缩项模型
 * 
 * @author admin
 * @date 2019年11月1日
 */
public class ZipEntryFileModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件 */
    private File file;
    /** 文件名 */
    private String fileName;
    /** 是否有加密 */
    private Boolean encrypted;

    public ZipEntryFileModel() {
        super();
    }

    public ZipEntryFileModel(File file, String fileName, Boolean encrypted) {
        super();
        this.file = file;
        this.fileName = fileName;
        this.encrypted = encrypted;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

}
