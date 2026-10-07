package dbp.backend.musictaste.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import dbp.backend.musictaste.model.MusicTrack;

@Schema(description = "Spotify 곡 정보")
public record TrackResponse(
        @Schema(
                description = "Spotify 곡 ID",
                example = "0a4MMyCrzT0En247IhqZbD"
        )
        String spotifyTrackId,

        @Schema(
                description = "곡 이름",
                example = "Hype Boy"
        )
        String trackName,

        @Schema(
                description = "아티스트 이름",
                example = "NewJeans"
        )
        String artistName,

        @Schema(
                description = "곡 커버 이미지 URL",
                example = "https://i.scdn.co/image/ab67616d0000b2739d28fd01859073a3ae6ea209"
        )
        String imageUrl
) {
    public static TrackResponse from(MusicTrack track) {
        return new TrackResponse(
                track.getSpotifyTrackId(),
                track.getTrackName(),
                track.getArtistName(),
                track.getImageUrl()
        );
    }
}
