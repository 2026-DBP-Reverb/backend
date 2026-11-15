package dbp.backend.settlement.dao;

import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

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
            INSERT INTO settlement_share(settlement_id, member_id, meetup_id, amount)
            VALUES (#{settlementId}, #{participant.memberId}, #{meetupId}, #{participant.amount})
            """)
    int insertSettlementShare(
            Long settlementId,
            Long meetupId,
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
                    WHEN EXISTS(  -- 정산에 멤버별 정산항목이 존재하고
                        SELECT 1
                        FROM settlement_share ss
                        WHERE ss.settlement_id = s.settlement_id
                    ) AND NOT EXISTS(  -- 멤버별 정산항목이 confirmed가 아닌 게 하나도 존재하지 않는 경우
                        SELECT 1
                        FROM settlement_share ss
                        WHERE ss.settlement_id = s.settlement_id AND ss.status <> 'CONFIRMED'
                    ) THEN 1  -- 모든 멤버별 정산항목이 완료됨
                    ELSE 0
                END AS completed
            FROM settlement s, member m
            WHERE s.paid_by_member_id = m.member_id
              AND meetup_id = #{meetupId}
            ORDER BY s.created_at DESC, settlement_id DESC
            """)
    List<SettlementSummaryResponse> findSettlementsByMeetupId(Long meetupId);
}
