package cn.eyecool.common.match;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.teso.drivers.iris.SsMobilePushAdapter;

import cn.eyecool.common.utils.DateUtils;
import cn.eyecool.fox.algs.SingleModelScore;
import cn.eyecool.fox.algs.multibiometricfusion.Fusion;
import cn.eyecool.fox.algs.multibiometricfusion.LibDriver;

/**
 * 自己通过teso调用算法库进行封装比对
 *
 * @Author zfx
 * @create 2020/12/2 17:52
 **/
@Service
public class MultiFusionFeatureService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MultiFusionFeatureService.class);

    /** 算法初始化多模态Fusion */
    private static Fusion fusion = null;
    /** 虹膜算法初始化 */
    private static SsMobilePushAdapter irisDriver = null;

    private static AtomicBoolean hasFusionInit = new AtomicBoolean(Boolean.FALSE);
    private static AtomicBoolean hasIrisInit = new AtomicBoolean(Boolean.FALSE);

    /**
     * 特征融合
     * 
     * @param faceFeature 人脸特征
     * @param irisFeature 虹膜特征
     * @return
     */
    public String fusionFeature(String faceFeature, String irisFeature) {
        byte[][] featuresToBeMerged =
            new byte[][] {Base64.getMimeDecoder().decode(faceFeature), Base64.getMimeDecoder().decode(irisFeature)};
        byte[] fusionFeature = fusion.mergeFeatures(featuresToBeMerged);
        return Base64.getEncoder().encodeToString(fusionFeature);
    }

    /**
     * 融合特征比对
     * 
     * @param algsFusionFeature
     * @param abisFusionFeature
     * @return
     */
    public float matchFusionFeatures(String algsFusionFeature, String abisFusionFeature) {
        LibDriver.FusionResult fusionResult = fusion.matchFusionFeatures(
            Base64.getMimeDecoder().decode(algsFusionFeature), Base64.getMimeDecoder().decode(abisFusionFeature));
        return fusionResult.getScore();
    }

    /**
     * 多模态中单模态比对
     * 
     * @param feature
     * @param template
     * @return
     */
    public float matchSingleModelFeature(String feature, String template) {
        SingleModelScore fusionResult = fusion.matchSingleModelFeature(Base64.getMimeDecoder().decode(feature),
            Base64.getMimeDecoder().decode(template));
        return fusionResult.getScore();
    }

    /**
     * 算法初始化
     * 
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!hasFusionInit.get()) {
            log.info("=============== init Fusion start[{}]============", DateUtils.getTime());
            try {
                fusion = new Fusion();
                if (!fusion.init()) {
                    // 初始化未成功，抛出错误
                    log.error("-----------Fusion init error---------");
                }
            } catch (Exception e) {
                log.error("Fusion init fail,does not affect the normal use of other modules, error[{}]", e.toString(),
                    e);
            }
            hasFusionInit.set(Boolean.TRUE);
        } else {
            log.info("-----------Fusion has already init---------");
        }
        if (!hasIrisInit.get()) {
            log.info("=============== IrisServer Iris start[{}]============", DateUtils.getTime());
            try {
                irisDriver = new SsMobilePushAdapter();
                irisDriver.setLibBaseName("SsLyIris");
                if (!irisDriver.init()) {
                    // 虹膜初始化未成功，抛出错误
                    log.error("-----------IrisServer init error---------");
                }
            } catch (Exception e) {
                log.error("IrisServer init fail,does not affect the normal use of other modules, error[{}]",
                    e.toString(), e);
            }
            hasIrisInit.set(Boolean.TRUE);
        } else {
            log.info("-----------IrisServer has already init---------");
        }

    }
}
