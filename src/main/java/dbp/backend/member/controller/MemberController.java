package dbp.backend.member.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.member.dto.*;
import dbp.backend.member.service.EmailSendManager;
import dbp.backend.member.service.EmailVerifyManager;
import dbp.backend.member.service.MemberManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberManager memberManager;
    private final EmailSendManager emailSendManager;
    private final EmailVerifyManager emailVerifyManager;

    public MemberController(
            MemberManager memberManager,
            EmailSendManager emailSendManager,
            EmailVerifyManager emailVerifyManager
    ) {
        this.memberManager = memberManager;
        this.emailSendManager = emailSendManager;
        this.emailVerifyManager = emailVerifyManager;
    }

    @Operation(summary = "회원가입 이메일 인증 코드 발송")
    @PostMapping("/email-verifications")
    public ResponseEntity<ResponseBody<Void>> sendEmailVerification(
            @RequestBody EmailVerificationRequest request
    ) {
        emailSendManager.sendCode(request.email());
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    @Operation(
            summary = "회원가입 이메일 인증 코드 확인",
            description = "인증 코드를 확인합니다. 코드는 회원가입이 완료될 때 사용 처리됩니다."
    )
    @PostMapping("/email-verifications/confirm")
    public ResponseEntity<ResponseBody<Void>> confirmEmailVerification(
            @RequestBody EmailVerificationConfirmRequest request
    ) {
        emailVerifyManager.confirm(request.email(), request.verificationCode());
        return ResponseEntity.ok(new ResponseBody<>(null));
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

    @Operation(summary = "내 프로필 수정")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME)
    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody<MemberResponse>> updateProfile(
            @RequestBody ProfileUpdateRequest update,
            HttpServletRequest request
    ) {
        Long memberId = SessionUtil.getLoginMemberId(request);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }

        MemberResponse member = memberManager.updateProfile(memberId, update.validated());
        return ResponseEntity.ok(new ResponseBody<>(member));
    }
}
