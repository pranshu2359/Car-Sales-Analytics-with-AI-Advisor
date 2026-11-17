package com.example.demo.repository;

import com.example.demo.entity.carsales;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarSalesRepo extends JpaRepository<carsales, Integer> {

    boolean existsByCarNumber(String carNumber);
    List<carsales> findByYear(int year);

        @Query("select function('year', sale.dateOfPurchase), count(sale) from carsales sale "
            + "where sale.dateOfPurchase is not null "
            + "group by function('year', sale.dateOfPurchase) order by function('year', sale.dateOfPurchase)")
        List<Object[]> countSalesByPurchaseYear();

        @Query("select function('month', sale.dateOfPurchase), count(sale) from carsales sale "
            + "where function('year', sale.dateOfPurchase) = :year "
            + "group by function('month', sale.dateOfPurchase) order by function('month', sale.dateOfPurchase)")
        List<Object[]> countSalesByPurchaseMonth(@Param("year") int year);

}
