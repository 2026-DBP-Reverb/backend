package dbp.backend.home.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.home.dto.response.HomeMeetupCardResponse;
import dbp.backend.home.service.HomeManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.home.dto.response.HomeMemberCardResponse;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final HomeManager homeManager;

    public HomeController(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Operation(summary = "홈 최근 등록 모임 조회")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @GetMapping("/meetups")
    public ResponseEntity<
            ResponseBody<List<HomeMeetupCardResponse>>
            > getRecentMeetups(
                    HttpServletRequest request
    ) {
        getMemberId(request);

        List<HomeMeetupCardResponse> response =
                homeManager.getRecentMeetups();

        return ResponseEntity.ok(
                new ResponseBody<>(response)
        );
    }

    @Operation(summary = "홈 최근 가입 회원 조회")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @GetMapping("/members")
    public ResponseEntity<
            ResponseBody<List<HomeMemberCardResponse>>
            > getRecentMembers(
            HttpServletRequest request
    ) {
        Long memberId = getMemberId(request);

        List<HomeMemberCardResponse> response =
                homeManager.getRecentMembers(memberId);

        return ResponseEntity.ok(
                new ResponseBody<>(response)
        );
    }

    private Long getMemberId(
            HttpServletRequest request
    ) {
        Long memberId =
                SessionUtil.getLoginMemberId(request);

        if (memberId == null) {
            throw new ApiException(
                    CommonErrorCode.UNAUTHORIZED
            );
        }

        return memberId;
    }
}