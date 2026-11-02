package dbp.backend.recommendation.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
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
}