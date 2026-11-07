package dbp.backend.meetup.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.meetup.dao.MeetupDao;
import dbp.backend.meetup.dto.request.MeetupCreateRequest;
import dbp.backend.meetup.dto.response.MeetupResponse;
import dbp.backend.meetup.exception.MeetupErrorCode;
import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MeetupManager {
    private final MeetupDao meetupDao;

    public MeetupManager(MeetupDao meetupDao){
        this.meetupDao = meetupDao;
    }

    public MeetupResponse createMeetup(
        Long hostMemberId,
        MeetupCreateRequest request
    ){
        validateCreateRequest(request);

        Meetup meetup = new Meetup(
                null,
                hostMemberId,
                request.title().trim(),
                request.description(),
                request.imageUrl(),
                request.category(),
                request.place().trim(),
                request.capacity(),
                request.startsAt(),
                null,
                MeetupStatus.OPEN,
                null
        );

        Meetup savedMeetup = meetupDao.save(meetup);

        return MeetupResponse.from(savedMeetup);
    }

    public MeetupResponse getMeetup(Long meetupId){
        Meetup meetup = meetupDao.findById(meetupId);

        if(meetup == null){
            throw new ApiException(
                MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }

        return MeetupResponse.from(meetup);
    }

    private void validateCreateRequest(MeetupCreateRequest request){
        if (request.title() == null || request.title().isBlank()) {
            throw new ApiException(MeetupErrorCode.INVALID_TITLE);
        }

        if (request.capacity() == null || request.capacity() < 1) {
            throw new ApiException(MeetupErrorCode.INVALID_CAPACITY);
        }

        if (request.place() == null || request.place().isBlank()) {
            throw new ApiException(MeetupErrorCode.INVALID_PLACE);
        }

        if (request.category() == null) {
            throw new ApiException(MeetupErrorCode.INVALID_CATEGORY);
        }

        if (request.startsAt() == null
                || !request.startsAt().isAfter(LocalDateTime.now())) {
            throw new ApiException(MeetupErrorCode.INVALID_STARTS_AT);
        }
    }
}
