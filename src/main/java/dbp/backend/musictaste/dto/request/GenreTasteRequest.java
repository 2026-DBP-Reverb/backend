package dbp.backend.musictaste.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택한 장르 정보")
public record GenreTasteRequest(
        @Schema(
                description = "장르 ID",
                example = "1"
        )
        Long genreId
) {
}
