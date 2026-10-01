package cn.eyecool.common.exception;

import cn.eyecool.common.exception.user.UserException;

/**
 * @ClassName UserBioNotCollectException
 * @Description 自定义生物特征未采集异常
 * @Author csm
 * @date:2021-1-6
 * @Version 1.0
 */
public class UserBioNotCollectException extends UserException {
    private static final long serialVersionUID = 1L;

    public UserBioNotCollectException() {
        super("user.bio.not.collect", null);
    }

}
