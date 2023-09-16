package com.dnlkk.controller.http;

public interface HttpStatusCode {
    int code();

    boolean isInformational();
    boolean isSuccessful();
    boolean isRedirection();
    boolean isClientError();
    boolean isServerError();
	boolean isError();

    default boolean equalsCode(HttpStatusCode httpStatus) {
        return code() == httpStatus.code();
    }
}