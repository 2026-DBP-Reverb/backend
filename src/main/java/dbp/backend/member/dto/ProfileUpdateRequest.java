package dbp.backend.member.dto;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.member.exception.MemberErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public record ProfileUpdateRequest(
        @Schema(description = "닉네임", example = "박솜솜")
        String nickname,
        @Schema(description = "인스타그램 아이디, 빈 문자열을 보내면 삭제", example = "reverb_music")
        String instagramId,
        @Schema(description = "외부 이미지의 전체 HTTP 또는 HTTPS URL", example = "https://example.com/profile.png")
        String profileImageUrl
) {
    public ProfileUpdateRequest validated() {
        if (nickname == null && instagramId == null && profileImageUrl == null) {
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        }
        return new ProfileUpdateRequest(
                normalizeNickname(nickname),
                normalizeInstagramId(instagramId),
                validateImageUrl(profileImageUrl)
        );
    }

    private static String normalizeNickname(String value) {
        if (value == null) {
            return null;
        }
        String nickname = value.strip();
        if (nickname.isEmpty() || nickname.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new ApiException(MemberErrorCode.INVALID_NICKNAME);
        }
        return nickname;
    }

    private static String normalizeInstagramId(String value) {
        if (value == null) {
            return null;
        }
        String instagramId = value.strip();
        if (instagramId.isEmpty()) {
            return "";
        }
        if (instagramId.startsWith("@")) {
            instagramId = instagramId.substring(1);
        }
        if (!instagramId.matches("[A-Za-z0-9_][A-Za-z0-9_.]{0,29}")
                || instagramId.endsWith(".") || instagramId.contains("..")) {
            throw new ApiException(MemberErrorCode.INVALID_INSTAGRAM_ID);
        }
        return instagramId;
    }

    private static String validateImageUrl(String value) {
        if (value == null) {
            return null;
        }

        if (value.getBytes(StandardCharsets.UTF_8).length > 500) {
            throw new ApiException(MemberErrorCode.INVALID_PROFILE_IMAGE_URL);
        }
        try {
            URI uri = new URI(value);
            if (uri.getHost() != null
                    && uri.getRawUserInfo() == null
                    && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
                return value;
            }
        } catch (URISyntaxException e) {
            throw new ApiException(MemberErrorCode.INVALID_PROFILE_IMAGE_URL, e);
        }
        throw new ApiException(MemberErrorCode.INVALID_PROFILE_IMAGE_URL);
    }
}
