package dbp.backend.member.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.dao.MemberDao;
import dbp.backend.member.dto.MemberLoginRequest;
import dbp.backend.member.dto.MemberResponse;
import dbp.backend.member.dto.MemberSignupRequest;
import dbp.backend.member.exception.MemberErrorCode;
import dbp.backend.member.model.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MemberService {
    private static final String EMAIL_PATTERN =
            "^20[0-9]{6}@dongduk\\.ac\\.kr$";

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberDao memberDao,
            PasswordEncoder passwordEncoder
    ) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberResponse signup(MemberSignupRequest request) {
        validateSignupRequest(request);

        String email = normalizeEmail(request.email());

        if (memberDao.existsByEmail(email)) {
            throw new ApiException(
                    MemberErrorCode.DUPLICATE_EMAIL
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        memberDao.save(email, passwordHash);

        Member savedMember = memberDao.findByEmail(email);

        if (savedMember == null) {
            throw new ApiException(
                    MemberErrorCode.MEMBER_NOT_FOUND
            );
        }

        return toResponse(savedMember);
    }

    public MemberResponse login(MemberLoginRequest request) {
        if (request == null
                || request.email() == null
                || request.password() == null
                || request.email().isBlank()
                || request.password().isBlank()) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        String email = normalizeEmail(request.email());
        Member member = memberDao.findByEmail(email);

        if (member == null
                || !passwordEncoder.matches(
                request.password(),
                member.getPasswordHash()
        )) {
            throw new ApiException(
                    MemberErrorCode.INVALID_LOGIN_CREDENTIALS
            );
        }

        return toResponse(member);
    }

    public MemberResponse findById(Long memberId) {
        if (memberId == null) {
            throw new ApiException(
                    CommonErrorCode.UNAUTHORIZED
            );
        }

        Member member = memberDao.findById(memberId);

        if (member == null) {
            throw new ApiException(
                    MemberErrorCode.MEMBER_NOT_FOUND
            );
        }

        return toResponse(member);
    }

    private void validateSignupRequest(
            MemberSignupRequest request
    ) {
        if (request == null
                || request.email() == null
                || request.password() == null
                || request.passwordConfirm() == null
                || request.email().isBlank()
                || request.password().isBlank()
                || request.passwordConfirm().isBlank()) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        String email = normalizeEmail(request.email());

        if (!email.matches(EMAIL_PATTERN)) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        if (!request.password().matches(PASSWORD_PATTERN)) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        if (!request.password().equals(
                request.passwordConfirm()
        )) {
            throw new ApiException(
                    MemberErrorCode.PASSWORD_CONFIRM_MISMATCH
            );
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getProfileImageUrl(),
                member.getInstagramId(),
                member.getNickname(),
                member.getCreatedAt()
        );
    }
}