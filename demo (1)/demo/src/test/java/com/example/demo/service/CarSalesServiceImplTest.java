package com.example.demo.service;

import com.example.demo.repository.CarSalesRepo;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CarSalesServiceImplTest {

    @Test
    void uploadCsvPersistsEachImportedRowOnce() {
        CarSalesRepo repository = mock(CarSalesRepo.class);
        when(repository.existsByCarNumber(anyString())).thenReturn(false);
        when(repository.saveAll(anyList())).thenAnswer(invocation -> {
            List<?> sales = invocation.getArgument(0);
            assertEquals(2, sales.size());
            assertNotSame(sales.get(0), sales.get(1));
            return sales;
        });
        CarSalesServiceImpl service = new CarSalesServiceImpl(repository);
        String csv = "car number,brand,model,color,year,time of purchase,date of purchase,price,mileage,engine(cc),fuel type,payment mode,state,city,customer name,contact number,email,warranty period\n"
                + "KA01AA0001,Toyota,Camry,White,2024,10:30:00,01-01-2024,2500000,15.5,2500,Petrol,Cash,Karnataka,Bengaluru,Customer One,9876543210,one@example.com,3 years\n"
                + "KA01AA0002,Honda,Civic,Blue,2023,11:30:00,02-02-2024,1800000,16.0,1800,Petrol,Card,Karnataka,Mysuru,Customer Two,9876543211,two@example.com,2 years\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sales.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        var result = service.uploadCsv(file);

        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(repository).saveAll(anyList());
    }
}