package com.et.etclassic.controller;

import com.et.etclassic.dto.MeterReadingRequest;
import com.et.etclassic.dto.UpdateReadingRequest;
import com.et.etclassic.model.EtMeterReading;
import com.et.etclassic.service.MeterReadingService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

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

    @PostMapping("/generate-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateFiles(@RequestBody List<Map<String, Object>> billData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Current month string e.g. "2026-03"
            String monthStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // File paths — saved in working directory under /bills/
            String billPath = "bills/water-bill-" + monthStr + ".txt";
            String csvPath  = "bills/water-bill-" + monthStr + ".csv";

            // Create bills directory if not exists
            Files.createDirectories(Paths.get("bills"));

            // ── Generate Bill Text ──────────────────────────────
            StringBuilder billContent = new StringBuilder();
            for (Map<String, Object> item : billData) {
                billContent.append("Name & Flat # : ").append(item.get("name")).append(" ").append(item.get("flatNumber")).append("\n");
                billContent.append("Reading(Curr,Prev,Difference): ")
                        .append(item.get("currReading")).append(", ")
                        .append(item.get("prevReading")).append(", ")
                        .append(item.get("consumption")).append(" Liters\n");
                Object charges = item.get("charges");
                String totalCharges = getTotalCharges((String) charges);
                billContent.append("Amount Payable: Rs.").append(totalCharges)
                        .append("/- (").append(item.get("base")).append(" + ").append("1500").append(")\n");
                billContent.append("Note: Please pay your amount before ").append(item.get("dueDate")).append(" to avoid penalty.\n");
                billContent.append("\n").append("─".repeat(60)).append("\n\n");
            }
            // Override if exists
            Files.writeString(Paths.get(billPath), billContent.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // ── Generate CSV ────────────────────────────────────
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("flat_number,name,phone,pres_reading,prev_reading,consumption,charges\n");
            for (Map<String, Object> item : billData) {
                Object charges = item.get("charges");
                String totalCharges = getTotalCharges((String) charges);
                csvContent.append(item.get("flatNumber")).append(",")
                        .append("\"").append(item.get("name")).append("\",")
                        .append(item.get("phone")).append(",")
                        .append(item.get("currReading")).append(",")
                        .append(item.get("prevReading")).append(",")
                        .append(item.get("consumption")).append(",")
                        .append(totalCharges).append("\n");
            }
            Files.writeString(Paths.get(csvPath), csvContent.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            response.put("status",   "success");
            response.put("billPath", billPath);
            response.put("csvPath",  csvPath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private static @NonNull String getTotalCharges(String charges) {
        Double totCharges = Double.parseDouble(charges) + 1500;
        String formatted = String.format("%.2f", totCharges);
        return formatted;
    }
}
