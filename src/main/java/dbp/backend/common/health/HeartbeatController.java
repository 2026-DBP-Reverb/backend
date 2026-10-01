package dbp.backend.common.health;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HeartbeatController {
    @GetMapping("/api/heartbeat")
    public ApiResponse<Map<String, String>> heartbeat() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "Backend"
        ));
    }

    @GetMapping("/api/heartbeat/db")
    public ApiResponse<Map<String, String>> databaseHeartbeat() {
        try (Connection conn = ConnectionManager.getConnection()) {
            Integer result = JDBCUtil.executeQuery(
                    conn,
                    "SELECT 1 FROM DUAL",
                    rs -> {
                        if (!rs.next()) {
                            return 0;
                        }
                        return rs.getInt(1);
                    }
            );

            if (result == null || result != 1) {
                throw new DatabaseException();
            }

            return ApiResponse.success(Map.of(
                    "status", "UP",
                    "database", "Oracle"
            ));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
