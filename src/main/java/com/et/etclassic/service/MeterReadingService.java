package com.et.etclassic.service;

import com.et.etclassic.dto.MeterReadingRequest;
import com.et.etclassic.dto.UpdateReadingRequest;
import com.et.etclassic.model.EtFlat;
import com.et.etclassic.model.EtMeterReading;
import com.et.etclassic.repository.EtFlatRepository;
import com.et.etclassic.repository.EtMeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeterReadingService {

    @Autowired
    private EtMeterReadingRepository readingRepository;

    @Autowired
    private EtFlatRepository flatRepository;

    // Load current month readings + missing flats with 0s
    public List<EtMeterReading> getReadingsForCurrentMonth() {
        List<EtMeterReading> current  = readingRepository.findCurrentMonthReadings();
        List<EtFlat>         allFlats = flatRepository.findAllByOrderByFlatNumber();

        Set<String> savedFlats = current.stream()
                .map(EtMeterReading::getFlatNumber)
                .collect(Collectors.toSet());

        List<EtMeterReading> missingFlats = allFlats.stream()
                .filter(flat -> !savedFlats.contains(flat.getFlatNumber()))
                .map(flat -> {
                    EtMeterReading dummy = new EtMeterReading();
                    dummy.setId(null);
                    dummy.setFlatNumber(flat.getFlatNumber());
                    dummy.setReading(0L);
                    dummy.setConsumption(0L);
                    dummy.setCharges(0.0);
                    dummy.setReadingDate(LocalDateTime.now());
                    return dummy;
                })
                .collect(Collectors.toList());

        List<EtMeterReading> result = new ArrayList<>();
        result.addAll(current);
        result.addAll(missingFlats);
        result.sort(Comparator.comparing(EtMeterReading::getFlatNumber));

        return result;
    }

    // Create new reading for current month
    public Map<String, Object> createReading(MeterReadingRequest request) {
        double previousReading = readingRepository
                .findPreviousMonthReadingByFlatNumber(request.getFlatNumber())
                .map(r -> r.getReading().doubleValue())
                .orElse(0.0);

        double consumption = request.getReading() - previousReading;
        double charges     = calculateCharges(consumption);

        EtMeterReading newRecord = new EtMeterReading();
        newRecord.setFlatNumber(request.getFlatNumber());
        newRecord.setReading((long) request.getReading());
        newRecord.setConsumption((long) consumption);
        newRecord.setCharges(charges);
        newRecord.setReadingDate(LocalDateTime.now());

        EtMeterReading saved = readingRepository.save(newRecord);

        Map<String, Object> response = new HashMap<>();
        response.put("id",          saved.getId());
        response.put("consumption", consumption);
        response.put("charges",     charges);
        return response;
    }

    // Update existing reading
    public Map<String, Object> updateReading(UpdateReadingRequest request) {
        EtMeterReading record = readingRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Reading not found: " + request.getId()));

        double previousReading = readingRepository
                .findPreviousMonthReadingByFlatNumber(record.getFlatNumber())
                .map(r -> r.getReading().doubleValue())
                .orElse(0.0);

        double consumption = request.getReading() - previousReading;
        double charges     = calculateCharges(consumption);

        record.setReading((long) request.getReading());
        record.setConsumption((long) consumption);
        record.setCharges(charges);
        record.setUpdatedAt(LocalDateTime.now());

        readingRepository.save(record);

        Map<String, Object> response = new HashMap<>();
        response.put("consumption", consumption);
        response.put("charges",     charges);
        return response;
    }

    // Get all readings for a specific flat
    public List<EtMeterReading> getReadingsByFlat(String flatNumber) {
        return readingRepository.findAllByFlatNumberOrderByReadingDateDesc(flatNumber);
    }

    // Delete a reading
    public void deleteReading(Long id) {
        readingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading not found: " + id));
        readingRepository.deleteById(id);
    }

    // Slab-based charges calculation
    public double calculateCharges(double consumption) {
        double charges;
        if (consumption <= 0) {
            charges = 0.0;
        } else if (consumption <= 20000) {
            charges = consumption * 0.015;
        } else if (consumption <= 30000) {
            charges = (20000 * 0.015) + (consumption - 20000) * 0.025;
        } else if (consumption <= 40000) {
            charges = (20000 * 0.015) + (10000 * 0.025) + (consumption - 30000) * 0.035;
        } else {
            charges = (20000 * 0.015) + (10000 * 0.025) + (10000 * 0.035) + (consumption - 40000) * 0.045;
        }
        return Math.round(charges * 100.0) / 100.0;
    }
}
