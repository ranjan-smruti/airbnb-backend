package com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler;

import com.codingshuttle.projects.airBnbApp.ExceptionHandler.ApiError;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class APIResponse<T> {
    //private LocalDateTime timestamp;
    private Long timestamp;
    private T data;
    private ApiError error;

    public APIResponse(){
        Instant instant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        this.timestamp = instant.toEpochMilli();
    }

    public APIResponse(T data){
        this();
        this.data = data;
    }

    public APIResponse(ApiError error){
        this();
        this.error = error;
    }
}
