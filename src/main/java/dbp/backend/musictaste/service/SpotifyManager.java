package dbp.backend.musictaste.service;

import dbp.backend.common.config.spotify.SpotifyProperties;
import dbp.backend.common.exception.ApiException;
import dbp.backend.musictaste.dto.response.SpotifyTokenResponse;
import dbp.backend.musictaste.exception.MusicTasteErrorCode;
import dbp.backend.musictaste.model.MusicArtist;
import dbp.backend.musictaste.model.MusicTrack;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

/**
 * 자바 프로그램-스포티파이 간 요청을 위한 매니저 클래스
 */
@Service
public class SpotifyManager {
    private final RestClient spotifyApiRestClient;
    private final RestClient spotifyAccountsRestClient;
    private final SpotifyProperties spotifyProperties;

    // 스포티파이에서 전달되는 액세스 토큰 정보 (검색 등 요청에 사용)
    private String accessToken;
    private Instant tokenExpiresAt;

    public SpotifyManager(
            @Qualifier("spotifyApiRestClient") RestClient spotifyApiRestClient,
            @Qualifier("spotifyAccountsRestClient") RestClient spotifyAccountsRestClient,
            SpotifyProperties spotifyProperties
    ) {
        this.spotifyApiRestClient = spotifyApiRestClient;
        this.spotifyAccountsRestClient = spotifyAccountsRestClient;
        this.spotifyProperties = spotifyProperties;
    }

    /**
     * 아티스트 검색
     *
     * @param query 검색어
     * @param limit 검색 결과 개수 제한
     * @return 검색 결과 리스트
     */
    public List<MusicArtist> searchArtists(
            String query,
            int limit
    ) {
        JsonNode response = search(query, "artist", limit);

        return response.path("artists").path("items")
                .valueStream()
                .map(this::toMusicArtist)
                .toList();
    }

    /**
     * 트랙 (곡) 검색
     *
     * @param query 검색어
     * @param limit 검색 결과 개수 제한
     * @return 검색 결과 리스트
     */
    public List<MusicTrack> searchTracks(
            String query,
            int limit
    ) {
        JsonNode response = search(query, "track", limit);

        return response.path("tracks").path("items")
                .valueStream()
                .map(this::toMusicTrack)
                .toList();
    }

    /**
     * 스포티파이 검색 API 호출
     */
    private JsonNode search(String query, String type, int limit) {
        return spotifyApiRestClient.get()
                .uri(builder -> builder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("type", type)
                        .queryParam("market", "KR")
                        .queryParam("limit", limit)
                        .build())
                .header("Authorization", "Bearer " + getAccessToken())
                .retrieve()
                .body(JsonNode.class);
    }

    private MusicArtist toMusicArtist(JsonNode artist) {
        return new MusicArtist(
                artist.path("id").asString(),
                artist.path("name").asString(),
                firstImageUrl(artist.path("images"))
        );
    }

    private MusicTrack toMusicTrack(JsonNode track) {
        return new MusicTrack(
                track.path("id").asString(),
                track.path("name").asString(),
                artistNames(track.path("artists")),
                firstImageUrl(track.path("album").path("images"))
        );
    }

    private String firstImageUrl(JsonNode images) {
        if (!images.isArray() || images.isEmpty()) {
            return null;
        }
        return images.get(0).path("url").asString(null);
    }

    private String artistNames(JsonNode artists) {
        return artists.valueStream()
                .map(artist -> artist.path("name").asString())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    /**
     * 스포티파이 API 요청 시 쓰이는 토큰 받아오기
     */
    private synchronized String getAccessToken() {
        if (accessToken != null
                && tokenExpiresAt.isAfter(Instant.now().plusSeconds(30))) {
            return accessToken;
        }

        SpotifyTokenResponse response = spotifyAccountsRestClient.post()
                .uri("/api/token")
                .headers(headers -> headers.setBasicAuth(
                        spotifyProperties.clientId(),
                        spotifyProperties.clientSecret()
                ))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(SpotifyTokenResponse.class);

        if (response != null) {
            accessToken = response.accessToken();
            tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn());
            return accessToken;
        }

        throw new ApiException(MusicTasteErrorCode.SPOTIFY_SEARCH_FAILED);
    }
}
