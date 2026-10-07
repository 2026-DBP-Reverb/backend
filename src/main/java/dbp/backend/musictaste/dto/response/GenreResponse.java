package dbp.backend.musictaste.dto.response;

import dbp.backend.musictaste.model.Genre;

public record GenreResponse(
        Long genreId,
        String genreName,
        boolean selected
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getGenreId(),
                genre.getGenreName(),
                genre.isSelected()
        );
    }
}
