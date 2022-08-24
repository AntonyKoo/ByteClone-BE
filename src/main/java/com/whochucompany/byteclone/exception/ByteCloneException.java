package com.whochucompany.byteclone.exception;

import org.springframework.http.HttpStatus;

public class ByteCloneException extends Exception{

    private HttpStatus httpStatus;
    private String message;

    public ByteCloneException (HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatusCode() {
        return httpStatus.value(); // 200~, 300~ ,400~ ,500~
    }

    public String getHttpStatusType() {
        return httpStatus.getReasonPhrase(); // error message 같은 정보 --> series 는 메서드 안만들었음.
    }

    public HttpStatus getHttpStatus() {
        return httpStatus; // httpStatus 객체 자체 return.
    }
}
