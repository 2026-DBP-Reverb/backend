package dbp.backend.recommendation.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.recommendation.dto.response.MateRecommendationResponse;
import dbp.backend.recommendation.service.MateRecommendationManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import dbp.backend.common.response.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mate Recommendation", description = "메이트 추천 API")
@RestController
@RequestMapping("/api/mates")
public class MateRecommendationController {
    private final MateRecommendationManager mateRecommendationManager;

    public MateRecommendationController(MateRecommendationManager mateRecommendationManager) {
        this.mateRecommendationManager = mateRecommendationManager;
    }

    @Operation(summary = "메이트 추천 리스트 조회")
    @GetMapping("/recommendations")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    public ResponseEntity<ResponseBody<List<MateRecommendationResponse>>> getRecommendations(
            HttpServletRequest request
    ){
        return ResponseEntity.ok(new ResponseBody<>(
                mateRecommendationManager.getRecommendations(getMemberId(request))
        ));
    }

    private Long getMemberId(HttpServletRequest request) {
        Long memberId = SessionUtil.getLoginMemberId(request);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }
        return memberId;
    }

}
