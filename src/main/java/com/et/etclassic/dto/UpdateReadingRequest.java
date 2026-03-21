package com.et.etclassic.dto;

public class UpdateReadingRequest {
    private Long id;
    private double reading;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getReading() { return reading; }
    public void setReading(double reading) { this.reading = reading; }
}
