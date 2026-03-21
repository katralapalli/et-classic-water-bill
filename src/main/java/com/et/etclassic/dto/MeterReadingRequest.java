package com.et.etclassic.dto;

public class MeterReadingRequest {
    private String flatNumber;
    private double reading;

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public double getReading() { return reading; }
    public void setReading(double reading) { this.reading = reading; }
}
