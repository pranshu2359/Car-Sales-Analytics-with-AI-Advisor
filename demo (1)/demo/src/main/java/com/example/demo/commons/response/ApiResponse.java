package com.example.demo.commons.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiResponse<T>{
    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success,String message,T data,int statusCode) {
        this.message=message;
        this.data=data;
        this.statusCode=statusCode;
        this.timestamp=LocalDateTime.now();
        this.success=success;
    }
}
