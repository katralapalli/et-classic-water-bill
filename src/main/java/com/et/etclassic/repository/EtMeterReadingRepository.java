package com.et.etclassic.repository;

import com.et.etclassic.model.EtMeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtMeterReadingRepository extends JpaRepository<EtMeterReading, Long> {

    @Query(value = "SELECT * FROM et_meter_reading " +
            "WHERE EXTRACT(MONTH FROM reading_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR FROM reading_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "ORDER BY flat_number", nativeQuery = true)
    List<EtMeterReading> findCurrentMonthReadings();

    @Query(value = "SELECT * FROM et_meter_reading " +
            "WHERE EXTRACT(MONTH FROM reading_date) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month') " +
            "AND EXTRACT(YEAR FROM reading_date) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month') " +
            "ORDER BY flat_number", nativeQuery = true)
    List<EtMeterReading> findPreviousMonthReadings();

    @Query(value = "SELECT * FROM et_meter_reading " +
            "WHERE flat_number = :flatNumber " +
            "AND EXTRACT(MONTH FROM reading_date) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month') " +
            "AND EXTRACT(YEAR FROM reading_date) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month')",
            nativeQuery = true)
    Optional<EtMeterReading> findPreviousMonthReadingByFlatNumber(@Param("flatNumber") String flatNumber);

    @Query(value = "SELECT * FROM et_meter_reading " +
            "WHERE flat_number = :flatNumber " +
            "ORDER BY reading_date DESC", nativeQuery = true)
    List<EtMeterReading> findAllByFlatNumberOrderByReadingDateDesc(@Param("flatNumber") String flatNumber);

    @Query(value = "SELECT COUNT(*) FROM et_meter_reading " +
            "WHERE flat_number = :flatNumber " +
            "AND EXTRACT(MONTH FROM reading_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR FROM reading_date) = EXTRACT(YEAR FROM CURRENT_DATE)",
            nativeQuery = true)
    int existsCurrentMonthReadingForFlat(@Param("flatNumber") String flatNumber);
}
