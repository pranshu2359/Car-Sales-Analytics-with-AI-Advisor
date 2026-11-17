package com.example.demo.service;

import com.example.demo.dto.uploadSalesResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CarSalesService {


       uploadSalesResponse uploadCsv(MultipartFile file);

    List<YearlyCount> getYearlyCarsCount();

    List<MonthlyCount> getMonthlyCountByYear(int year);

    record YearlyCount(int year, long count) { }

    record MonthlyCount(int month, long count) { }
}