package fi.metropolia.tempconverter.dao;

import fi.metropolia.tempconverter.db.DBConnection;
import fi.metropolia.tempconverter.model.TemperatureUnit;

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

class TemperatureUnitDAOTest {

    private TemperatureUnitDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        DBConnection.setCredentials(
                "jdbc:h2:mem:unitdao;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");

        try (Statement st = DBConnection.getConnection().createStatement()) {
            st.execute("DROP TABLE IF EXISTS temp_records");
            st.execute("DROP TABLE IF EXISTS temperature_units");
            st.execute("CREATE TABLE temperature_units ("
                    + "unit_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "unit_name VARCHAR(20) NOT NULL,"
                    + "symbol VARCHAR(5) NOT NULL)");
            st.execute("INSERT INTO temperature_units (unit_name, symbol) VALUES "
                    + "('Celsius','C'), ('Fahrenheit','F'), ('Kelvin','K')");
        }

        dao = new TemperatureUnitDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DBConnection.closeConnection();
    }

    @Test
    void findAllReturnsThreeUnits() throws SQLException {
        List<TemperatureUnit> units = dao.findAll();
        assertEquals(3, units.size());
    }

    @Test
    void findByIdReturnsCorrectUnit() throws SQLException {
        TemperatureUnit unit = dao.findById(1);
        assertNotNull(unit);
        assertEquals("Celsius", unit.getUnitName());
        assertEquals("C", unit.getSymbol());
    }

    @Test
    void findByIdReturnsNullForUnknownId() throws SQLException {
        assertNull(dao.findById(999));
    }

    @Test
    void findBySymbolReturnsCorrectUnit() throws SQLException {
        TemperatureUnit unit = dao.findBySymbol("K");
        assertNotNull(unit);
        assertEquals("Kelvin", unit.getUnitName());
    }

    @Test
    void findBySymbolReturnsNullForUnknownSymbol() throws SQLException {
        assertNull(dao.findBySymbol("X"));
    }

    @Test
    void insertAddsNewUnit() throws SQLException {
        int id = dao.insert(new TemperatureUnit(0, "Rankine", "R"));
        assertTrue(id > 0);
        assertEquals(4, dao.findAll().size());

        TemperatureUnit saved = dao.findById(id);
        assertNotNull(saved);
        assertEquals("Rankine", saved.getUnitName());
    }

    @Test
    void deleteRemovesUnit() throws SQLException {
        int id = dao.insert(new TemperatureUnit(0, "Temp", "T"));
        assertTrue(dao.delete(id));
        assertNull(dao.findById(id));
    }

    @Test
    void deleteReturnsFalseForUnknownId() throws SQLException {
        assertFalse(dao.delete(999));
    }
}