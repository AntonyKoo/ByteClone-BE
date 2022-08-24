package com.whochucompany.byteclone.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

public class ByteCloneExceptionHandler {

    @ExceptionHandler(value = ByteCloneException.class) // value 로 어떤 exception 을 잡을 것인지.
    public ResponseEntity<Map<String, String>> ExceptionHandler (ByteCloneException e) {

        HttpHeaders httpHeaders = new HttpHeaders();

        Map<String, String> map = new HashMap<>();

        map.put("에러 타입", e.getHttpStatusType());
        map.put("에러 코드", Integer.toString(e.getHttpStatusCode())); // Map<String, String> getHttpStatusCode 는 int 이기에 형변환을 해주어야 한다.
        map.put("에러 메시지", e.getMessage());

        return new ResponseEntity<>(map, httpHeaders, e.getHttpStatus()); // body, headers, httpStatus
    }

}
