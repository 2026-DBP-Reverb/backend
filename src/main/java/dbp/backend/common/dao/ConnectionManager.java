package dbp.backend.common.dao;

import dbp.backend.common.config.DatabaseConfig;
import dbp.backend.common.exception.DatabaseException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public final class ConnectionManager {
    private static final BasicDataSource DATA_SOURCE = createDataSource();

    private ConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static int getActiveConnectionCount() {
        return DATA_SOURCE.getNumActive();
    }

    public static void close() {
        try {
            DATA_SOURCE.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static BasicDataSource createDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(DatabaseConfig.DB_DRIVER);
        dataSource.setUrl(DatabaseConfig.DB_URL);
        dataSource.setUsername(DatabaseConfig.DB_USERNAME);
        dataSource.setPassword(DatabaseConfig.DB_PASSWORD);
        dataSource.setInitialSize(3);
        dataSource.setMinIdle(3);
        dataSource.setMaxIdle(10);
        dataSource.setMaxTotal(20);
        dataSource.setMaxWait(Duration.ofSeconds(3));
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        return dataSource;
    }
}
