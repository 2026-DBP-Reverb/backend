package dbp.backend.meetup.model;

import java.time.LocalDateTime;

public class Meetup {
    private final Long meetupId;
    private final Long hostMemberId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final MeetupCategory category;
    private final String place;
    private final Integer capacity;
    private final LocalDateTime startsAt;
    private final MeetupStatus status;
    private final LocalDateTime createdAt;

    public Meetup(
            Long meetupId,
            Long hostMemberId,
            String title,
            String description,
            String imageUrl,
            MeetupCategory category,
            String place,
            Integer capacity,
            LocalDateTime startsAt,
            MeetupStatus status,
            LocalDateTime createdAt
    ){
        this.meetupId = meetupId;
        this.hostMemberId = hostMemberId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.place = place;
        this.capacity = capacity;
        this.startsAt = startsAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getMeetupId(){
        return meetupId;
    }

    public Long getHostMemberId(){
        return hostMemberId;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public MeetupCategory getCategory() {
        return category;
    }

    public String getPlace() {
        return place;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public MeetupStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
