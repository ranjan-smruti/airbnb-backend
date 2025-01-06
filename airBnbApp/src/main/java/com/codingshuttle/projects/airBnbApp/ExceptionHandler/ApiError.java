package com.codingshuttle.projects.airBnbApp.ExceptionHandler;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ApiError {
    private HttpStatus status;
    private String errorMessage;
    private List<String> subErrors;
}
