package com.mall.commons.tool.exception;

/**
 * User：zhouchen
 * Time: 2020/5/12  23:27
 * Description:
 */
public class UpdateException extends BaseBusinessException {

    public UpdateException() {
        super();
    }

    public UpdateException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public UpdateException(Throwable arg0) {
        super(arg0);
    }

    public UpdateException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public UpdateException(String errorCode, String message) {
        super();
        this.errorCode = errorCode;
        this.message = message;
    }

    public UpdateException(String errorCode, String message, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.message = message;
    }
}
