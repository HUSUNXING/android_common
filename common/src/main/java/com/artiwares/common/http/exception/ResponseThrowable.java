package com.artiwares.common.http.exception;

public class ResponseThrowable extends Exception {
    public String resultCode;
    public String resultMsg;

    public ResponseThrowable(String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }
}