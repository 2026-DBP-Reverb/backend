package dbp.backend.meetup.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.meetup.dto.response.MeetupListResponse;
import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupCategory;
import dbp.backend.meetup.model.MeetupJoinResult;
import dbp.backend.meetup.model.MeetupLeaveResult;
import dbp.backend.meetup.model.MeetupParticipant;
import dbp.backend.meetup.model.MeetupStatus;
import dbp.backend.meetup.model.MeetupUpdateResult;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    public int countActiveParticipants(Long meetupId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return countActiveParticipants(
                    conn,
                    meetupId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    private int countActiveParticipants(
            Connection conn,
            Long meetupId
    ) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM meetup_participant
            WHERE meetup_id = ?
              AND status = 'ACTIVE'
            """;

        return JDBCUtil.executeQuery(
                conn,
                sql,
                rs -> {
                    rs.next();
                    return rs.getInt(1);
                },
                meetupId
        );
    }

    //모임 정보 수정
    public MeetupUpdateResult update(Meetup meetup) {
        try (Connection conn = ConnectionManager.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            boolean transactionCompleted = false;
            Throwable transactionFailure = null;

            try {
                conn.setAutoCommit(false);

                MeetupUpdateResult result = updateInTransaction(
                        conn,
                        meetup
                );

                if (result == MeetupUpdateResult.SUCCESS) {
                    conn.commit();
                } else {
                    conn.rollback();
                }

                transactionCompleted = true;
                return result;

            } catch (SQLException | RuntimeException e) {
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

            } finally {
                if (transactionCompleted) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException restoreException) {
                        if (transactionFailure != null) {
                            transactionFailure.addSuppressed(
                                    restoreException
                            );
                        } else {
                            throw restoreException;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private MeetupUpdateResult updateInTransaction(
            Connection conn,
            Meetup meetup
    ) throws SQLException {
        Meetup savedMeetup = findByIdForUpdate(
                conn,
                meetup.getMeetupId()
        );

        if (savedMeetup == null
                || savedMeetup.getStatus() == MeetupStatus.CANCELLED
                || savedMeetup.getStatus() == MeetupStatus.COMPLETED) {
            return MeetupUpdateResult.MEETUP_NOT_FOUND;
        }

        int participantCount = countActiveParticipants(
                conn,
                meetup.getMeetupId()
        );

        if (meetup.getCapacity() < participantCount) {
            return MeetupUpdateResult
                    .CAPACITY_BELOW_PARTICIPANT_COUNT;
        }

        boolean updated = update(
                conn,
                meetup
        );

        if (!updated) {
            return MeetupUpdateResult.MEETUP_NOT_FOUND;
        }

        return MeetupUpdateResult.SUCCESS;
    }

    private boolean update(
            Connection conn,
            Meetup meetup
    ) throws SQLException {
        String sql = """
            UPDATE meetup
            SET title = ?,
                description = ?,
                image_url = ?,
                category = ?,
                place = ?,
                capacity = ?,
                starts_at = ?
            WHERE meetup_id = ?
              AND host_member_id = ?
              AND status NOT IN ('CANCELLED', 'COMPLETED')
            """;

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

        return updated == 1;
    }

    // 모임 삭제 처리
    public boolean delete(
            Long meetupId,
            Long hostMemberId
    ) {
        String participantDeleteSql = """
                DELETE FROM meetup_participant
                WHERE meetup_id = ?
                """;

        String meetupDeleteSql = """
                DELETE FROM meetup
                WHERE meetup_id = ?
                  AND host_member_id = ?
                  AND status NOT IN ('CANCELLED', 'COMPLETED')
                """;

        try (Connection conn = ConnectionManager.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            boolean transactionCompleted = false;
            Throwable transactionFailure = null;

            try {
                conn.setAutoCommit(false);

                Meetup meetup = findByIdForUpdate(
                        conn,
                        meetupId
                );

                if (meetup == null
                        || !meetup.getHostMemberId().equals(hostMemberId)
                        || meetup.getStatus() == MeetupStatus.CANCELLED
                        || meetup.getStatus() == MeetupStatus.COMPLETED) {
                    conn.rollback();
                    transactionCompleted = true;
                    return false;
                }

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
                    conn.rollback();
                    transactionCompleted = true;
                    return false;
                }

                conn.commit();
                transactionCompleted = true;
                return true;

            } catch (SQLException | RuntimeException e) {
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

            } finally {
                if (transactionCompleted) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException restoreException) {
                        if (transactionFailure != null) {
                            transactionFailure.addSuppressed(
                                    restoreException
                            );
                        } else {
                            throw restoreException;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 모임 참여
    public MeetupJoinResult join(
            Long meetupId,
            Long memberId
    ){
        try(Connection conn = ConnectionManager.getConnection()){
            boolean autoCommit = conn.getAutoCommit();
            boolean transactionCompleted = false;
            Throwable transactionFailure = null;

            try{
                conn.setAutoCommit(false);

                MeetupJoinResult result = joinInTransaction(
                        conn,
                        meetupId,
                        memberId
                );

                if(result ==MeetupJoinResult.SUCCESS){
                    conn.commit();
                }else{
                    conn.rollback();
                }

                transactionCompleted = true;
                return result;
            }catch (SQLException | RuntimeException e){
                transactionFailure = e;

                try{
                    conn.rollback();
                    transactionCompleted = true;
                }catch(SQLException rollbackException){
                    e.addSuppressed(rollbackException);
                }

                if( e instanceof SQLException sqlException){
                    throw new DatabaseException(sqlException);
                }

                throw(RuntimeException) e;
            }finally {
                if(transactionCompleted){
                    try{
                        conn.setAutoCommit(autoCommit);
                    }catch (SQLException restoreException){
                        if(transactionFailure != null){
                            transactionFailure.addSuppressed(restoreException);
                        }else{
                            throw restoreException;
                        }
                    }
                }
            }
        }catch (SQLException e){
            throw new DatabaseException(e);
        }
    }

    private MeetupJoinResult joinInTransaction(
            Connection conn,
            Long meetupId,
            Long memberId
    )throws SQLException{
        Meetup meetup = findByIdForUpdate(conn,meetupId);

        if(meetup == null){
            return MeetupJoinResult.MEETUP_NOT_FOUND;
        }

        if(meetup.getStatus() != MeetupStatus.OPEN || !meetup.getStartsAt().isAfter(LocalDateTime.now())){
            return MeetupJoinResult.MEETUP_NOT_OPEN;
        }

        String participantStatus = findParticipantStatus(
                conn,
                meetupId,
                memberId
        );

        if("ACTIVE".equals(participantStatus)){
            return MeetupJoinResult.ALREADY_JOINED;
        }

        int participantCount = countActiveParticipants(conn,meetupId);

        if(participantCount >= meetup.getCapacity()){
            return MeetupJoinResult.CAPACITY_FULL;
        }

        if("LEFT".equals(participantStatus)){
            boolean reactivated = reactivateParticipant(
                    conn,
                    meetupId,
                    memberId
            );

            if(!reactivated) {
                throw new SQLException(
                        "모임 참여 상태를 변경할 수 없습니다."
                );
            }
        }else{
            boolean inserted = insertParticipant(
                    conn,
                    meetupId,
                    memberId
            );

            if(!inserted){
                throw  new SQLException(
                        "모임 참여 정보를 저장할 수 없습니다."
                );
            }
        }

        return MeetupJoinResult.SUCCESS;
    }

    private Meetup findByIdForUpdate(
            Connection conn,
            Long meetupId
    )throws SQLException{
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
            FOR UPDATE
            """;
        return JDBCUtil.executeQuery(
                conn,
                sql,
                rs -> {
                    if(!rs.next()){
                        return null;
                    }

                    Timestamp endsAt = rs.getTimestamp("ends_at");

                    return new Meetup(
                            rs.getLong("meetup_id"),
                            rs.getLong("host_member_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("image_url"),
                            MeetupCategory.valueOf(rs.getString("category")),
                            rs.getString("place"),
                            rs.getInt("capacity"),
                            rs.getTimestamp("starts_at").toLocalDateTime(),
                            endsAt == null ? null : endsAt.toLocalDateTime(),
                            MeetupStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at")
                                    .toLocalDateTime()
                    );
                },
                meetupId
        );
    }

    private String findParticipantStatus(
            Connection conn,
            Long meetupId,
            Long memberId
    )throws SQLException {
        String sql = """
            SELECT status
            FROM meetup_participant
            WHERE meetup_id = ?
              AND member_id = ?
            """;

        return JDBCUtil.executeQuery(
                conn,
                sql,
                rs -> {
                    if(!rs.next()){
                        return null;
                    }
                    return rs.getString("status");
                },
                meetupId,
                memberId
        );
    }

    private boolean reactivateParticipant(
            Connection conn,
            Long meetupId,
            Long memberId
    ) throws SQLException{
        String sql = """
                UPDATE meetup_participant
                SET status = 'ACTIVE',
                      joined_at = SYSTIMESTAMP,
                      left_at = NULL
                WHERE meetup_id = ?
                   AND member_id = ?
                   AND status = 'LEFT'
                """;
        int updated = JDBCUtil.executeUpdate(
                conn,
                sql,
                meetupId,
                memberId
        );

        return updated == 1;
    }

    private boolean insertParticipant(
            Connection conn,
            Long meetupId,
            Long memberId
    ) throws SQLException {
        String sql = """
            INSERT INTO meetup_participant (
                meetup_id,
                member_id,
                status,
                joined_at,
                left_at
            ) VALUES (?, ?, 'ACTIVE', SYSTIMESTAMP, NULL)
            """;
        int inserted = JDBCUtil.executeUpdate(
                conn,
                sql,
                meetupId,
                memberId
        );

        return inserted == 1;
    }

    // 모임 탈퇴
    public MeetupLeaveResult leave(
            Long meetupId,
            Long memberId
    ){
        try(Connection conn = ConnectionManager.getConnection()){
            boolean autoCommit = conn.getAutoCommit();
            boolean transactionCompleted = false;
            Throwable transactionFailure = null;

            try {
                conn.setAutoCommit(false);

                MeetupLeaveResult result = leaveInTransaction(
                        conn,
                        meetupId,
                        memberId
                );

                if (result == MeetupLeaveResult.SUCCESS) {
                    conn.commit();
                } else {
                    conn.rollback();
                }

                transactionCompleted = true;
                return result;

            } catch (SQLException | RuntimeException e) {
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

            } finally {
                if (transactionCompleted) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException restoreException) {
                        if (transactionFailure != null) {
                            transactionFailure.addSuppressed(
                                    restoreException
                            );
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

    private MeetupLeaveResult leaveInTransaction (
            Connection conn,
            Long meetupId,
            Long memberId
    ) throws SQLException{
        Meetup meetup = findByIdForUpdate(conn, meetupId);

        if (meetup == null || meetup.getStatus() == MeetupStatus.CANCELLED) {
            return MeetupLeaveResult.MEETUP_NOT_FOUND;
        }

        if (meetup.getHostMemberId().equals(memberId)) {
            return MeetupLeaveResult.HOST_CANNOT_LEAVE;
        }

        if (meetup.getStatus() == MeetupStatus.COMPLETED
                || !meetup.getStartsAt().isAfter(LocalDateTime.now())) {
            return MeetupLeaveResult.LEAVE_NOT_ALLOWED;
        }

        String participantStatus = findParticipantStatus(
                conn,
                meetupId,
                memberId
        );

        if (!"ACTIVE".equals(participantStatus)) {
            return MeetupLeaveResult.NOT_JOINED;
        }

        boolean left = deactivateParticipant(
                conn,
                meetupId,
                memberId
        );

        if (!left) {
            throw new SQLException(
                    "모임 탈퇴 상태를 변경할 수 없습니다."
            );
        }

        return MeetupLeaveResult.SUCCESS;
    }

    private boolean deactivateParticipant(
            Connection conn,
            Long meetupId,
            Long memberId
    ) throws SQLException {
        String sql = """
                UPDATE meetup_participant
                SET status = 'LEFT',
                    left_at = SYSTIMESTAMP
                WHERE meetup_id = ?
                  AND member_id = ?
                  AND status = 'ACTIVE'
                """;
        int updated = JDBCUtil.executeUpdate(
                conn,
                sql,
                meetupId,
                memberId
        );

        return updated == 1;
    }

    //모임 목록 카드
    public List<MeetupListResponse> findAll(
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
                            AND mp.status = 'ACTIVE'
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
                FROM meetup m
                WHERE m.status IN ('OPEN', 'CLOSED')
                ORDER BY m.starts_at ASC, m.meetup_id DESC
                """;

        try (Connection conn =
                ConnectionManager.getConnection()){
            return JDBCUtil.executeQuery(
                    conn,
                    sql,
                    rs -> {
                        List<MeetupListResponse> meetups =
                                new ArrayList<>();

                        while(rs.next()) {
                            meetups.add(
                                    new MeetupListResponse(
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
