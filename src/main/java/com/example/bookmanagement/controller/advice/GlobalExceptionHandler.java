package com.example.bookmanagement.controller.advice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.bookmanagement.controller.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	/**
     * @Valid による入力バリデーションエラーを処理する
     * 400 Bad Request を返却
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "入力内容に不備があります。",
            fieldErrors
        );
    }
    
    /**
     * Service層での IllegalArgumentException (ISBN重複など) を処理する
     * 400 Bad Request を返却
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            null
        );
    }

    /**
     * ドメインモデルでの IllegalStateException (二重貸出など) を処理する
     * 409 Conflict を返却
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalStateException(IllegalStateException ex) {
        return new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            null
        );
    }
}
