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
        String title = trim(request.title());
        String place = trim(request.place());
        String imageUrl = trim(request.imageUrl());

        validateCreateRequest(request, title, place, imageUrl);

        Meetup meetup = new Meetup(
                null,
                hostMemberId,
                title,
                request.description(),
                imageUrl,
                request.category(),
                place,
                request.capacity(),
                request.startsAt(),
                null,
                MeetupStatus.OPEN,
                null
        );

        Meetup savedMeetup = meetupDao.save(meetup);

        return MeetupResponse.from(savedMeetup);
    }

    private void validateCreateRequest(
            MeetupCreateRequest request,
            String title,
            String place,
            String imageUrl
    ){
        if (title == null || title.isEmpty()) {
            throw new ApiException(MeetupErrorCode.INVALID_TITLE);
        }

        if (title.length() > 200) {
            throw new ApiException(MeetupErrorCode.INVALID_TITLE_LENGTH);
        }

        if (request.capacity() == null
                || request.capacity() < 1
                || request.capacity() > 99999) {
            throw new ApiException(MeetupErrorCode.INVALID_CAPACITY);
        }

        if (place == null || place.isEmpty()) {
            throw new ApiException(MeetupErrorCode.INVALID_PLACE);
        }

        if (place.length() > 255) {
            throw new ApiException(MeetupErrorCode.INVALID_PLACE_LENGTH);
        }

        if (imageUrl != null && imageUrl.length() > 500) {
            throw new ApiException(MeetupErrorCode.INVALID_IMAGE_URL_LENGTH);
        }

        if (request.category() == null) {
            throw new ApiException(MeetupErrorCode.INVALID_CATEGORY);
        }

        if (request.startsAt() == null
                || !request.startsAt().isAfter(LocalDateTime.now())) {
            throw new ApiException(MeetupErrorCode.INVALID_STARTS_AT);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
