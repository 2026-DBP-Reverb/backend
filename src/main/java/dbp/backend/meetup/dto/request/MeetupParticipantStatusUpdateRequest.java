package dbp.backend.meetup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MeetupParticipantStatusUpdateRequest(
        @Schema(
                description = "변경할 참여 상태",
                example = "LEFT",
                allowableValues = {"LEFT"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String status
) {
}
