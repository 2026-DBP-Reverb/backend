package dbp.backend.meetup.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupCategory;
import dbp.backend.meetup.model.MeetupParticipant;
import dbp.backend.meetup.model.MeetupStatus;
import dbp.backend.meetup.model.MeetupSummary;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MeetupDao {

    // 모임 개설
    public Meetup save(Meetup meetup){
        String seqSql = """
                SELECT MEETUP_SEQ.NEXTVAL
                FROM DUAL
                """;
        String meetupInsertSql = """
                INSERT INTO MEETUP(
                    meetup_id,
                    host_member_id,
                    title,
                    description,
                    image_url,
                    category,
                    place,
                    capacity,
                    starts_at,
                    status
                )VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String participantInsertSql = """
                INSERT INTO MEETUP_PARTICIPANT(
                    meetup_id,
                    member_id
                )VALUES (?, ?)
                """;
        try (Connection conn = ConnectionManager.getConnection()){
            boolean autoCommit = conn.getAutoCommit();
            boolean transactionCompleted = false;
            Throwable transactionFailure = null;

            try{
                conn.setAutoCommit(false);

                Long meetupId = JDBCUtil.executeQuery(
                        conn,
                        seqSql,
                        rs -> {
                            rs.next();
                            return rs.getLong(1);
                        }
                );

                JDBCUtil.executeUpdate(
                        conn,
                        meetupInsertSql,
                        meetupId,
                        meetup.getHostMemberId(),
                        meetup.getTitle(),
                        meetup.getDescription(),
                        meetup.getImageUrl(),
                        meetup.getCategory().name(),
                        meetup.getPlace(),
                        meetup.getCapacity(),
                        Timestamp.valueOf(meetup.getStartsAt()),
                        meetup.getStatus().name()
                );
                JDBCUtil.executeUpdate(
                        conn,
                        participantInsertSql,
                        meetupId,
                        meetup.getHostMemberId()
                );

                Meetup savedMeetup = findById(conn,meetupId);

                if (savedMeetup == null) {
                    throw new SQLException("저장된 모임을 조회할 수 없습니다.");
                }

                conn.commit();
                transactionCompleted = true;
                return savedMeetup;
            }catch (SQLException | RuntimeException e){
                transactionFailure = e;

                try {
                    conn.rollback();
                    transactionCompleted = true;
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                if (e instanceof SQLException sqlException) {
                    throw new DatabaseException(sqlException);
                }

                throw (RuntimeException) e;
            }finally {
                if (transactionCompleted) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException restoreException) {
                        if (transactionFailure != null) {
                            transactionFailure.addSuppressed(restoreException);
                        } else {
                            throw restoreException;
                        }
                    }
                }
            }

        }catch (SQLException e){
            throw new DatabaseException(e);
        }
    }


    // 저장된 모임 조회
    public Meetup findById(Long meetupId){
        try(Connection conn = ConnectionManager.getConnection()){
            return findById(conn, meetupId);
        }catch(SQLException e){
            throw new DatabaseException(e);
        }
    }

    private Meetup findById(Connection conn, Long meetupId) throws SQLException{
        String sql = """
                SELECT
                    meetup_id,
                    host_member_id,
                    title,
                    description,
                    image_url,
                    category,
                    place,
                    capacity,
                    starts_at,
                    ends_at,
                    status,
                    created_at
                    FROM meetup
                    WHERE meetup_id = ?
                """;

        return JDBCUtil.executeQuery(
                conn,
                sql,
                rs -> {
                    if(!rs.next())
                        return null;
                    Timestamp endsAt = rs.getTimestamp("ends_at");

                    return new Meetup(
                            rs.getLong("meetup_id"),
                            rs.getLong("host_member_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("image_url"),
                            MeetupCategory.valueOf(
                                    rs.getString("category")
                            ),
                            rs.getString("place"),
                            rs.getInt("capacity"),
                            rs.getTimestamp("starts_at")
                                    .toLocalDateTime(),
                            endsAt == null ? null : endsAt.toLocalDateTime(),
                            MeetupStatus.valueOf(
                                    rs.getString("status")
                            ),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                },
                meetupId
        );
    }

    // 모임에 참여중인 회원 목록 조회
    public List<MeetupParticipant> findActiveParticipants(
            Long meetupId
    ){
        String sql = """
            SELECT
                m.member_id,
                m.nickname,
                m.profile_image_url
            FROM meetup_participant mp
            JOIN member m
                ON m.member_id = mp.member_id
            JOIN meetup mt
                ON mt.meetup_id = mp.meetup_id
            WHERE mp.meetup_id = ?
              AND mp.status = 'ACTIVE'
            ORDER BY CASE
                    WHEN m.member_id = mt.host_member_id
                    THEN 0
                    ELSE 1
                END,
                mp.joined_at
            """;

        try (Connection conn = ConnectionManager.getConnection()){
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    rs -> {
                        List<MeetupParticipant> participants = new ArrayList<>();

                        while(rs.next()){
                            participants.add(
                                    new MeetupParticipant(
                                            rs.getLong("member_id"),
                                            rs.getString("nickname"),
                                            rs.getString("profile_image_url")
                                    )
                            );
                        }
                        return participants;
                    },
                    meetupId
            );
        }catch (SQLException e){
            throw new DatabaseException(e);
        }
    }

    //현재 ACTIVE 상태인 참여 인원 조회
    public int countActiveParticipants(Long meetupId){
        String sql = """
                SELECT count(*)
                FROM meetup_participant mp
                WHERE mp.meetup_id = ? AND mp.status = 'ACTIVE'
                """;
        try(Connection conn = ConnectionManager.getConnection()){
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    rs -> {
                        rs.next();
                        return rs.getInt(1);
                    },
                    meetupId
            );

        }catch (SQLException e){
            throw new DatabaseException(e);
        }
    }

    //모임 정보 수정
    public boolean update(Meetup meetup){
        String sql = """
                UPDATE meetup
                SET title = ?, description = ?, image_url = ?, category = ?, place = ?, capacity = ?, starts_at = ?
                WHERE meetup_id = ? AND host_member_id = ? AND status NOT IN ('CANCELLED', 'COMPLETED')
                """;
        try(Connection conn = ConnectionManager.getConnection()){
            int updated = JDBCUtil.executeUpdate(
                            conn,
                            sql,
                            meetup.getTitle(),
                            meetup.getDescription(),
                            meetup.getImageUrl(),
                            meetup.getCategory().name(),
                            meetup.getPlace(),
                            meetup.getCapacity(),
                            Timestamp.valueOf(meetup.getStartsAt()),
                            meetup.getMeetupId(),
                            meetup.getHostMemberId()
            );
            return  updated == 1;
        }catch (SQLException e){
            throw  new DatabaseException(e);
        }
    }

    // 모임 삭제 처리
    public boolean delete(
            Long meetupId,
            Long hostMemberId
    ){
        String participantDeleteSql = """
            DELETE FROM meetup_participant
            WHERE meetup_id = ?
            """;

        String meetupDeleteSql = """
            DELETE FROM meetup
            WHERE meetup_id = ?
              AND host_member_id = ?
              AND status != 'COMPLETED'
            """;

        try (Connection conn = ConnectionManager.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                JDBCUtil.executeUpdate(
                        conn,
                        participantDeleteSql,
                        meetupId
                );

                int deleted = JDBCUtil.executeUpdate(
                        conn,
                        meetupDeleteSql,
                        meetupId,
                        hostMemberId
                );

                if (deleted != 1) {
                    JDBCUtil.rollbackQuietly(conn);
                    return false;
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                JDBCUtil.rollbackQuietly(conn);
                throw new DatabaseException(e);

            } finally {
                conn.setAutoCommit(autoCommit);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    //모임 목록 카드
    public List<MeetupSummary> findAll(
            Long loginMemberId
    ){
        String sql = """
                SELECT
                    m.meetup_id,
                    m.title,
                    m.image_url,
                    m.place,
                    m.category,
                    m.capacity,
                    m.starts_at,
                    m.status,
                    (
                        SELECT COUNT(*)
                        FROM meetup_participant mp
                        WHERE mp.meetup_id = m.meetup_id
                            AND mp. status = 'ACTIVE'
                    ) AS participant_count,
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM meetup_participant mp
                            WHERE mp.meetup_id = m.meetup_id
                                AND mp.member_id = ?
                                AND mp.status = 'ACTIVE'
                        )
                        THEN 1
                        ELSE 0
                    END AS joined
                FROM meeup m
                WHERE m.status IN ('OPEN', 'CLOSED')
                ORDER BY m.starts_at ASC, m.meetup_id DESC
                """;

        try (Connection conn =
                ConnectionManager.getConnection()){
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    rs -> {
                        List<MeetupSummary> meetups =
                                new ArrayList<>();

                        while(rs.next()) {
                            meetups.add(
                                    new MeetupSummary(
                                            rs.getLong("meetup_id"),
                                            rs.getString("title"),
                                            rs.getString("image_url"),
                                            rs.getString("place"),
                                            MeetupCategory.valueOf(rs.getString("category")),
                                            rs.getInt("participant_count"),
                                            rs.getInt("capacity"),
                                            rs.getTimestamp("starts_at").toLocalDateTime(),
                                            MeetupStatus.valueOf(rs.getString("status")),
                                            rs.getInt("joined") == 1
                                    )
                            );
                        }
                        return meetups;
                    },
                    loginMemberId
            );
        } catch(SQLException e){
            throw  new DatabaseException(e);
        }
    }
}
