package com.astoev.cave.survey.exception;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 4/16/12
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataException() {
        super();
    }

    public DataException(String detailMessage) {
        super(detailMessage);
    }

    public DataException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
