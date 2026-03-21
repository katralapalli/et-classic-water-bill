package com.et.etclassic.controller;

import com.et.etclassic.dto.FlatRequest;
import com.et.etclassic.dto.UpdateTenantRequest;
import com.et.etclassic.model.EtFlat;
import com.et.etclassic.service.FlatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flats")
public class FlatController {

    @Autowired
    private FlatService flatService;

    // GET all flats
    @GetMapping
    public ResponseEntity<List<EtFlat>> getAllFlats() {
        return ResponseEntity.ok(flatService.getAllFlats());
    }

    // GET single flat by flat number
    @GetMapping("/{flatNumber}")
    public ResponseEntity<EtFlat> getFlatByNumber(@PathVariable String flatNumber) {
        return ResponseEntity.ok(flatService.getFlatByNumber(flatNumber));
    }

    // POST create new flat — owner info set here permanently
    @PostMapping
    public ResponseEntity<EtFlat> createFlat(@RequestBody FlatRequest request) {
        return ResponseEntity.ok(flatService.createFlat(request));
    }

    // PUT update tenant only — owner info stays untouched
    @PutMapping("/{flatNumber}/tenant")
    public ResponseEntity<EtFlat> updateTenant(@PathVariable String flatNumber,
                                                @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(flatService.updateTenant(flatNumber, request));
    }

    // DELETE flat
    @DeleteMapping("/{flatNumber}")
    public ResponseEntity<String> deleteFlat(@PathVariable String flatNumber) {
        flatService.deleteFlat(flatNumber);
        return ResponseEntity.ok("Flat " + flatNumber + " deleted successfully");
    }
}
