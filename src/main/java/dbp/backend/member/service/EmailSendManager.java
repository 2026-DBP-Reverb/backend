package dbp.backend.member.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.member.dao.MemberDao;
import dbp.backend.member.exception.MemberErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailSendManager {
    private final MemberDao memberDao;
    private final EmailVerifyManager emailVerifyManager;
    private final JavaMailSender mailSender;
    private final String from;

    public EmailSendManager(
            MemberDao memberDao,
            EmailVerifyManager emailVerifyManager,
            JavaMailSender mailSender,
            @Value("${email-from}") String from
    ) {
        this.memberDao = memberDao;
        this.emailVerifyManager = emailVerifyManager;
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendCode(String email) {
        String normalizedEmail = SchoolEmailPolicy.normalizeAndValidate(email);
        if (memberDao.existsByEmail(normalizedEmail)) {
            throw new ApiException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        if (from.isBlank()) {
            throw new ApiException(MemberErrorCode.EMAIL_VERIFICATION_DELIVERY_FAILED);
        }

        Optional<String> issuedCode = emailVerifyManager.issue(normalizedEmail);
        if (issuedCode.isEmpty()) {
            return;
        }

        String code = issuedCode.get();
        SimpleMailMessage message = createSimpleMailMessage(normalizedEmail, code);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            try {
                emailVerifyManager.expireFailedDelivery(normalizedEmail, code);
            } catch (RuntimeException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw new ApiException(MemberErrorCode.EMAIL_VERIFICATION_DELIVERY_FAILED, e);
        }
    }

    private SimpleMailMessage createSimpleMailMessage(String normalizedEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(normalizedEmail);
        message.setSubject("[Reverb] 회원가입 이메일 인증 코드");
        message.setText(code + "\n\n5분 이내에 입력해 주세요.");
        return message;
    }
}
