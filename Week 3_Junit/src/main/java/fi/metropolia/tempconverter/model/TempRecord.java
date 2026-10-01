package fi.metropolia.tempconverter.model;

import java.time.LocalDateTime;

public class TempRecord {

    private int recordId;
    private double inputValue;
    private double convertedValue;
    private int unitId;
    private LocalDateTime createdAt;

    public TempRecord() {
    }

    public TempRecord(double inputValue, double convertedValue, int unitId) {
        this.inputValue = inputValue;
        this.convertedValue = convertedValue;
        this.unitId = unitId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public double getInputValue() {
        return inputValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    public double getConvertedValue() {
        return convertedValue;
    }

    public void setConvertedValue(double convertedValue) {
        this.convertedValue = convertedValue;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}