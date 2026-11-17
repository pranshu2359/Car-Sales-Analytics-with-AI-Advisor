package com.example.demo.controller;

import com.example.demo.commons.response.ApiResponse;
import com.example.demo.dto.uploadSalesResponse;
import com.example.demo.service.CarSalesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/car-sales")
public class CarSalesController {

    private final CarSalesService salesService;

    public CarSalesController(CarSalesService salesService) {

        this.salesService = salesService;
    }

    @PostMapping("/upload-csv")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            uploadSalesResponse response = new uploadSalesResponse(0, 0, 0);
            ApiResponse<uploadSalesResponse> apiResponse = new ApiResponse<>(
                    false,
                    "file is empty",
                    null,
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        uploadSalesResponse response = salesService.uploadCsv(file);
        ApiResponse<uploadSalesResponse> apiResponse = getApiResponse(response);
        return ResponseEntity.ok(apiResponse);
    }

    private ApiResponse<uploadSalesResponse> getApiResponse(uploadSalesResponse response) {
        String message;
        boolean success;

        if (response.getFailureCount() == 0) {
            message = "success";
            success = true;
        } else if (response.getSuccessCount() == 0) {
            message = "failed to upload";
            success = false;
        } else {
            message = "upload with some error " + response.getFailureCount() + " rows failed";
            success = false;
        }
        return new ApiResponse<>(success, message, response, HttpStatus.OK.value());
    }

    @GetMapping("/yearly-count")
    public ResponseEntity<ApiResponse<List<CarSalesService.YearlyCount>>> yearlyCount() {
        List<CarSalesService.YearlyCount> carsCount = salesService.getYearlyCarsCount();
        ApiResponse<List<CarSalesService.YearlyCount>> response = new ApiResponse<>(
                true,
                "data read successfully",
                carsCount,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-count")
    public ResponseEntity<ApiResponse<List<CarSalesService.MonthlyCount>>> monthlyCount(@RequestParam int year) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "monthly data read successfully",
                        salesService.getMonthlyCountByYear(year),
                        HttpStatus.OK.value()
                )
        );
    }

}
