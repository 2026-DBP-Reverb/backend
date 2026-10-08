package dbp.backend.common.config.spotify;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SpotifyProperties.class)
public class SpotifyHttpConfig {
    private final SpotifyProperties spotifyProperties;

    // 스포티파이 api 설정값 주입
    public SpotifyHttpConfig(SpotifyProperties spotifyProperties) {
        this.spotifyProperties = spotifyProperties;
    }

    @Bean
    public RestClient spotifyApiRestClient() {
        return RestClient.builder()
                .baseUrl(spotifyProperties.apiBaseUrl())
                .build();
    }

    @Bean
    public RestClient spotifyAccountsRestClient() {
        return RestClient.builder()
                .baseUrl(spotifyProperties.accountsBaseUrl())
                .build();
    }
}
