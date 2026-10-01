package cn.eyecool.common.utils.file;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 存储每个解压后文件的model
 * 
 * @author admin
 * @date 2019年10月14日
 */
public class FileModel implements Serializable {
    private static final long serialVersionUID = 13846812783412684L;

    private String fileName; // 解压后文件的名字
    private String fileType; // 文件类型
    private InputStream fileInputstream; // 解压后每个文件的输入流
    private Long fileSize;// 文件大小

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public InputStream getFileInputstream() {
        return this.fileInputstream;
    }

    public void setFileInputstream(InputStream fileInputstream) {
        this.fileInputstream = fileInputstream;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "FileModel{fileName=\'" + this.fileName + '\'' + ", fileType=\'" + this.fileType + '\''
            + ", fileInputstream=" + this.fileInputstream + '}';
    }
}
