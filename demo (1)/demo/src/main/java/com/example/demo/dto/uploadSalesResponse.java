package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class uploadSalesResponse {
    private int totalRecords;
    private int successCount;
    private int failureCount;


}
