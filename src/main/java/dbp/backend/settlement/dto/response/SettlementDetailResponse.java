package dbp.backend.settlement.dto.response;

import dbp.backend.common.util.ProfileImageUrls;

import java.time.LocalDate;
import java.util.List;

public record SettlementDetailResponse(
        Long settlementId,
        String title,
        int totalAmount,
        LocalDate spentAt,
        String account,              // 송금받을 계좌
        String splitMethod,          // EQUAL / WEIGHTED
        Long paidByMemberId,         // 대표 결제자 id
        String paidByNickname,
        List<ShareItem> shares       // 멤버별 정산 항목
) {
    // 멤버 한 명의 정산 항목
    public record ShareItem(
            Long memberId,
            String nickname,
            String profileImageUrl,
            int amount,
            String status            // UNPAID / REQUESTED / CONFIRMED
    ) {
        // 생성될 때 프로필 이미지를 전체 URL로 변환 (없으면 기본 이미지)
        public ShareItem {
            profileImageUrl = ProfileImageUrls.resolve(profileImageUrl);
        }
    }
}

