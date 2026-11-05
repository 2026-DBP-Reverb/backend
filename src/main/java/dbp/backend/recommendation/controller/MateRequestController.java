package dbp.backend.recommendation.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.recommendation.dto.response.MyMateResponse;
import dbp.backend.recommendation.dto.response.ReceivedRequestResponse;
import dbp.backend.recommendation.service.MateRequestManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mate Request", description = "메이트 요청 관리 API")
@RestController
@RequestMapping("/api/mates/requests")
public class MateRequestController {
    private final MateRequestManager mateRequestManager;

    public MateRequestController(MateRequestManager mateRequestManager) {
        this.mateRequestManager = mateRequestManager;
    }

    @Operation(summary = "받은 메이트 요청 목록 조회")
    @GetMapping("/received")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    public ResponseEntity<ResponseBody<List<ReceivedRequestResponse>>> getReceivedRequests(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new ResponseBody<>(mateRequestManager.getReceivedRequests(getMemberId(request))));
    }

    @Operation(summary = "메이트 요청 거절")
    @PutMapping("/{requestId}/reject")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    public ResponseEntity<ResponseBody<Void>> rejectRequest(
            @PathVariable Long requestId,
            HttpServletRequest request
    ) {
        mateRequestManager.rejectRequest(getMemberId(request), requestId);
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    @Operation(summary = "메이트 요청 수락")
    @PutMapping("/{requestId}/accept")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    public ResponseEntity<ResponseBody<Void>> acceptRequest(
            @PathVariable Long requestId,
            HttpServletRequest request
    ) {
        mateRequestManager.acceptRequest(getMemberId(request), requestId);
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    private Long getMemberId(HttpServletRequest request) {
        Long memberId = SessionUtil.getLoginMemberId(request);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }
        return memberId;
    }

    @Operation(summary = "나의 메이트 목록 조회")
    @GetMapping("/my")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    public ResponseEntity<ResponseBody<List<MyMateResponse>>> getMyMates(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new ResponseBody<>(mateRequestManager.getMyMates(getMemberId(request))));
    }
}
