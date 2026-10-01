package fi.metropolia.tempconverter.dao;

import fi.metropolia.tempconverter.db.DBConnection;
import fi.metropolia.tempconverter.model.TempRecord;
import fi.metropolia.tempconverter.model.TempRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TempRecordDAO {

    public int insert(TempRecord record) throws SQLException {
        String sql = "INSERT INTO temp_records (input_value, converted_value, unit_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, record.getInputValue());
            ps.setDouble(2, record.getConvertedValue());
            ps.setInt(3, record.getUnitId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setRecordId(keys.getInt(1));
                    return record.getRecordId();
                }
            }
        }
        return -1;
    }

    public List<TempRecord> findAll() throws SQLException {
        List<TempRecord> records = new ArrayList<>();
        String sql = "SELECT record_id, input_value, converted_value, unit_id, created_at "
                + "FROM temp_records ORDER BY record_id DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        }
        return records;
    }

    public TempRecord findById(int recordId) throws SQLException {
        String sql = "SELECT record_id, input_value, converted_value, unit_id, created_at "
                + "FROM temp_records WHERE record_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean delete(int recordId) throws SQLException {
        String sql = "DELETE FROM temp_records WHERE record_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    public int deleteAll() throws SQLException {
        try (Statement st = DBConnection.getConnection().createStatement()) {
            return st.executeUpdate("DELETE FROM temp_records");
        }
    }

    public int count() throws SQLException {
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM temp_records")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private TempRecord mapRow(ResultSet rs) throws SQLException {
        TempRecord r = new TempRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setInputValue(rs.getDouble("input_value"));
        r.setConvertedValue(rs.getDouble("converted_value"));
        r.setUnitId(rs.getInt("unit_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        return r;
    }
}