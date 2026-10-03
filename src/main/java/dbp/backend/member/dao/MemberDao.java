package dbp.backend.member.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.member.model.Member;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MemberDao {

    public boolean existsByEmail(String email) {
        String sql = """
                SELECT COUNT(*)
                FROM MEMBER
                WHERE EMAIL = ?
                """;

        try (Connection conn = ConnectionManager.getConnection()) {
            Integer count = JDBCUtil.executeQuery(
                    conn,
                    sql,
                    rs -> {
                        rs.next();
                        return rs.getInt(1);
                    },
                    email
            );

            return count > 0;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void save(String email, String passwordHash) {
        String sql = """
                INSERT INTO MEMBER (
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH
                ) VALUES (
                    MEMBER_SEQ.NEXTVAL,
                    ?,
                    ?
                )
                """;

        try (Connection conn = ConnectionManager.getConnection()) {
            JDBCUtil.executeUpdate(
                    conn,
                    sql,
                    email,
                    passwordHash
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Member findByEmail(String email) {
        String sql = """
                SELECT
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH,
                    CREATED_AT
                FROM MEMBER
                WHERE EMAIL = ?
                """;

        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    this::mapSingleMember,
                    email
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Member findById(Long memberId) {
        String sql = """
                SELECT
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH,
                    CREATED_AT
                FROM MEMBER
                WHERE MEMBER_ID = ?
                """;

        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    this::mapSingleMember,
                    memberId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private Member mapSingleMember(ResultSet rs)
            throws SQLException {
        if (!rs.next()) {
            return null;
        }

        return new Member(
                rs.getLong("MEMBER_ID"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD_HASH"),
                rs.getTimestamp("CREATED_AT").toLocalDateTime()
        );
    }
}