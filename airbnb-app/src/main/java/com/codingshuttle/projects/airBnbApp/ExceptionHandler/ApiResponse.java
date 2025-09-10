package com.codingshuttle.projects.airBnbApp.ExceptionHandler;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ApiResponse {
    private HttpStatus status;
    private String msg;
    private List<String> subErrors;
}
