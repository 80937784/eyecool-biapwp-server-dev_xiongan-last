package cn.eyecool.noninductive.sdk.HCNetCamera;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Pointer;

import cn.eyecool.noninductive.disruptor.event.FaceSearchEvent;
import cn.eyecool.noninductive.disruptor.queue.FaceSearchDisruptorQueue;
import cn.eyecool.noninductive.sdk.HCNetCamera.service.HCNetSDK;
import cn.eyecool.system.service.ISysConfigService;

@Component
public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {

    @Value("${captureImageRootPath:./}")
    private String captureImageRootPath;
    @Autowired
    private ISysConfigService configService;

    // 报警信息回调函数
    private static final Logger LOGGER = LoggerFactory.getLogger(FMSGCallBack_V31.class);

    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
        Pointer pUser) {
        LOGGER.info("触发回调,内部触发类型[{}]", lCommand);
        this.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        return true;
    }

    private void alarmDataHandle(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen,
        Pointer pUser) {
        String alarmTem = configService.selectConfigByKey("bio.face.hcnet.alarmtem");
        if (alarmTem == null) {
            alarmTem = "37.0";
        }
        String sAlarmType = "lCommand=0x" + Integer.toHexString(lCommand);
        LOGGER.info("sAlarmType: [{}]", sAlarmType);
        String[] newRow = new String[3];
        // 报警时间
        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String[] sIP;
        if (lCommand == HCNetSDK.COMM_THERMOMETRY_ALARM) {
            HCNetSDK.NET_DVR_THERMOMETRY_ALARM struThermAlarm = new HCNetSDK.NET_DVR_THERMOMETRY_ALARM();
            struThermAlarm.write();
            Pointer pThermAlarm = struThermAlarm.getPointer();
            pThermAlarm.write(0, pAlarmInfo.getByteArray(0, struThermAlarm.size()), 0, struThermAlarm.size());
            struThermAlarm.read();

            StringBuilder strRegion = new StringBuilder(", 区域坐标:");
            if (struThermAlarm.struRegion.dwPointNum != 4) {
                return;
            }
            for (int i = 0; i < struThermAlarm.struRegion.dwPointNum; i++) {
                strRegion.append("[").append(struThermAlarm.struRegion.struPos[i].fX).append(",")
                    .append(struThermAlarm.struRegion.struPos[i].fY).append("]");
            }

            sAlarmType = sAlarmType + "：温度报警信息, 通道号:" + struThermAlarm.dwChannel + ", 规则标定类型:"
                + struThermAlarm.byRuleCalibType + ", 当前温度:" + struThermAlarm.fCurrTemperature + strRegion;
            LOGGER.info("体温：[{}]", struThermAlarm.fCurrTemperature);
            LOGGER.info("配置规则温度：[{}]", struThermAlarm.fRuleTemperature);
            LOGGER.info(sAlarmType);
            newRow[0] = dateFormat.format(today);
            // 报警类型
            newRow[1] = sAlarmType;
            // 报警设备IP地址
            sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
            newRow[2] = sIP[0];

            // 保存可见光图片
            if ((struThermAlarm.dwPicLen > 0) && (struThermAlarm.pPicBuff != null)) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                String newName = sf.format(new Date());
                String fileDir = captureImageRootPath + File.separator + new String(pAlarmer.sDeviceIP).trim();
                if (!new File(fileDir).exists()) {
                    new File(fileDir).mkdirs();
                }
                String filename = fileDir + File.separator + newName + "_ThermCapture.jpg";
                try (FileOutputStream fout = new FileOutputStream(filename)) {
                    // 将字节写入文件
                    long offset = 0;
                    ByteBuffer buffers = struThermAlarm.pPicBuff.getByteBuffer(offset, struThermAlarm.dwPicLen);
                    byte[] bytes = new byte[struThermAlarm.dwPicLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    byte[] cutPictureBytes = cutPicture(filename, struThermAlarm);
                    JSONObject comment = new JSONObject();
                    comment.put("fCurrTemperature",
                        BigDecimal.valueOf(struThermAlarm.fCurrTemperature).setScale(1, RoundingMode.HALF_UP));
                    comment.put("fRuleTemperature", struThermAlarm.fRuleTemperature);
                    FaceSearchEvent.FaceSearchMessage faceSearchMessage =
                        new FaceSearchEvent.FaceSearchMessage(cutPictureBytes, new String(pAlarmer.sDeviceIP), comment);
                    // 将人脸搜索1：N的请求放入队列中
                    FaceSearchDisruptorQueue.publishEvent(faceSearchMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (lCommand == HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT) {
            HCNetSDK.NET_VCA_FACESNAP_RESULT strFaceResult = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
            strFaceResult.write();
            Pointer pFaceResult = strFaceResult.getPointer();
            pFaceResult.write(0, pAlarmInfo.getByteArray(0, strFaceResult.size()), 0, strFaceResult.size());
            strFaceResult.read();
            HCNetSDK.NET_VCA_HUMAN_FEATURE struFeature = strFaceResult.struFeature;
            LOGGER.info("age:[{}],sex:[{}],mask:[{}]", struFeature.byAge, struFeature.bySex, struFeature.byMask);

            HCNetSDK.NET_VCA_FACESNAP_ADDINFO netVcaFacesnapAddinfo = new HCNetSDK.NET_VCA_FACESNAP_ADDINFO();
            netVcaFacesnapAddinfo.write();
            Pointer netVcaFacesnapAddinfoPointer = netVcaFacesnapAddinfo.getPointer();
            netVcaFacesnapAddinfoPointer.write(0,
                strFaceResult.pAddInfoBuffer.getByteArray(0, netVcaFacesnapAddinfo.size()), 0,
                netVcaFacesnapAddinfo.size());
            netVcaFacesnapAddinfo.read();
            JSONObject comment = new JSONObject();
            comment.put("fCurrTemperature",
                BigDecimal.valueOf(netVcaFacesnapAddinfo.fFaceTemperature).setScale(1, RoundingMode.HALF_UP));
            comment.put("fRuleTemperature",
                BigDecimal.valueOf(netVcaFacesnapAddinfo.fAlarmTemperature).setScale(1, RoundingMode.HALF_UP));
            String fileDir = captureImageRootPath + new String(pAlarmer.sDeviceIP).trim();
            if (!new File(fileDir).exists()) {
                new File(fileDir).mkdirs();
            }
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
            String newName = sf.format(new Date());
            if (strFaceResult.dwFacePicLen > 0 && strFaceResult.pBuffer1 != null) {
                String filename = fileDir + File.separator + newName + "_new_fThermCapture.jpg";
                LOGGER.info("filename:{}", filename);
                try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
                    long offset = 0;
                    ByteBuffer buffers = strFaceResult.pBuffer1.getByteBuffer(offset, strFaceResult.dwFacePicLen);
                    byte[] bytes = new byte[strFaceResult.dwFacePicLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fileOutputStream.write(bytes);
                    FaceSearchEvent.FaceSearchMessage faceSearchMessage =
                        new FaceSearchEvent.FaceSearchMessage(bytes, new String(pAlarmer.sDeviceIP), comment);
                    // 将人脸搜索1：N的请求放入队列中
                    FaceSearchDisruptorQueue.publishEvent(faceSearchMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] cutPicture(String srcFilename, float fX, float fY, float fWidth, float fHeight)
        throws IOException {
        String destFile = srcFilename.substring(0, srcFilename.lastIndexOf(".")) + "_cut.jpg";
        FileInputStream fileInputStream = new FileInputStream(srcFilename);
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader imageReader = iterator.next();
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
        imageReader.setInput(imageInputStream, true);
        ImageReadParam param = imageReader.getDefaultReadParam();
        Rectangle rect = new Rectangle(Math.round(fX), Math.round(fY), Math.round(fWidth), Math.round(fHeight));
        param.setSourceRegion(rect);
        BufferedImage bi = imageReader.read(0, param);
        ImageIO.write(bi, "jpg", new File(destFile));
        fileInputStream.close();
        imageInputStream.close();
        return FileUtils.readFileToByteArray(new File(destFile));

    }

    public static byte[] cutPicture(String srcFilename, HCNetSDK.NET_DVR_THERMOMETRY_ALARM struThermAlarm)
        throws IOException {
        BufferedImage sourceImg = ImageIO.read(new FileInputStream(srcFilename));
        int width = sourceImg.getWidth(); // 源图宽度
        int height = sourceImg.getHeight(); // 源图高度
        float fx = struThermAlarm.struRegion.struPos[0].fX * width;
        float fy = struThermAlarm.struRegion.struPos[0].fY * height;
        float cutWidth = struThermAlarm.struRegion.struPos[2].fX - struThermAlarm.struRegion.struPos[0].fX;
        float cutHeight = struThermAlarm.struRegion.struPos[2].fY - struThermAlarm.struRegion.struPos[0].fY;
        return cutPicture(srcFilename, fx, fy, cutWidth * width, cutHeight * height);

    }

}
