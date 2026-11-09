package dbp.backend.member.service;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.dao.EmailVerificationDao;
import dbp.backend.member.exception.MemberErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmailVerifyManager {
    private static final String ALGORITHM = "HmacSHA256";

    private final EmailVerificationDao emailVerificationDao;
    private final SecureRandom random = new SecureRandom();
    private final SecretKeySpec key;

    // dao와 HMAC 서명 키 초기화
    public EmailVerifyManager(
            EmailVerificationDao emailVerificationDao,
            @Value("${email-hmac-key}") String encodedKey
    ) {
        this.emailVerificationDao = emailVerificationDao;
        byte[] bytes = Base64.getDecoder().decode(encodedKey);
        if (bytes.length < 32) {
            throw new ApiException(MemberErrorCode.EMAIL_HMAC_KEY_INVALID);
        }
        this.key = new SecretKeySpec(bytes, ALGORITHM);
    }

    // 정규화된 이메일에 인증 코드를 발급하고 발급 트랜잭션을 커밋
    Optional<String> issue(String normalizedEmail) {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<String> code = issueLocked(conn, normalizedEmail);
                conn.commit();
                return code;
            } catch (SQLException | RuntimeException e) {
                JDBCUtil.rollbackQuietly(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    void expireFailedDelivery(String normalizedEmail, String code) {
        try (Connection conn = ConnectionManager.getConnection()) {
            emailVerificationDao.expireFailedDelivery(conn, normalizedEmail, digest(normalizedEmail, code));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 이메일의 인증 행 잠근 뒤 최초 발급 or 재발급
    private Optional<String> issueLocked(Connection conn, String email) throws SQLException {
        if (emailVerificationDao.lockVerificationRow(conn, email)) {
            return replaceCode(conn, email);
        }

        String code = newCode();
        if (emailVerificationDao.insertIfAbsent(conn, email, digest(email, code))) {
            return Optional.of(code);
        }

        if (!emailVerificationDao.lockVerificationRow(conn, email)) {
            throw new DatabaseException();
        }
        return replaceCode(conn, email);
    }

    // 인증 코드 재발급
    private Optional<String> replaceCode(Connection conn, String email) throws SQLException {
        String previousDigest = emailVerificationDao.findDigest(conn, email);

        String code;
        String nextDigest;
        do {
            code = newCode();
            nextDigest = digest(email, code);
        } while (nextDigest.equals(previousDigest));

        return emailVerificationDao.replaceAfterCooldown(conn, email, nextDigest)
                ? Optional.of(code)
                : Optional.empty();
    }

    // 인증 코드 생성
    private String newCode() {
        return String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
    }

    // 이메일과 코드로 계산한 HMAC을 DB 저장용 16진수 문자열로 변환
    private String digest(String email, String code) {
        return HexFormat.of().formatHex(mac(email, code));
    }

    // 인증 코드 확인 (사용 처리 없음)
    public void confirm(String email, String code) {
        String normalizedEmail = SchoolEmailPolicy.normalizeAndValidate(email);
        boolean verified;
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                verified = verifyCode(conn, normalizedEmail, code);
                conn.commit(); // 오입력으로 증가한 실패 횟수 저장
            } catch (SQLException | RuntimeException e) {
                JDBCUtil.rollbackQuietly(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        if (!verified) {
            throw new ApiException(MemberErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }
    }

    // 유효한 인증 코드를 검증하고 일치하면 사용 처리
    public boolean verifyAndConsume(Connection conn, String normalizedEmail, String code) {
        try {
            return verifyCode(conn, normalizedEmail, code)
                    && emailVerificationDao.markUsed(conn, normalizedEmail);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 인증 코드 유효성 검증
    private boolean verifyCode(Connection conn, String normalizedEmail, String code) throws SQLException {
        if (conn.getAutoCommit()) {
            throw new DatabaseException();
        }
        if (code == null || !code.matches("[0-9]{6}")) {
            return false;
        }
        if (!emailVerificationDao.lockVerificationRow(conn, normalizedEmail)) {
            return false;
        }

        String digest = emailVerificationDao.findActiveDigest(conn, normalizedEmail);
        if (digest == null) {
            return false;
        }

        boolean matches = MessageDigest.isEqual(
                HexFormat.of().parseHex(digest), mac(normalizedEmail, code));
        if (!matches) {
            emailVerificationDao.recordFailedAttempt(conn, normalizedEmail);
        }
        return matches;
    }

    // 이메일과 코드의 조합에 대해 HMAC-SHA256을 계산
    private byte[] mac(String email, String code) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return mac.doFinal((email + ":" + code).getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new ApiException(
                    CommonErrorCode.INTERNAL_SERVER_ERROR,
                    new Throwable("이메일 인증 코드의 HMAC을 계산할 수 없습니다.")
            );
        }
    }
}
