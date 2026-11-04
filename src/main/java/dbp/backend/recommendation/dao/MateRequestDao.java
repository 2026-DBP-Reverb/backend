package dbp.backend.recommendation.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.recommendation.model.MyMate;
import dbp.backend.recommendation.model.ReceivedRequest;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MateRequestDao {
    public List<ReceivedRequest> findReceivedRequests(Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT request_id, requester_id, nickname, profile_image_url, requested_at 
                            FROM mate_match_request r inner join member m on r.requester_id = m.member_id
                            WHERE recipient_id = ? AND STATUS = 'PENDING'
                            ORDER BY requested_at DESC
                            
                            """,
                    rs -> {
                        List<ReceivedRequest> receivedRequests = new ArrayList<>();
                        while (rs.next()) {
                            receivedRequests.add(new ReceivedRequest(
                                    rs.getLong("request_id"),
                                    rs.getLong("requester_id"),
                                    rs.getString("nickname"),
                                    rs.getString("profile_image_url"),
                                    rs.getTimestamp("requested_at").toLocalDateTime()
                            ));
                        }
                        return receivedRequests;
                    },
                    memberId);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean rejectRequest(Long requestId, Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            int updated = JDBCUtil.executeUpdate(
                    conn,
                    """
                            UPDATE MATE_MATCH_REQUEST
                            SET status = 'REJECTED', responded_at = systimestamp
                            WHERE request_id = ? AND status = 'PENDING' AND recipient_id = ?
                            """,
                    requestId,
                    memberId
            );
            return updated == 1;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    //받은 메이트 요청을 수락하고 매칭 생성 (트랜잭션: 하나라도 실패하면 전부 취소)
    public boolean acceptRequest(
            Long requestId,
            Long memberId
    ) {
        try (Connection conn = ConnectionManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);
                int updated = JDBCUtil.executeUpdate(conn,
                        """
                            UPDATE MATE_MATCH_REQUEST
                            SET status = 'ACCEPTED', responded_at = systimestamp
                            WHERE request_id = ? AND status = 'PENDING' AND recipient_id = ?
                            """,
                requestId,
                memberId
                );

                if (updated != 1) {
                    JDBCUtil.rollbackQuietly(conn);
                    return false;
                }
                JDBCUtil.executeUpdate(conn,
                """
                        INSERT INTO mate_match (match_id, request_id, member_a_id, member_b_id)
                        SELECT mate_match_seq.NEXTVAL, request_id, requester_id, recipient_id
                        FROM mate_match_request
                        WHERE request_id = ?
                        """,
                        requestId);
                conn.commit();
                return true;
            } catch (SQLException e) {                    // 안쪽 try의 catch
                JDBCUtil.rollbackQuietly(conn);
                throw new DatabaseException(e);
            } finally {                                   // 안쪽 try의 finally
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {                        // 바깥 try의 catch
            throw new DatabaseException(e);
        }
    }

    // 나의 메이트 목록 조회 (매칭 완료 + 보낸 요청 + 받은 요청)
    public List<MyMate> findMyMates(Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT 'MATCHED' AS status, mm.request_id,
                                   m.member_id, m.nickname, m.profile_image_url, m.instagram_id
                            FROM mate_match mm
                            JOIN member m
                              ON m.member_id = CASE WHEN mm.member_a_id = ? THEN mm.member_b_id ELSE mm.member_a_id END
                            WHERE mm.ended_at IS NULL
                              AND (mm.member_a_id = ? OR mm.member_b_id = ?)
                            UNION ALL
                            
                            SELECT 'SENT', r.request_id,
                                   m.member_id, m.nickname, m.profile_image_url, CAST(NULL AS VARCHAR2(100))
                            FROM mate_match_request r
                            JOIN member m ON m.member_id = r.recipient_id
                            WHERE r.requester_id = ? AND r.status = 'PENDING'
                            UNION ALL
                            
                            SELECT 'RECEIVED', r.request_id,
                                   m.member_id, m.nickname, m.profile_image_url, CAST(NULL AS VARCHAR2(100))
                            FROM mate_match_request r
                            JOIN member m ON m.member_id = r.requester_id
                            WHERE r.recipient_id = ? AND r.status = 'PENDING'
                            """,
                    rs -> {
                        List<MyMate> myMates = new ArrayList<>();
                        while (rs.next()) {
                            myMates.add(new MyMate(
                                    rs.getString("status"),
                                    rs.getLong("request_id"),
                                    rs.getLong("member_id"),
                                    rs.getString("nickname"),
                                    rs.getString("profile_image_url"),
                                    rs.getString("instagram_id")
                            ));
                        }
                        return myMates;
                    },
                    memberId,
                    memberId,
                    memberId,
                    memberId,
                    memberId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}