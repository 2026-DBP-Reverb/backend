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

    public void save(String email, String passwordHash, String nickname) {
        try (Connection conn = ConnectionManager.getConnection()) {
            save(conn, email, passwordHash, nickname);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void save(
            Connection conn,
            String email,
            String passwordHash,
            String nickname
    ) throws SQLException {
        String sql = """
                INSERT INTO MEMBER (
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH,
                    NICKNAME,
                    PROFILE_IMAGE_URL
                ) VALUES (
                    MEMBER_SEQ.NEXTVAL,
                    ?,
                    ?,
                    ?,
                    '/images/default-profile.png'
                )
                """;

        JDBCUtil.executeUpdate(conn, sql, email, passwordHash, nickname);
    }

    public Member findByEmail(String email) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return findByEmail(conn, email);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Member findByEmail(Connection conn, String email) throws SQLException {
        String sql = """
                SELECT
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH,
                    PROFILE_IMAGE_URL,
                    INSTAGRAM_ID,
                    NICKNAME,
                    CREATED_AT
                FROM MEMBER
                WHERE EMAIL = ?
                """;

        return JDBCUtil.executeQuery(conn, sql, this::mapSingleMember, email);
    }

    public Member findById(Long memberId) {
        String sql = """
                SELECT
                    MEMBER_ID,
                    EMAIL,
                    PASSWORD_HASH,
                    PROFILE_IMAGE_URL,
                    INSTAGRAM_ID,
                    NICKNAME,
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
                rs.getString("PROFILE_IMAGE_URL"),
                rs.getString("INSTAGRAM_ID"),
                rs.getString("NICKNAME"),
                rs.getTimestamp("CREATED_AT").toLocalDateTime()
        );
    }
}