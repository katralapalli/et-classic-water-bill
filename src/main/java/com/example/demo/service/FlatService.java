package com.example.demo.service;

import com.example.demo.dto.FlatRequest;
import com.example.demo.dto.UpdateTenantRequest;
import com.example.demo.model.EtFlat;
import com.example.demo.repository.EtFlatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlatService {

    @Autowired
    private EtFlatRepository repository;

    public List<EtFlat> getAllFlats() {
        return repository.findAllByOrderByFlatNumber();
    }

    public EtFlat getFlatByNumber(String flatNumber) {
        return repository.findByFlatNumber(flatNumber)
                .orElseThrow(() -> new RuntimeException("Flat not found: " + flatNumber));
    }

    public EtFlat createFlat(FlatRequest request) {
        if (repository.existsByFlatNumber(request.getFlatNumber())) {
            throw new RuntimeException("Flat already exists: " + request.getFlatNumber());
        }
        EtFlat flat = new EtFlat();
        flat.setFlatNumber(request.getFlatNumber());
        flat.setOwnerName(request.getOwnerName());       // permanent — set only on create
        flat.setOwnerPhone(request.getOwnerPhone());     // permanent — set only on create
        flat.setTenantName(request.getTenantName());
        flat.setTenantPhone(request.getTenantPhone());
        return repository.save(flat);
    }

    // Only tenant info can be updated — owner info is permanent
    public EtFlat updateTenant(String flatNumber, UpdateTenantRequest request) {
        EtFlat flat = getFlatByNumber(flatNumber);
        flat.setTenantName(request.getTenantName());
        flat.setTenantPhone(request.getTenantPhone());
        // flatNumber, ownerName, ownerPhone — never touched
        return repository.save(flat);
    }

    public void deleteFlat(String flatNumber) {
        EtFlat flat = getFlatByNumber(flatNumber);
        repository.delete(flat);
    }
}
