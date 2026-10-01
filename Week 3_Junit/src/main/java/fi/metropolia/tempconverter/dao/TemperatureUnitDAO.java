package fi.metropolia.tempconverter.dao;

import fi.metropolia.tempconverter.db.DBConnection;
import fi.metropolia.tempconverter.model.TemperatureUnit;
import fi.metropolia.tempconverter.model.TemperatureUnit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TemperatureUnitDAO {

    public List<TemperatureUnit> findAll() throws SQLException {
        List<TemperatureUnit> units = new ArrayList<>();
        String sql = "SELECT unit_id, unit_name, symbol FROM temperature_units ORDER BY unit_id";

        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                units.add(new TemperatureUnit(
                        rs.getInt("unit_id"),
                        rs.getString("unit_name"),
                        rs.getString("symbol")));
            }
        }
        return units;
    }

    public TemperatureUnit findById(int id) throws SQLException {
        String sql = "SELECT unit_id, unit_name, symbol FROM temperature_units WHERE unit_id = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TemperatureUnit(
                            rs.getInt("unit_id"),
                            rs.getString("unit_name"),
                            rs.getString("symbol"));
                }
            }
        }
        return null;
    }

    public TemperatureUnit findBySymbol(String symbol) throws SQLException {
        String sql = "SELECT unit_id, unit_name, symbol FROM temperature_units WHERE symbol = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, symbol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TemperatureUnit(
                            rs.getInt("unit_id"),
                            rs.getString("unit_name"),
                            rs.getString("symbol"));
                }
            }
        }
        return null;
    }

    public int insert(TemperatureUnit unit) throws SQLException {
        String sql = "INSERT INTO temperature_units (unit_name, symbol) VALUES (?, ?)";

        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, unit.getUnitName());
            ps.setString(2, unit.getSymbol());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    unit.setUnitId(keys.getInt(1));
                    return unit.getUnitId();
                }
            }
        }
        return -1;
    }

    public boolean delete(int unitId) throws SQLException {
        String sql = "DELETE FROM temperature_units WHERE unit_id = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            return ps.executeUpdate() > 0;
        }
    }
}