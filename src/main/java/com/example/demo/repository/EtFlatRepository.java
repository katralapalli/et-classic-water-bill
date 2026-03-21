package com.example.demo.repository;

import com.example.demo.model.EtFlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtFlatRepository extends JpaRepository<EtFlat, Long> {

    Optional<EtFlat> findByFlatNumber(String flatNumber);

    List<EtFlat> findAllByOrderByFlatNumber();

    boolean existsByFlatNumber(String flatNumber);
}
