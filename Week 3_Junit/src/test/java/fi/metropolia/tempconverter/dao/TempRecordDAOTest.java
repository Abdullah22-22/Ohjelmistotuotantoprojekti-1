package fi.metropolia.tempconverter.dao;

import fi.metropolia.tempconverter.db.DBConnection;
import fi.metropolia.tempconverter.model.TempRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempRecordDAOTest {

    private TempRecordDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        DBConnection.setCredentials(
                "jdbc:h2:mem:recorddao;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");

        try (Statement st = DBConnection.getConnection().createStatement()) {
            st.execute("DROP TABLE IF EXISTS temp_records");
            st.execute("DROP TABLE IF EXISTS temperature_units");
            st.execute("CREATE TABLE temperature_units ("
                    + "unit_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "unit_name VARCHAR(20) NOT NULL,"
                    + "symbol VARCHAR(5) NOT NULL)");
            st.execute("CREATE TABLE temp_records ("
                    + "record_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "input_value DOUBLE NOT NULL,"
                    + "converted_value DOUBLE NOT NULL,"
                    + "unit_id INT,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (unit_id) REFERENCES temperature_units(unit_id))");
            st.execute("INSERT INTO temperature_units (unit_name, symbol) VALUES "
                    + "('Celsius','C'), ('Fahrenheit','F'), ('Kelvin','K')");
        }

        dao = new TempRecordDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DBConnection.closeConnection();
    }

    @Test
    void tableIsEmptyAtStart() throws SQLException {
        assertEquals(0, dao.count());
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void insertReturnsGeneratedId() throws SQLException {
        int id = dao.insert(new TempRecord(212.0, 100.0, 1));
        assertTrue(id > 0);
        assertEquals(1, dao.count());
    }

    @Test
    void insertStoresCorrectValues() throws SQLException {
        int id = dao.insert(new TempRecord(32.0, 0.0, 1));
        TempRecord saved = dao.findById(id);

        assertNotNull(saved);
        assertEquals(32.0, saved.getInputValue(), 0.001);
        assertEquals(0.0, saved.getConvertedValue(), 0.001);
        assertEquals(1, saved.getUnitId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findAllReturnsAllRecords() throws SQLException {
        dao.insert(new TempRecord(212.0, 100.0, 1));
        dao.insert(new TempRecord(32.0, 0.0, 1));
        dao.insert(new TempRecord(0.0, 273.15, 3));

        List<TempRecord> records = dao.findAll();
        assertEquals(3, records.size());
    }

    @Test
    void findByIdReturnsNullForUnknownId() throws SQLException {
        assertNull(dao.findById(999));
    }

    @Test
    void deleteRemovesRecord() throws SQLException {
        int id = dao.insert(new TempRecord(100.0, 212.0, 2));
        assertTrue(dao.delete(id));
        assertNull(dao.findById(id));
        assertEquals(0, dao.count());
    }

    @Test
    void deleteReturnsFalseForUnknownId() throws SQLException {
        assertFalse(dao.delete(12345));
    }

    @Test
    void deleteAllClearsTable() throws SQLException {
        dao.insert(new TempRecord(1.0, 2.0, 1));
        dao.insert(new TempRecord(3.0, 4.0, 1));

        int deleted = dao.deleteAll();
        assertEquals(2, deleted);
        assertEquals(0, dao.count());
    }

    @Test
    void negativeTemperaturesAreStored() throws SQLException {
        int id = dao.insert(new TempRecord(-40.0, -40.0, 1));
        TempRecord saved = dao.findById(id);
        assertNotNull(saved);
        assertEquals(-40.0, saved.getInputValue(), 0.001);
    }
}