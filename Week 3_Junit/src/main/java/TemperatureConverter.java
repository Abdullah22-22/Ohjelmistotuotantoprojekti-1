public class TemperatureConverter {

    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public boolean isExtremeTemperature(double celsius) {
        return celsius < -40 || celsius > 50;
    }

    public double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    public static void main(String[] args) {
        TemperatureConverter converter = new TemperatureConverter();

        System.out.println("=== Temperature Converter ===");
        System.out.println("32 F  = " + converter.fahrenheitToCelsius(32) + " C");
        System.out.println("100 C = " + converter.celsiusToFahrenheit(100) + " F");
        System.out.println("300 K = " + converter.kelvinToCelsius(300) + " C");
        System.out.println("Is 60 C extreme? " + converter.isExtremeTemperature(60));
        System.out.println("=============================");
    }
}