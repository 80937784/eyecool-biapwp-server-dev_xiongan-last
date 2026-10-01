package cn.eyecool.common.exception;

import cn.eyecool.common.exception.user.UserException;

public class UserBioNotMatchException extends UserException {
    private static final long serialVersionUID = 1L;

    public UserBioNotMatchException() {
        super("user.bio.not.match", null);
    }

}