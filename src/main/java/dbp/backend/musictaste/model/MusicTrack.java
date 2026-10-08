package dbp.backend.musictaste.model;

public class MusicTrack {
    private String spotifyTrackId;
    private String trackName;
    private String artistName;
    private String imageUrl;

    public MusicTrack(
            String spotifyTrackId,
            String trackName,
            String artistName,
            String imageUrl
    ) {
        this.spotifyTrackId = spotifyTrackId;
        this.trackName = trackName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    public String getSpotifyTrackId() {
        return spotifyTrackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
