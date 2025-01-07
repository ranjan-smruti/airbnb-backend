package com.codingshuttle.projects.airBnbApp.ExceptionHandler;

import com.codingshuttle.projects.airBnbApp.GlobalAPIResponseHandler.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleEmployeeNotFound(ResourceNotFoundException ex){
        ApiResponse apiError = ApiResponse
                .builder()
                .status(HttpStatus.NOT_FOUND)
                .msg(ex.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> internalServerError(Exception exception)
    {
        ApiResponse apiError = ApiResponse
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .msg(exception.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> handleInputValidationError(MethodArgumentNotValidException exception)
    {
        List<String> errors = exception
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(error->error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiResponse apiError = ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .msg("Input validation failed")
                .subErrors(errors)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    private ResponseEntity<APIResponse<?>> buildErrorResponseEntity(ApiResponse apiError) {
        return new ResponseEntity<>(new APIResponse<>(apiError),apiError.getStatus());
    }
}
