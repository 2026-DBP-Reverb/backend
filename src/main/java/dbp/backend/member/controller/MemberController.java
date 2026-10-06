package dbp.backend.member.controller;

import dbp.backend.common.config.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.member.dto.MemberLoginRequest;
import dbp.backend.member.dto.MemberResponse;
import dbp.backend.member.dto.MemberSignupRequest;
import dbp.backend.member.service.MemberManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberManager memberManager;

    public MemberController(MemberManager memberManager) {
        this.memberManager = memberManager;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ResponseBody<MemberResponse>> signup(
            @RequestBody MemberSignupRequest request
    ) {
        MemberResponse member = memberManager.signup(request);
        return ResponseEntity.ok(new ResponseBody<>(member));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ResponseBody<MemberResponse>> login(
            @RequestBody MemberLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        MemberResponse member = memberManager.login(request);

        SessionUtil.login(
                httpRequest,
                member.memberId()
        );

        return ResponseEntity.ok(new ResponseBody<>(member));
    }

    @Operation(summary = "로그아웃")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @PostMapping("/logout")
    public ResponseEntity<ResponseBody<?>> logout(
            HttpServletRequest request
    ) {
        if (!SessionUtil.isLoggedIn(request)) {
            throw new ApiException(
                    CommonErrorCode.UNAUTHORIZED
            );
        }

        SessionUtil.logout(request);
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @GetMapping("/me")
    public ResponseEntity<ResponseBody<MemberResponse>> me(
            HttpServletRequest request
    ) {
        Long memberId =
                SessionUtil.getLoginMemberId(request);

        if (memberId == null) {
            throw new ApiException(
                    CommonErrorCode.UNAUTHORIZED
            );
        }

        MemberResponse member =
                memberManager.findById(memberId);

        return ResponseEntity.ok(new ResponseBody<>(member));
    }
}
