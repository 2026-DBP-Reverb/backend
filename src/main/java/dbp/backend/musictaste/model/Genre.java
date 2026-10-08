package dbp.backend.musictaste.model;

public class Genre {
    private Long genreId;
    private String genreName;
    private boolean selected;

    public Genre(
            Long genreId,
            String genreName,
            boolean selected
    ) {
        this.genreId = genreId;
        this.genreName = genreName;
        this.selected = selected;
    }

    public Long getGenreId() {
        return genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public boolean isSelected() {
        return selected;
    }
}
