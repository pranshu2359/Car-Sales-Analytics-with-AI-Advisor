package com.example.demo.service;

import com.example.demo.dto.uploadSalesResponse;
import com.example.demo.entity.carsales;

import com.example.demo.repository.CarSalesRepo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public final class CarSalesServiceImpl implements CarSalesService {

    @Override
    public List<YearlyCount> getYearlyCarsCount() {
        return repository.countSalesByPurchaseYear().stream()
                .map(row -> new YearlyCount(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public List<MonthlyCount> getMonthlyCountByYear(int year) {
        return repository.countSalesByPurchaseMonth(year).stream()
                .map(row -> new MonthlyCount(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
                .toList();
    }

    private final CarSalesRepo repository;

    public CarSalesServiceImpl(CarSalesRepo repository) {
        this.repository = repository;
    }

    @Override
    public uploadSalesResponse uploadCsv(MultipartFile file) {

        List<carsales> cars = new ArrayList<>();

        int failCount = 0;
        int totalRecords = 0;

        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            CSVParser parse = new CSVParser(reader, csvFormat);

            for (CSVRecord record : parse) {
                totalRecords++;

                try {
                    String carNumber = record.get("car number");
                    boolean exists = repository.existsByCarNumber(carNumber);

                    if (exists) {
                        failCount++;
                        System.out.println("duplicate data found " + carNumber);
                        continue;
                    }

                        carsales carSale = new carsales();
                        carSale.setCarNumber(carNumber);
                        carSale.setBrand(record.get("brand"));
                        carSale.setModel(record.get("model"));
                        carSale.setColor(record.get("color"));
                        carSale.setYear(Integer.parseInt(record.get("year")));
                        carSale.setTimeOfPurchase(LocalTime.parse(record.get("time of purchase")));
                        carSale.setDateOfPurchase(LocalDate.parse(record.get("date of purchase"),
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                        carSale.setPrice(Long.parseLong(record.get("price")));
                        carSale.setMileage(Double.parseDouble(record.get("mileage")));
                        carSale.setEngine(Integer.parseInt(record.get("engine(cc)")));
                        carSale.setFuelType(record.get("fuel type"));
                        carSale.setPaymentMode(record.get("payment mode"));
                        carSale.setState(record.get("state"));
                        carSale.setCity(record.get("city"));
                        carSale.setCustomerName(record.get("customer name"));
                        carSale.setContactNumber(record.get("contact number"));
                        carSale.setEmail(record.get("email"));
                        carSale.setWarrantyPeriod(record.get("warranty period"));
                        cars.add(carSale);

                } catch (Exception e) {
                    failCount++;
                    System.out.println("fail to process row:" + record.getRecordNumber());
                }
            }

            if (!cars.isEmpty()) {
                repository.saveAll(cars);
            }

        } catch (Exception exception) {
            throw new RuntimeException("Unable to parse CSV " + exception.getMessage());
        }

        int successCount = totalRecords - failCount;
        return new uploadSalesResponse(totalRecords, successCount, failCount);
    }
}
