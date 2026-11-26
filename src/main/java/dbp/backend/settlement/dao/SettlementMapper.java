package dbp.backend.settlement.dao;

import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementDetailResponse;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import dbp.backend.settlement.model.SettlementConfirmation;
import dbp.backend.settlement.model.SettlementDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementMapper {

    @Select("SELECT settlement_seq.nextval FROM dual")
    Long nextSettlementId();

    @Select("""
            SELECT meetup_id
            FROM meetup
            WHERE meetup_id = #{meetupId}
            FOR UPDATE
            """)
    Long lockMeetupById(Long meetupId);

    @Insert("""
            INSERT INTO settlement (settlement_id, meetup_id, paid_by_member_id,
                                    title, total_amount, account, split_method, spent_at)
            VALUES (#{settlementId}, #{request.meetupId}, #{memberId}, #{request.title},
                    #{request.totalAmount}, #{request.account}, #{request.splitMethod}, #{request.spentAt})
            """)
    int insertSettlement(
            Long settlementId,
            Long memberId,
            CreateSettlementRequest request
    );

    @Insert("""
            INSERT INTO settlement_share(settlement_id, member_id, meetup_id, amount, status, confirmed_at)
            VALUES (#{settlementId},
                    #{participant.memberId},
                    #{meetupId},
                    #{participant.amount},
                    #{status},
                    #{confirmedAt,jdbcType=TIMESTAMP})
            """)
    int insertSettlementShare(
            Long settlementId,
            Long meetupId,
            String status,
            LocalDateTime confirmedAt,
            CreateSettlementRequest.SettlementParticipant participant
    );

    @Select("""
            SELECT member_id
            FROM meetup_participant
            WHERE meetup_id = #{meetupId}
              AND status = 'ACTIVE'
            """)
    List<Long> findParticipantMemberIds(Long meetupId);

    @Select("""
            SELECT
                settlement_id,
                title,
                total_amount,
                spent_at,
                m.nickname,
                CASE
                    WHEN EXISTS(
                        SELECT 1
                        FROM settlement_share ss
                        WHERE ss.settlement_id = s.settlement_id
                    ) AND NOT EXISTS(
                        SELECT 1
                        FROM settlement_share ss
                        WHERE ss.settlement_id = s.settlement_id AND ss.status <> 'CONFIRMED'
                    ) THEN 1
                    ELSE 0
                END AS completed
            FROM settlement s, member m
            WHERE s.paid_by_member_id = m.member_id
              AND meetup_id = #{meetupId}
            ORDER BY s.created_at DESC, settlement_id DESC
            """)
    List<SettlementSummaryResponse> findSettlementsByMeetupId(Long meetupId);

    @Select("""
            SELECT s.settlement_id,
                   s.meetup_id,
                   s.title,
                   s.total_amount,
                   s.spent_at,
                   s.created_at,
                   s.account,
                   s.split_method,
                   s.paid_by_member_id,
                   m.nickname
            FROM settlement s, member m
            WHERE s.paid_by_member_id = m.member_id
            AND s.settlement_id = #{settlementId}
            """)
    SettlementDetail findSettlementDetail(Long settlementId);

    @Select("""
            SELECT ss.member_id,
                   m.nickname,
                   m.profile_image_url,
                   ss.amount,
                   ss.status,
                   ss.confirmed_at
            FROM settlement_share ss, member m
            WHERE ss.member_id = m.member_id
              AND ss.settlement_id = #{settlementId}
            ORDER BY ss.member_id
            """)
    List<SettlementDetailResponse.ShareItem> findSettlementShares(Long settlementId);

    // 내 정산 항목을 송금 보고 상태로 변경 (UNPAID → REQUESTED)
    // 반환값: 바뀐 행 수 (0이면 대상 아님 또는 이미 보고함)
    @Update("""
            UPDATE settlement_share
            SET status = 'REQUESTED'
            WHERE settlement_id = #{settlementId}
            AND member_id = #{memberId}
            AND status = 'UNPAID'
            """)
    int requestTransfer(Long settlementId, Long memberId);

    @Update("""
            UPDATE settlement_share
            SET status = 'CONFIRMED', confirmed_at = SYSTIMESTAMP
            WHERE settlement_id = #{settlementId}
              AND member_id = #{requestedMemberId}
              AND status = 'REQUESTED'
              AND EXISTS (
                  SELECT 1 FROM settlement s
                  WHERE s.settlement_id = #{settlementId}
                    AND s.paid_by_member_id = #{paidByMemberId}
              )
            """)
    int confirmTransfer(Long settlementId, Long requestedMemberId, Long paidByMemberId);

    @Select("""
            SELECT status, confirmed_at
            FROM settlement_share
            WHERE settlement_id = #{settlementId}
              AND member_id = #{memberId}
            """)
    SettlementConfirmation findSettlementConfirmation(Long settlementId, Long memberId);
}
