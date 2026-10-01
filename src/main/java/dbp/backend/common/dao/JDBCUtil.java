package dbp.backend.common.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JDBCUtil {
    private JDBCUtil() {
    }

    // insert, update, delete용
    public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindParameters(pstmt, params);
            return pstmt.executeUpdate();
        }
    }

    // select용
    public static <T> T executeQuery(
            Connection conn,
            String sql,
            ResultSetMapper<T> mapper,
            Object... params
    ) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                return mapper.map(rs);
            }
        }
    }

    public static void bindParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    public static void commitQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.commit();
        } catch (SQLException ignored) {
        }
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    public static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }

        try {
            resource.close();
        } catch (Exception ignored) {
        }
    }
}
