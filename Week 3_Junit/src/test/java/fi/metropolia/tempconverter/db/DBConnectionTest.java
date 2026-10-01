package fi.metropolia.tempconverter.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DBConnectionTest {

    @BeforeEach
    void setUp() {
        DBConnection.setCredentials(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
    }

    @AfterEach
    void tearDown() throws SQLException {
        DBConnection.closeConnection();
    }

    @Test
    void getConnectionReturnsOpenConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());
    }

    @Test
    void getConnectionReusesSameConnection() throws SQLException {
        Connection first = DBConnection.getConnection();
        Connection second = DBConnection.getConnection();
        assertSame(first, second);
    }

    @Test
    void testConnectionReturnsTrueWhenOpen() {
        assertTrue(DBConnection.testConnection());
    }

    @Test
    void closeConnectionClosesIt() throws SQLException {
        Connection conn = DBConnection.getConnection();
        DBConnection.closeConnection();
        assertTrue(conn.isClosed());
    }

    @Test
    void connectionIsRecreatedAfterClose() throws SQLException {
        Connection first = DBConnection.getConnection();
        DBConnection.closeConnection();
        Connection second = DBConnection.getConnection();
        assertNotNull(second);
        assertFalse(second.isClosed());
    }

    @Test
    void setCredentialsResetsConnection() throws SQLException {
        Connection first = DBConnection.getConnection();
        DBConnection.setCredentials(
                "jdbc:h2:mem:otherdb;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
        Connection second = DBConnection.getConnection();
        assertNotNull(second);
        assertFalse(second.isClosed());
    }
}