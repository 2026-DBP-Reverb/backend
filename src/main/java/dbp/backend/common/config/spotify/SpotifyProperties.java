package dbp.backend.common.config.spotify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public record SpotifyProperties(
        String clientId,
        String clientSecret,
        String apiBaseUrl,
        String accountsBaseUrl
) {
}
