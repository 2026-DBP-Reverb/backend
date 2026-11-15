package dbp.backend.settlement.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import dbp.backend.settlement.service.SettlementManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SettlementController", description = "모임 정산 API")
@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementManager settlementManager;

    public SettlementController(SettlementManager settlementManager) {
        this.settlementManager = settlementManager;
    }

    @Operation(summary = "새 정산 생성")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    @PostMapping()
    public ResponseEntity<ResponseBody<Void>> createSettlement(
            @RequestBody CreateSettlementRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Long memberId = SessionUtil.getLoginMemberId(httpServletRequest);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }
        settlementManager.createSettlement(memberId, request);
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    @Operation(summary = "모임별 정산 목록 조회")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<ResponseBody<List<SettlementSummaryResponse>>> getSettlements(
            @RequestParam("meetupId") Long meetupId,
            HttpServletRequest httpServletRequest
    ) {
        Long memberId = SessionUtil.getLoginMemberId(httpServletRequest);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }

        List<SettlementSummaryResponse> settlements = settlementManager.getSettlementsList(memberId, meetupId);
        return ResponseEntity.ok(new ResponseBody<>(settlements));
    }
}
