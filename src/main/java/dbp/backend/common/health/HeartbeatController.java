package dbp.backend.common.health;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.common.response.ResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
@Tag(name = "Health")
public class HeartbeatController {

    @GetMapping("/api/heartbeat")
    @Operation(summary = "서버 상태 확인")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"status":"UP","service":"Backend"}}
                    """)
            )
    )})
    public ResponseBody<Map<String, String>> heartbeat() {
        return ResponseBody.success(Map.of(
                "status", "UP",
                "service", "Backend"
        ));
    }

    @GetMapping("/api/heartbeat/db")
    @Operation(summary = "데이터베이스 상태 확인")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            content = @Content(examples = @ExampleObject(value = """
                    {"success":true,"data":{"status":"UP","database":"Oracle"}}
                    """)
            )
    )})
    public ResponseBody<Map<String, String>> databaseHeartbeat() {
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

            return ResponseBody.success(Map.of(
                    "status", "UP",
                    "database", "Oracle"
            ));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
