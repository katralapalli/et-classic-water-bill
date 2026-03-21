package com.example.demo.controller;

import com.example.demo.dto.MeterReadingRequest;
import com.example.demo.dto.UpdateReadingRequest;
import com.example.demo.model.EtMeterReading;
import com.example.demo.service.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/readings")
public class MeterReadingController {

    @Autowired
    private MeterReadingService meterReadingService;

    // GET current month readings (all flats, missing ones show 0)
    @GetMapping("/current-month")
    public ResponseEntity<List<EtMeterReading>> getCurrentMonthReadings() {
        return ResponseEntity.ok(meterReadingService.getReadingsForCurrentMonth());
    }

    // GET all readings for a specific flat
    @GetMapping("/flat/{flatNumber}")
    public ResponseEntity<List<EtMeterReading>> getReadingsByFlat(@PathVariable String flatNumber) {
        return ResponseEntity.ok(meterReadingService.getReadingsByFlat(flatNumber));
    }

    // POST create new reading for current month
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReading(@RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(meterReadingService.createReading(request));
    }

    // PUT update existing reading
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateReading(@RequestBody UpdateReadingRequest request) {
        return ResponseEntity.ok(meterReadingService.updateReading(request));
    }

    // DELETE reading by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReading(@PathVariable Long id) {
        meterReadingService.deleteReading(id);
        return ResponseEntity.ok("Reading deleted successfully");
    }
}
