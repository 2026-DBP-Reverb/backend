package dbp.backend.musictaste.model;

public class MusicArtist {
    private String spotifyArtistId;
    private String artistName;
    private String imageUrl;

    public MusicArtist(
            String spotifyArtistId,
            String artistName,
            String imageUrl
    ) {
        this.spotifyArtistId = spotifyArtistId;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    public String getSpotifyArtistId() {
        return spotifyArtistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
