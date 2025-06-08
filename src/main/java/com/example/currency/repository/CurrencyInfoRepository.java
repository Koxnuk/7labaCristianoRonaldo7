package com.example.currency.repository;

import com.example.currency.models.CurrencyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyInfoRepository extends JpaRepository<CurrencyInfo, Integer> {
    Optional<CurrencyInfo> findByCurAbbreviation(String abbreviation);

    @Query("SELECT c FROM CurrencyInfo c LEFT JOIN FETCH c.rates WHERE c.curId = :id")
    Optional<CurrencyInfo> findByIdWithRates(@Param("id") Integer id);
}