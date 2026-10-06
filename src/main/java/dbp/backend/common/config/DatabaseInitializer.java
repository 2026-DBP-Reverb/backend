package dbp.backend.common.config;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        String sql = new String(
                new ClassPathResource("db/schema.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        try (Connection conn = ConnectionManager.getConnection()) {
            for (String query : sql.split(";")) {
                if (query.isBlank()) {
                    continue;
                }
                try {
                    JDBCUtil.executeUpdate(conn, query);
                } catch (SQLException e) {
                    // 이미 존재하는 테이블(ORA-00955) 오류는 무시
                    if (e.getErrorCode() != 955) {
                        throw e;
                    }
                }
            }
        }
    }
}
