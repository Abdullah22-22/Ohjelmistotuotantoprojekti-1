package fi.metropolia.tempconverter.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempCalculatorTest {

    private TempCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TempCalculator();
    }

    @Test
    @DisplayName("Fahrenheit to Celsius")
    void fahrenheitToCelsius() {
        assertEquals(0, calculator.fahrenheitToCelsius(32), 0.01);
        assertEquals(100, calculator.fahrenheitToCelsius(212), 0.01);
        assertEquals(-40, calculator.fahrenheitToCelsius(-40), 0.01);
        assertEquals(37, calculator.fahrenheitToCelsius(98.6), 0.01);
    }

    @Test
    @DisplayName("Celsius to Fahrenheit")
    void celsiusToFahrenheit() {
        assertEquals(32, calculator.celsiusToFahrenheit(0), 0.01);
        assertEquals(212, calculator.celsiusToFahrenheit(100), 0.01);
        assertEquals(-40, calculator.celsiusToFahrenheit(-40), 0.01);
    }

    @Test
    @DisplayName("Kelvin to Celsius")
    void kelvinToCelsius() {
        assertEquals(26.85, calculator.kelvinToCelsius(300), 0.01);
        assertEquals(0, calculator.kelvinToCelsius(273.15), 0.01);
        assertEquals(-273.15, calculator.kelvinToCelsius(0), 0.01);
    }

    @Test
    @DisplayName("Celsius to Kelvin")
    void celsiusToKelvin() {
        assertEquals(273.15, calculator.celsiusToKelvin(0), 0.01);
        assertEquals(373.15, calculator.celsiusToKelvin(100), 0.01);
    }

    @Test
    @DisplayName("Round trip conversion stays the same")
    void roundTripConversion() {
        double original = 25.0;
        double back = calculator.fahrenheitToCelsius(calculator.celsiusToFahrenheit(original));
        assertEquals(original, back, 0.0001);
    }

    @Test
    @DisplayName("Extreme temperature detection")
    void isExtremeTemperature() {
        assertTrue(calculator.isExtremeTemperature(-41));
        assertTrue(calculator.isExtremeTemperature(51));
        assertTrue(calculator.isExtremeTemperature(1000));
        assertFalse(calculator.isExtremeTemperature(-40));
        assertFalse(calculator.isExtremeTemperature(50));
        assertFalse(calculator.isExtremeTemperature(25));
        assertFalse(calculator.isExtremeTemperature(0));
    }
}