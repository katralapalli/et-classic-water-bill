package com.et.etclassic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "et_meter_reading")
public class EtMeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_number", nullable = false)
    private String flatNumber;

    @Column(name = "reading_date")
    private LocalDateTime readingDate;

    @Column(name = "reading")
    private Long reading;

    @Column(name = "consumption")
    private Long consumption;

    @Column(name = "charges")
    private Double charges;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public LocalDateTime getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDateTime readingDate) { this.readingDate = readingDate; }

    public Long getReading() { return reading; }
    public void setReading(Long reading) { this.reading = reading; }

    public Long getConsumption() { return consumption; }
    public void setConsumption(Long consumption) { this.consumption = consumption; }

    public Double getCharges() { return charges; }
    public void setCharges(Double charges) { this.charges = charges; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
