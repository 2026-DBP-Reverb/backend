package dbp.backend.musictaste.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택한 아티스트 정보")
public record ArtistTasteRequest(
        @Schema(
                description = "Spotify 아티스트 ID",
                example = "6HvZYsbFfjnjFrWF950C9d"
        )
        String spotifyArtistId,

        @Schema(
                description = "아티스트 이름",
                example = "NewJeans"
        )
        String artistName,

        @Schema(
                description = "아티스트 이미지 URL",
                example = "https://i.scdn.co/image/ab6761610000e5eb841bdcf28a956f3a384ffcf4"
        )
        String imageUrl
) {
}
