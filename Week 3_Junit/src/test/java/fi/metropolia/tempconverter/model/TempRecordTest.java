package fi.metropolia.tempconverter.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TempRecordTest {

    @Test
    void emptyConstructorCreatesObject() {
        TempRecord record = new TempRecord();
        assertNotNull(record);
        assertEquals(0, record.getRecordId());
        assertEquals(0.0, record.getInputValue(), 0.001);
        assertNull(record.getCreatedAt());
    }

    @Test
    void parameterizedConstructorSetsValues() {
        TempRecord record = new TempRecord(212.0, 100.0, 1);
        assertEquals(212.0, record.getInputValue(), 0.001);
        assertEquals(100.0, record.getConvertedValue(), 0.001);
        assertEquals(1, record.getUnitId());
    }

    @Test
    void settersAndGettersWork() {
        TempRecord record = new TempRecord();
        LocalDateTime now = LocalDateTime.now();

        record.setRecordId(5);
        record.setInputValue(32.0);
        record.setConvertedValue(0.0);
        record.setUnitId(2);
        record.setCreatedAt(now);

        assertEquals(5, record.getRecordId());
        assertEquals(32.0, record.getInputValue(), 0.001);
        assertEquals(0.0, record.getConvertedValue(), 0.001);
        assertEquals(2, record.getUnitId());
        assertEquals(now, record.getCreatedAt());
    }

    @Test
    void negativeValuesAreAllowed() {
        TempRecord record = new TempRecord(-40.0, -40.0, 1);
        assertEquals(-40.0, record.getInputValue(), 0.001);
        assertEquals(-40.0, record.getConvertedValue(), 0.001);
    }
}