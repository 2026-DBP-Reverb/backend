package dbp.backend.meetup.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.meetup.dao.MeetupDao;
import dbp.backend.meetup.dto.request.MeetupCreateRequest;
import dbp.backend.meetup.dto.request.MeetupUpdateRequest;
import dbp.backend.meetup.dto.response.MeetupDetailResponse;
import dbp.backend.meetup.dto.response.MeetupResponse;
import dbp.backend.meetup.exception.MeetupErrorCode;
import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupParticipant;
import dbp.backend.meetup.model.MeetupStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeetupManager {
    private final MeetupDao meetupDao;

    public MeetupManager(MeetupDao meetupDao){
        this.meetupDao = meetupDao;
    }

    // 모임 생성
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

    // 모임 상세 정보 및 참여자 조회
    public MeetupDetailResponse getMeetup(Long meetupId, Long loginMemberId){
        Meetup meetup = getMeetupOrThrow(meetupId);

        if (meetup.getStatus() == MeetupStatus.CANCELLED) {
            throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }

        List<MeetupParticipant> participants = meetupDao.findActiveParticipants(meetupId);

        return MeetupDetailResponse.from(meetup,participants,loginMemberId);
    }

    // 모임 정보 수정
    public MeetupResponse updateMeetup(
            Long meetupId,
            Long loginMemberId,
            MeetupUpdateRequest request
    ){
        Meetup meetup = getMeetupOrThrow(meetupId);
        validateMeetupChange(meetup, loginMemberId);

        validateUpdateRequest(request);

        int participantCount = meetupDao.countActiveParticipants(meetupId);

        if(request.capacity()<participantCount){
            throw new ApiException(
                    MeetupErrorCode.CAPACITY_BELOW_PARTICIPANT_COUNT
            );
        }

        Meetup updateMeetup = new Meetup(
                meetup.getMeetupId(),
                meetup.getHostMemberId(),
                request.title().trim(),
                request.description(),
                request.imageUrl(),
                request.category(),
                request.place().trim(),
                request.capacity(),
                request.startsAt(),
                meetup.getEndsAt(),
                meetup.getStatus(),
                meetup.getCreatedAt()
        );

        boolean updated = meetupDao.update(updateMeetup);

        if(!updated){
            throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }

        Meetup savedMeetup = meetupDao.findById(meetupId);

        return MeetupResponse.from(savedMeetup);
    }

    // 모임 삭제
    public void deleteMeetup(
            Long meetupId,
            Long loginMemberId
    ){
        Meetup meetup = getMeetupOrThrow(meetupId);
        validateMeetupChange(meetup, loginMemberId);

        boolean deleted = meetupDao.delete(meetupId, loginMemberId);

        if (!deleted) {
            throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }
    }

    // 모임 조회 및 존재 여부 확인
    private Meetup getMeetupOrThrow(Long meetupId){
        Meetup meetup = meetupDao.findById(meetupId);

        if(meetup == null){
            throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }

        return meetup;
    }

    // 모임 변경 권한 및 상태 검증
    private void validateMeetupChange(Meetup meetup, Long loginMemberId){
        if(!meetup.getHostMemberId().equals(loginMemberId)){
            throw new ApiException(
                    MeetupErrorCode.NOT_MEETUP_HOST
            );
        }

        if(meetup.getStatus() == MeetupStatus.CANCELLED){
            throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
        }

        if(meetup.getStatus() == MeetupStatus.COMPLETED){
            throw new ApiException(
                    MeetupErrorCode.COMPLETED_MEETUP_CANNOT_BE_CHANGED
            );
        }
    }

    //모임 생성 요청값 검증
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


    // 모임 수정 요청값 검증
    private void validateUpdateRequest(MeetupUpdateRequest request){
        if (request.title() == null || request.title().isBlank()) {
            throw new ApiException(
                    MeetupErrorCode.INVALID_TITLE
            );
        }

        if (request.capacity() == null || request.capacity() < 1) {
            throw new ApiException(
                    MeetupErrorCode.INVALID_CAPACITY
            );
        }

        if (request.place() == null
                || request.place().isBlank()) {
            throw new ApiException(
                    MeetupErrorCode.INVALID_PLACE
            );
        }

        if (request.category() == null) {
            throw new ApiException(
                    MeetupErrorCode.INVALID_CATEGORY
            );
        }

        if (request.startsAt() == null || !request.startsAt().isAfter(LocalDateTime.now())) {
            throw new ApiException(
                    MeetupErrorCode.INVALID_STARTS_AT
            );
        }
    }
}
