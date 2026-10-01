package fi.metropolia.tempconverter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TemperatureUnitTest {

    @Test
    void emptyConstructorCreatesObject() {
        TemperatureUnit unit = new TemperatureUnit();
        assertNotNull(unit);
        assertEquals(0, unit.getUnitId());
        assertNull(unit.getUnitName());
        assertNull(unit.getSymbol());
    }

    @Test
    void parameterizedConstructorSetsValues() {
        TemperatureUnit unit = new TemperatureUnit(1, "Celsius", "C");
        assertEquals(1, unit.getUnitId());
        assertEquals("Celsius", unit.getUnitName());
        assertEquals("C", unit.getSymbol());
    }

    @Test
    void settersAndGettersWork() {
        TemperatureUnit unit = new TemperatureUnit();
        unit.setUnitId(3);
        unit.setUnitName("Kelvin");
        unit.setSymbol("K");

        assertEquals(3, unit.getUnitId());
        assertEquals("Kelvin", unit.getUnitName());
        assertEquals("K", unit.getSymbol());
    }

    @Test
    void toStringContainsNameAndSymbol() {
        TemperatureUnit unit = new TemperatureUnit(2, "Fahrenheit", "F");
        String text = unit.toString();
        assertEquals("Fahrenheit (F)", text);
    }
}