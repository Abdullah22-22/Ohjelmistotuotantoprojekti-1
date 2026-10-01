package fi.metropolia.tempconverter.model;

public class TemperatureUnit {

    private int unitId;
    private String unitName;
    private String symbol;

    public TemperatureUnit() {
    }

    public TemperatureUnit(int unitId, String unitName, String symbol) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.symbol = symbol;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return unitName + " (" + symbol + ")";
    }
}