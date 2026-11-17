package com.example.demo.commons.response;

import com.example.demo.dto.uploadSalesResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<uploadSalesResponse>> handleAllExceptions(Exception ex){
        ex.printStackTrace();

        uploadSalesResponse response =new uploadSalesResponse(0,0,0);
        ApiResponse<uploadSalesResponse> apiResponse=new ApiResponse<uploadSalesResponse>

                (false, ex.getMessage(),
                        response,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
        return new ResponseEntity<ApiResponse<uploadSalesResponse>>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);


    }


}
