package dbp.backend.member.service;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.dao.MemberDao;
import dbp.backend.member.dto.MemberLoginRequest;
import dbp.backend.member.dto.MemberResponse;
import dbp.backend.member.dto.MemberSignupRequest;
import dbp.backend.member.exception.MemberErrorCode;
import dbp.backend.member.model.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class MemberManager {
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerifyManager emailVerifyManager;

    public MemberManager(
            MemberDao memberDao,
            PasswordEncoder passwordEncoder,
            EmailVerifyManager emailVerifyManager
    ) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
        this.emailVerifyManager = emailVerifyManager;
    }

    public MemberResponse signup(MemberSignupRequest request) {
        String email = validateSignupRequest(request);

        if (memberDao.existsByEmail(email)) {
            throw new ApiException(
                    MemberErrorCode.DUPLICATE_EMAIL
            );
        }

        String code = request.verificationCode();
        if (code == null || !code.matches("[0-9]{6}")) {
            throw new ApiException(MemberErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        String nickname = generateRandomNickname();

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (emailVerifyManager.verifyAndConsume(conn, email, code)) {
                    memberDao.save(conn, email, passwordHash, nickname);

                    Member savedMember = memberDao.findByEmail(conn, email);
                    if (savedMember == null) {
                        throw new ApiException(MemberErrorCode.MEMBER_NOT_FOUND);
                    }

                    MemberResponse response = MemberResponse.from(savedMember);
                    conn.commit();
                    return response;
                }
                conn.commit(); // 코드 불일치 시 실패 횟수 증가를 위한 커밋

            } catch (SQLException | RuntimeException e) {
                JDBCUtil.rollbackQuietly(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        throw new ApiException(MemberErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
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

        String email = SchoolEmailPolicy.normalize(request.email());
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

        return MemberResponse.from(member);
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

        return MemberResponse.from(member);
    }

    private String validateSignupRequest(
            MemberSignupRequest request
    ) {
        if (request == null
                || request.password() == null
                || request.passwordConfirm() == null
                || request.password().isBlank()
                || request.passwordConfirm().isBlank()) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        String email = SchoolEmailPolicy.normalizeAndValidate(request.email());

        if (!request.password().matches(PASSWORD_PATTERN)) {
            throw new ApiException(
                    MemberErrorCode.INVALID_PASSWORD_FORMAT
            );
        }

        if (!request.password().equals(
                request.passwordConfirm()
        )) {
            throw new ApiException(
                    MemberErrorCode.PASSWORD_CONFIRM_MISMATCH
            );
        }
        return email;
    }

    private String generateRandomNickname() {
        String[] surnames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "고", "신"};
        SecureRandom random = new SecureRandom();

        String surname = surnames[random.nextInt(surnames.length)];
        int number = random.nextInt(1000);

        return surname + "솜솜" + String.format("%03d", number);
    }
}