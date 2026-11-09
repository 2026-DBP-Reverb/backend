package dbp.backend.member.dao;

import dbp.backend.common.dao.JDBCUtil;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 이메일 인증 코드의 저장과 상태 변경을 담당하는 DAO입니다.
 */
@Repository
public class EmailVerificationDao {

    private static final String ACTIVE = """
            USED_AT IS NULL AND FAILED_ATTEMPTS < 5
            AND EXPIRES_AT > SYSTIMESTAMP
            """;

    private static final String REPLACE = """
            UPDATE EMAIL_VERIFICATION
            SET CODE_MAC = ?,
                EXPIRES_AT = SYSTIMESTAMP + INTERVAL '5' MINUTE,
                FAILED_ATTEMPTS = 0,
                USED_AT = NULL,
                RESEND_AFTER = SYSTIMESTAMP + INTERVAL '3' MINUTE
            WHERE EMAIL = ? AND RESEND_AFTER <= SYSTIMESTAMP
            """;

    private static final String INSERT = """
            INSERT INTO EMAIL_VERIFICATION (EMAIL, CODE_MAC, EXPIRES_AT, RESEND_AFTER)
            VALUES (?, ?, SYSTIMESTAMP + INTERVAL '5' MINUTE,
                          SYSTIMESTAMP + INTERVAL '3' MINUTE)
            """;

    // 이메일에 해당하는 인증 행을 조회하고 잠금
    public boolean lockVerificationRow(Connection conn, String email) throws SQLException {
        return JDBCUtil.executeQuery(conn,
                "SELECT email FROM email_verification WHERE email = ? FOR UPDATE",
                ResultSet::next, email);
    }

    // 이메일의 최초 인증 코드를 저장
    public boolean insertIfAbsent(Connection conn, String email, String digest) throws SQLException {
        try {
            JDBCUtil.executeUpdate(conn, INSERT, email, digest);
            return true;
        } catch (SQLException e) {
            // Oracle ORA-00001
            if (e.getErrorCode() == 1) {
                return false;
            }
            throw e;
        }
    }

    // 재발급 대기 시간이 지난 인증 행의 HMAC을 교체
    public boolean replaceAfterCooldown(Connection conn, String email, String digest)
            throws SQLException {
        return JDBCUtil.executeUpdate(conn, REPLACE, digest, email) == 1;
    }

    // 이메일에 저장된 HMAC을 조회, 재발급 시 새 코드가 직전 코드와 같은지 확인하는 데 사용
    public String findDigest(Connection conn, String email) throws SQLException {
        return JDBCUtil.executeQuery(conn,
                "SELECT CODE_MAC FROM EMAIL_VERIFICATION WHERE EMAIL = ?",
                rs -> rs.next() ? rs.getString(1) : null, email);
    }

    // 만료되지 않았고 아직 사용되지 않았으며 실패 횟수가 5회 미만인 코드의 HMAC을 조회
    public String findActiveDigest(Connection conn, String email) throws SQLException {
        return JDBCUtil.executeQuery(conn,
                "SELECT CODE_MAC FROM EMAIL_VERIFICATION WHERE EMAIL = ? AND " + ACTIVE,
                rs -> rs.next() ? rs.getString(1) : null,
                email);
    }

    // 코드가 검증 가능한 상태일 때 실패 횟수를 1 증가
    public void recordFailedAttempt(Connection conn, String email) throws SQLException {
        JDBCUtil.executeUpdate(conn,
                "UPDATE EMAIL_VERIFICATION SET FAILED_ATTEMPTS = FAILED_ATTEMPTS + 1"
                        + " WHERE EMAIL = ? AND " + ACTIVE,
                email);
    }

    public void expireFailedDelivery(Connection conn, String email, String digest)
            throws SQLException {
        JDBCUtil.executeUpdate(conn, """
                UPDATE EMAIL_VERIFICATION
                SET EXPIRES_AT = SYSTIMESTAMP, RESEND_AFTER = SYSTIMESTAMP
                WHERE EMAIL = ? AND CODE_MAC = ? AND USED_AT IS NULL
                """, email, digest);
    }

    // 코드가 검증 가능한 상태일 때 현재 DB 시각을 사용 시각으로 기록
    public boolean markUsed(Connection conn, String email) throws SQLException {
        return JDBCUtil.executeUpdate(conn,
                "UPDATE EMAIL_VERIFICATION SET USED_AT = SYSTIMESTAMP"
                        + " WHERE EMAIL = ? AND " + ACTIVE,
                email) == 1;
    }
}
