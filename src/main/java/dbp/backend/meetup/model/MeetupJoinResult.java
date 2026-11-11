package dbp.backend.meetup.model;

public enum MeetupJoinResult {
    SUCCESS,
    MEETUP_NOT_FOUND,
    MEETUP_NOT_OPEN,
    ALREADY_JOINED,
    CAPACITY_FULL
}
