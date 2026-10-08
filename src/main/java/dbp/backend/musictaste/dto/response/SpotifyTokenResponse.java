package dbp.backend.musictaste.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") long expiresIn
) {
}
