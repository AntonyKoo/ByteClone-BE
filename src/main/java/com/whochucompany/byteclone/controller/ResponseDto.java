package com.whochucompany.byteclone.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {

    private boolean result;
    private T data;
    private String message;
    private Error error;

    public ResponseDto(boolean result, T data){
        this.result = result;
        this.data = data;
    }

    public ResponseDto(boolean result, T data, Error error){
        this.result = result;
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseDto<T> success(T data, String message) {
        return new ResponseDto<>(true, data,message, null);
    }
    // 결과 성공여부만
    public static <T> ResponseDto<T> success(String message) {
        return new ResponseDto<>(true, null, message, null);
    }
    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, data);
    }

    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(false, null, new Error(code, message));
    }

    @Getter
    @AllArgsConstructor
    static class Error {
        private String code;
        private String message;
    }
}

