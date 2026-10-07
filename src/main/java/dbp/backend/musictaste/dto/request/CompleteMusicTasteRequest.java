package dbp.backend.musictaste.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "사용자의 전체 음악 취향 선택 정보",
        example = """
                {
                  "artists": [
                    {
                      "spotifyArtistId": "6HvZYsbFfjnjFrWF950C9d",
                      "artistName": "NewJeans",
                      "imageUrl": "https://i.scdn.co/image/ab6761610000e5eb841bdcf28a956f3a384ffcf4"
                    }
                  ],
                  "genres": [
                    {
                      "genreId": 1
                    },
                    {
                      "genreId": 3
                    }
                  ],
                  "tracks": [
                    {
                      "spotifyTrackId": "0a4MMyCrzT0En247IhqZbD",
                      "trackName": "Hype Boy",
                      "artistName": "NewJeans",
                      "imageUrl": "https://i.scdn.co/image/ab67616d0000b2739d28fd01859073a3ae6ea209"
                    }
                  ]
                }
                """
)
public record CompleteMusicTasteRequest(
        List<ArtistTasteRequest> artists,
        List<GenreTasteRequest> genres,
        List<TrackTasteRequest> tracks
) {
}
