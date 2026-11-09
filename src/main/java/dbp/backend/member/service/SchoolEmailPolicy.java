package dbp.backend.member.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.exception.MemberErrorCode;

import java.util.Locale;
import java.util.regex.Pattern;

final class SchoolEmailPolicy {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^20[0-9]{6}@dongduk\\.ac\\.kr$");

    private SchoolEmailPolicy() {
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String normalizeAndValidate(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        }

        String normalizedEmail = normalize(email);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ApiException(MemberErrorCode.INVALID_EMAIL_FORMAT);
        }
        return normalizedEmail;
    }
}
