package cn.eyecool.common.utils.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;

/**
 * 平台文件工具类(平台自定义的文件操作使用这个)
 * 
 * @author admin
 * @date 2019年12月24日
 */
public class PlatformFileUtils extends FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 根据路径获取图片的Base64
     *
     * @param path
     * @return
     */
    public static String getImageBase64(String path) {
        String stringBase64 = null;
        try {
            File file = new File(path);
            if (file != null && file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    byte[] fileBytes = new byte[fis.available()];
                    int size = fis.read(fileBytes);
                    LOG.debug("read size is [{}] bytes", size);
                    stringBase64 = Base64.encodeBase64String(fileBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fis.close();
                }
            }
        } catch (Exception ex) {
            LOG.error("Failed to get base64 of image：" + ex.getMessage());
        }
        return stringBase64;
    }
    /**
     * 读取本地图片，输出RFC2045 MIME‑Base64（VIID视图库图片上传专用）
     * @param filePath 本地图片绝对路径 D:/test/1.jpg
     * @return mimeBase64字符串
     * @throws IOException
     */
    public static String readImageToRfc2045Base64(String filePath){
        try {
            File file = new File(filePath);
            //读文件字节数组
            byte[] imageBytes = Files.readAllBytes(file.toPath());
            //✅MIME编码器，每76字符换行，GA/T1400标准
            String mimeBase64 = java.util.Base64.getMimeEncoder().encodeToString(imageBytes);
            return mimeBase64;
        } catch (IOException e) {
            LOG.error("获取图片的MIME‑Base64失败：" + e.getMessage());
            return Strings.EMPTY;
        }
    }
    /**
     * 将单行普通Base64字符串 转为 RFC2045 MIME Base64（76字符分割，\r\n换行）
     * @param plainBase64 不带换行的普通base64
     * @return RFC2045 MIME Base64
     */
    public static String toRfc2045MimeBase64(String plainBase64) {
        if (plainBase64 == null || plainBase64.isEmpty()) {
            return plainBase64;
        }
        plainBase64 = plainBase64.replaceAll("[\\r\\n\\s]+", "");
        StringBuilder sb = new StringBuilder();
        int len = plainBase64.length();
        int chunk = 76;
        for (int i = 0; i < len; i += chunk) {
            int end = Math.min(i + chunk, len);
            sb.append(plainBase64, i, end);
            if(end != len){
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
    /**
     * 根据MultipartFile获取图片的base64
     * 
     * @param multipartFile
     * @return
     */
    public static String getImageBase64(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String photoName = null;
        if (StringUtils.isNotBlank(originalFilename)) {
            photoName = originalFilename.split("\\.")[0];
            LOG.info("The uploaded image name is[{}]", photoName);
        }
        byte[] imageContent = null;
        try {
            imageContent = multipartFile.getBytes();
        } catch (Exception e) {
            LOG.error("image name[{}] ,there is en exception [{}]", photoName, e.getMessage());
        }
        return Base64.encodeBase64String(imageContent);
    }

    /**
     * 根据InputStream获取图片base64
     * 
     * @param inputStream
     * @return
     */
    public static String getImageBase64(InputStream in) {
        byte[] bytes = null;
        try {
            bytes = new byte[in.available()];
            int size = in.read(bytes);
            LOG.debug("read size is [{}] bytes", size);
        } catch (IOException e) {
            LOG.error("inputStream,Error converting Base64", e);
        }
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 压缩zip文件
     * 
     * @param entryList
     * @param zipFile
     */
    public static void zipFiles(List<ZipEntryFileModel> entryList, File zipFile) {
        // 判断压缩后的文件存在不，不存在则创建
        if (!zipFile.exists()) {
            try {
                boolean createResult = zipFile.createNewFile();
                if (!createResult) {
                    LOG.error("Failed to create compressed file");
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        // 创建 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 创建 ZipOutputStream
        ZipOutputStream zipOutputStream = null;
        // 创建 FileInputStream 对象
        FileInputStream fileInputStream = null;
        try {
            // 实例化 FileOutputStream 对象
            fileOutputStream = new FileOutputStream(zipFile);
            // 实例化 ZipOutputStream 对象
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件集合
            for (int i = 0; i < entryList.size(); i++) {
                // 将源文件数组中的当前文件读入 FileInputStream 流中
                ZipEntryFileModel zipEntryFileModel = entryList.get(i);
                if (null == zipEntryFileModel.getFile() || !zipEntryFileModel.getFile().exists()) {
                    continue;
                }
                fileInputStream = new FileInputStream(zipEntryFileModel.getFile());
                // 实例化 ZipEntry 对象，源文件数组中的当前文件
                String fileName = zipEntryFileModel.getFile().getName();
                if (StringUtils.isNotBlank(zipEntryFileModel.getFileName())) {
                    fileName = zipEntryFileModel.getFileName();
                }
                zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);
                // 获取字节数组
                if (null != zipEntryFileModel.getEncrypted() && zipEntryFileModel.getEncrypted()) {
                    byte[] bytes = PlatformCryptUtils.decryptStreamToBytes(fileInputStream);
                    zipOutputStream.write(bytes);
                } else {
                    // 该变量记录每次真正读的字节个数
                    int len;
                    // 定义每次读取的字节数组
                    byte[] buffer = new byte[1024];
                    while ((len = fileInputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != zipOutputStream) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
    }

    /**
     * 对zip类型的文件进行解压
     * 
     * @param multipartFile
     * @param zipFile
     * @return
     */
    public static List<FileModel> unzip(MultipartFile multipartFile) {
        // 判断文件是否为zip文件
        String filename = multipartFile.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            throw new CustomException(MessageUtils.message("platform.file.utils.filename.empty"));
        }
        if (!filename.endsWith("zip")) {
            LOG.info("The incoming file format is not a zip file" + filename);
            throw new CustomException(MessageUtils.message("platform.file.utils.file.format.error", filename));
        }
        try {
            return unzip(multipartFile.getInputStream());
        } catch (IOException e) {
            LOG.error("Failed to read the contents of the compressed package file, please confirm that the compressed package format is correct:" + filename, e);
            throw new CustomException(MessageUtils.message("platform.file.utils.zip.format.error", filename));
        }
    }

    /**
     * 对zip类型的文件流进行解压
     * 
     * @param inputStream
     * @return
     */
    public static List<FileModel> unzip(InputStream inputStream) {
        List<FileModel> fileModelList = new ArrayList<>();
        String zipFileName = null;
        // 对文件进行解析
        try {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream, Charset.forName("GBK"));
            BufferedInputStream bs = new BufferedInputStream(zipInputStream);
            ZipEntry zipEntry = null;
            byte[] bytes = null;
            // 获取zip包中的每一个zipFileEntry
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    throw new CustomException(MessageUtils.message("platform.file.utils.zip.not.allow.subfolders"));
                }
                zipFileName = zipEntry.getName();
                Assert.notNull(zipFileName, MessageUtils.message("platform.file.utils.zip.subfile.format.wrong"));
                bytes = new byte[(int)zipEntry.getSize()];
                bs.read(bytes, 0, (int)zipEntry.getSize());
                InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                FileModel fileModel = new FileModel();
                fileModel.setFileName(zipFileName);
                fileModel.setFileInputstream(byteArrayInputStream);
                fileModel.setFileSize(zipEntry.getSize());
                fileModelList.add(fileModel);
            }
            bs.close();
            zipInputStream.close();
        } catch (Exception e) {
            LOG.error("Failed to read the contents of the compressed package file, please confirm that the compressed package format is correct::" + zipFileName, e);
            throw new CustomException(MessageUtils.message("platform.file.utils.zip.format.error", zipFileName));
        }
        return fileModelList;
    }

    /**
     * 根据得到图片字节，获得图片后缀
     *
     * @param photoByte 图片字节
     * @return 图片后缀
     */
    public static String getImageFileExtendName(byte[] photoByte) {
        String strFileExtendName = ".jpg";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
            && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97)) {
            strFileExtendName = ".gif";
        } else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70)) {
            strFileExtendName = ".jpg";
        } else if ((photoByte[0] == 66) && (photoByte[1] == 77)) {
            strFileExtendName = ".bmp";
        } else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71)) {
            strFileExtendName = ".png";
        }
        return strFileExtendName;
    }

    /**
     * 根据图片Base64，获得图片后缀
     *
     * @param base64String 图片base64
     * @return 图片后缀
     */
    public static String getImageFileExtendName(String base64String) {
        byte[] bytes = Base64.decodeBase64(base64String);
        return getImageFileExtendName(bytes);
    }

    /**
     * 删除文件夹
     * 
     * @param dirPath
     */
    public static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isFile()) {
            boolean delete = file.delete();
            if (!delete) {
                LOG.error("Failed to delete file [{}] under folder [{}]", dirPath, file.getAbsolutePath());
            }
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                boolean delete = file.delete();
                if (!delete) {
                    LOG.error("Failed to delete file [{}] under folder [{}]", dirPath, file.getAbsolutePath());
                }
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                boolean delete = file.delete();
                if (!delete) {
                    LOG.error("Failed to delete folder [{}]", file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 计算base64图片的字节数(单位:字节) 传入的图片base64是去掉头部的data:image/png;base64,字符串
     * 
     * @param imageBase64Str
     * @return
     */
    public static Integer fileSize(String base64Str) {
        // 1.找到等号，把等号也去掉(=用来填充base64字符串长度用)
        Integer equalIndex = base64Str.indexOf("=");
        if (base64Str.indexOf("=") > 0) {
            base64Str = base64Str.substring(0, equalIndex);
        }
        // 2.原来的字符流大小，单位为字节
        Integer strLength = base64Str.length();
        // 3.计算后得到的文件流大小，单位为字节
        Integer size = strLength - (strLength / 8) * 2;
        return size;
    }
    /**
     * RFC2045 MIME Base64 转图片保存本地
     * @param rfc2045Base64Str 带换行的base64图片串，可包含 data:image/jpeg;base64, 前缀
     * @param saveBasePath 保存根目录 例：/data/upload/image/
     * @return 返回图片完整路径
     */
    public static String rfc2045Base64ToImage(String rfc2045Base64Str, String saveBasePath) {
        if (StringUtils.isBlank(rfc2045Base64Str)) {
            LOG.error("base64字符串为空");
            return null;
        }
        // 移除BOM头
        char bom = '\uFEFF';
        if(rfc2045Base64Str.length()>0 && rfc2045Base64Str.charAt(0)==bom){
            rfc2045Base64Str = rfc2045Base64Str.substring(1);
        }
        String base64 = rfc2045Base64Str;
        // 1. 移除data:image/jpeg;base64, 前缀
        if(base64.contains(",")){
            base64 = base64.substring(base64.indexOf(",") + 1);
        }
        base64 = base64.replace("\\r", "");
        base64 = base64.replace("\\n", "");
// 清除真正的换行、空格
        base64 = base64.replaceAll("[\\r\\n\\s]","");
        // 补齐base64填充等号
        int mod = base64.length() % 4;
        if(mod >0){
            base64 += "====".substring(mod);
        }
        try {
            byte[] imgBytes =  java.util.Base64.getDecoder().decode(base64);
            File dir = new File(saveBasePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = UUID.randomUUID() + ".jpg";
            String fullPath = saveBasePath + File.separator + fileName;
            try (FileOutputStream out = new FileOutputStream(fullPath)) {
                out.write(imgBytes);
            }
            LOG.info("图片保存成功：{}", fullPath);
            return fullPath;
        } catch (Exception e) {
            LOG.error("RFC2045 base64转图片失败", e);
            return null;
        }
    }
}
