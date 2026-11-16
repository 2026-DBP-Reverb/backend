package dbp.backend.meetup.model;

public enum MeetupKickResult {
    SUCCESS,
    MEETUP_NOT_FOUND,
    NOT_MEETUP_HOST,
    HOST_CANNOT_BE_KICKED,
    KICK_NOT_ALLOWED,
    NOT_JOINED,
    SETTLEMENT_EXISTS
}
