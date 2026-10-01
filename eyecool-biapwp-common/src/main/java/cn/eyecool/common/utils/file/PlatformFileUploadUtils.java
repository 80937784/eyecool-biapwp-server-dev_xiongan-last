package cn.eyecool.common.utils.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.eyecool.common.config.EyecoolConfig;
import cn.eyecool.common.exception.CustomException;
import cn.eyecool.common.exception.file.FileNameLengthLimitExceededException;
import cn.eyecool.common.exception.file.FileSizeLimitExceededException;
import cn.eyecool.common.exception.file.InvalidExtensionException;
import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.common.utils.MessageUtils;
import cn.eyecool.common.utils.PlatformCryptUtils;
import cn.eyecool.common.utils.StringUtils;
import cn.eyecool.common.utils.sign.Md5Utils;

/**
 * 文件上传工具类
 * 
 * @author admin
 */
public class PlatformFileUploadUtils {
    /**
     * 默认大小 50M,mawenjun修改为200M-20191227
     */
    public static final long DEFAULT_MAX_SIZE = 200L * 1024 * 1024;

    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 默认上传的地址
     */
    private static String defaultBaseDir = EyecoolConfig.getProfile();

    private static int counter = 0;

    public static void setDefaultBaseDir(String defaultBaseDir) {
        PlatformFileUploadUtils.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件名称
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException {
        try {
            return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir 相对应用的基目录
     * @param file 上传的文件
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file) throws IOException {
        try {
            return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir 相对应用的基目录
     * @param file 上传的文件
     * @param extension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太长
     * @throws IOException 比如读写文件出错时
     * @throws InvalidExtensionException 文件校验异常
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
        throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
        InvalidExtensionException {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new CustomException(MessageUtils.message("file.upload.filename.empty"));
        }
        int fileNamelength = originalFilename.length();
        if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, allowedExtension);

        String fileName = extractFilename(file);

        File desc = getAbsoluteFile(baseDir, fileName);
        file.transferTo(desc);
        String pathFileName = getPathFileName(baseDir, fileName);
        return pathFileName;
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isBlank(extension)) {
            extension = MimeTypeUtils.getExtension(file.getContentType());
            fileName = fileName + "." + extension;
        }
        return extractFilename(fileName);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(FileModel fileModel) {
        String fileName = fileModel.getFileName();
        return extractFilename(fileName);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        fileName = DateUtils.dateTime() + "/" + encodingFilename(fileName) + "." + extension;
        return fileName;
    }

    private static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException {
        File desc = new File(uploadDir + File.separator + fileName);

        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            boolean createResult = desc.createNewFile();
            if (!createResult) {
                throw new CustomException(MessageUtils.message("file.upload.create.file.failed"));
            }
        }
        return desc.getAbsoluteFile();
    }

    private static final String getPathFileName(String uploadDir, String fileName) throws IOException {
        // int dirLastIndex = uploadDir.lastIndexOf("/") + 1;
        // String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
        // String pathFileName = Constants.RESOURCE_PREFIX + "/" + currentDir +
        // "/" + fileName;
        // return pathFileName;
        return uploadDir + fileName;
    }

    /**
     * 编码文件名
     */
    private static final String encodingFilename(String fileName) {
        fileName = fileName.replace("_", " ");
        fileName = Md5Utils.hash(fileName + System.nanoTime() + counter++);
        return fileName;
    }

    /**
     * 文件大小校验
     *
     * @param file 上传的文件
     * @return
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws InvalidExtensionException
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
        throws FileSizeLimitExceededException, InvalidExtensionException {
        long size = file.getSize();
        if (DEFAULT_MAX_SIZE != -1 && size > DEFAULT_MAX_SIZE) {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                    fileName);
            } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                    fileName);
            } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                    fileName);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     *
     * @param extension
     * @param allowedExtension
     * @return
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension) {
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件名的后缀
     * 
     * @param file 表单文件
     * @return 后缀名
     */
    public static final String getExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(file.getContentType());
        }
        return extension;
    }

    /**
     * 文件上传
     * 
     * @param baseDir
     * @param fileModel
     * @return
     * @throws IOException
     * @throws FileNameLengthLimitExceededException
     */
    public static final String upload(String baseDir, FileModel fileModel)
        throws IOException, FileNameLengthLimitExceededException {
        String originalFilename = fileModel.getFileName();
        // 拓展名
        int fileNamelength = originalFilename.length();
        if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        String fileName = extractFilename(fileModel);
        File desc = getAbsoluteFile(baseDir, fileName);

        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(fileModel.getFileInputstream());
            out = new BufferedOutputStream(new FileOutputStream(desc));

            int len = -1;
            byte[] b = new byte[1024];
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
        } finally {
            if (null != in) {
                in.close();
            }
            if (null != out) {
                out.flush();
                out.close();
            }
        }
        String pathFileName = getPathFileName(baseDir, fileName);
        return pathFileName;
    }

    /**
     * 文件上传
     * 
     * @param baseDir 基础目录
     * @param originalFilename 源文件名
     * @param base64 图片base64
     * @return 文件名称
     * @throws IOException
     * @throws FileNameLengthLimitExceededException
     */
    public static final String upload(String baseDir, String originalFilename, String base64)
        throws IOException, FileNameLengthLimitExceededException {
        // 拓展名
        int fileNamelength = originalFilename.length();
        if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }
        String fileName = extractFilename(originalFilename);
        File desc = getAbsoluteFile(baseDir, fileName);
        byte[] imageContent = Base64.decodeBase64(base64);
        FileUtils.writeByteArrayToFile(desc, imageContent);
        String pathFileName = getPathFileName(baseDir, fileName);
        return pathFileName;
    }

    /**
     * Description uploadWithFileName 按照文件名存储照片
     * 
     * @param encrypted 是否加密, filename 文件名, imageBase64, baseDir 存储路径
     * @return java.lang.String
     * @author zfx
     * @date 2020/3/20 11:14
     */
    public static final String uploadWithFileName(boolean encrypted, String filename, String imageBase64,
        String baseDir) throws IOException, FileNameLengthLimitExceededException {
        if (encrypted) {
            imageBase64 = PlatformCryptUtils.encryptImageBase64(imageBase64);
        }
        if (!baseDir.endsWith(File.separator)) {
            baseDir = baseDir + File.separator;
        }
        String fileName = DateUtils.dateTime() + "/" + filename;
        File desc = getAbsoluteFile(baseDir, fileName);
        byte[] imageContent = Base64.decodeBase64(imageBase64);
        FileUtils.writeByteArrayToFile(desc, imageContent);
        return getPathFileName(baseDir, fileName);
    }
}