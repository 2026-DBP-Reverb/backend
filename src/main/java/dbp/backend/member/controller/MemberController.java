package dbp.backend.member.controller;

import dbp.backend.common.config.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.member.dto.MemberLoginRequest;
import dbp.backend.member.dto.MemberResponse;
import dbp.backend.member.dto.MemberSignupRequest;
import dbp.backend.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseBody<MemberResponse> signup(
            @RequestBody MemberSignupRequest request
    ) {
        MemberResponse member = memberService.signup(request);
        return ResponseBody.success(member);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseBody<MemberResponse> login(
            @RequestBody MemberLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        MemberResponse member = memberService.login(request);

        SessionUtil.login(
                httpRequest,
                member.memberId()
        );

        return ResponseBody.success(member);
    }

    @Operation(summary = "로그아웃")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @PostMapping("/logout")
    public ResponseBody<Void> logout(
            HttpServletRequest request
    ) {
        if (!SessionUtil.isLoggedIn(request)) {
            throw new ApiException(
                    CommonErrorCode.UNAUTHORIZED
            );
        }

        SessionUtil.logout(request);
        return ResponseBody.success(null);
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @GetMapping("/me")
    public ResponseBody<MemberResponse> me(
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
                memberService.findById(memberId);

        return ResponseBody.success(member);
    }
}