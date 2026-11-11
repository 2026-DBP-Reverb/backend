package dbp.backend.meetup.model;

import java.time.LocalDateTime;

public record MeetupSummary(
    Long meetupId,
    String title,
    String imageUrl,
    String place,
    MeetupCategory category,
    Integer participantCount,
    Integer capacity,
    LocalDateTime startsAt,
    MeetupStatus status,
    boolean joined
){

}
