package dbp.backend.meetup.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.meetup.dao.MeetupDao;
import dbp.backend.meetup.dto.request.MeetupCreateRequest;
import dbp.backend.meetup.dto.request.MeetupParticipantStatusUpdateRequest;
import dbp.backend.meetup.dto.request.MeetupUpdateRequest;
import dbp.backend.meetup.dto.response.MeetupDetailResponse;
import dbp.backend.meetup.dto.response.MeetupListResponse;
import dbp.backend.meetup.dto.response.MeetupResponse;
import dbp.backend.meetup.exception.MeetupErrorCode;
import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupJoinResult;
import dbp.backend.meetup.model.MeetupLeaveResult;
import dbp.backend.meetup.model.MeetupParticipant;
import dbp.backend.meetup.model.MeetupStatus;
import dbp.backend.meetup.model.MeetupUpdateResult;
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

        String title = trim(request.title());
        String place = trim(request.place());
        String imageUrl = trim(request.imageUrl());

        validateUpdateRequest(request, title, place, imageUrl);

        Meetup updateMeetup = new Meetup(
                meetup.getMeetupId(),
                meetup.getHostMemberId(),
                title,
                request.description(),
                imageUrl,
                request.category(),
                place,
                request.capacity(),
                request.startsAt(),
                meetup.getStatus(),
                meetup.getCreatedAt()
        );

        MeetupUpdateResult result = meetupDao.update(updateMeetup);

        switch (result) {
            case SUCCESS -> {
            }
            case MEETUP_NOT_FOUND -> throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
            case CAPACITY_BELOW_PARTICIPANT_COUNT ->
                    throw new ApiException(
                            MeetupErrorCode
                                    .CAPACITY_BELOW_PARTICIPANT_COUNT
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

    // 모임 참여
    public void joinMeetup(
            Long meetupId,
            Long loginMemberId
    ){
        MeetupJoinResult result = meetupDao.join(meetupId,loginMemberId);

        switch (result) {
            case SUCCESS -> {
            }
            case MEETUP_NOT_FOUND -> throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
            case MEETUP_NOT_OPEN -> throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_OPEN
            );
            case ALREADY_JOINED -> throw new ApiException(
                    MeetupErrorCode.ALREADY_JOINED
            );
            case CAPACITY_FULL -> throw new ApiException(
                    MeetupErrorCode.MEETUP_CAPACITY_FULL);
        }
    }

    // 모임 탈퇴
    public void leaveMeetup(
            Long meetupId,
            Long loginMemberId,
            MeetupParticipantStatusUpdateRequest request
    ){
        if (request == null || !"LEFT".equals(request.status())) {
            throw new ApiException(
                    CommonErrorCode.BAD_REQUEST
            );
        }

        MeetupLeaveResult result = meetupDao.leave(meetupId, loginMemberId);

        switch (result) {
            case SUCCESS -> {
            }
            case MEETUP_NOT_FOUND -> throw new ApiException(
                    MeetupErrorCode.MEETUP_NOT_FOUND
            );
            case HOST_CANNOT_LEAVE -> throw new ApiException(
                    MeetupErrorCode.HOST_CANNOT_LEAVE_MEETUP
            );
            case LEAVE_NOT_ALLOWED -> throw new ApiException(
                    MeetupErrorCode.MEETUP_LEAVE_NOT_ALLOWED
            );
            case NOT_JOINED -> throw new ApiException(
                    MeetupErrorCode.NOT_MEETUP_PARTICIPANT
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

        if(meetup.getStatus() == MeetupStatus.COMPLETED
                || !meetup.getStartsAt().isAfter(LocalDateTime.now())){
            throw new ApiException(
                    MeetupErrorCode.COMPLETED_MEETUP_CANNOT_BE_CHANGED
            );
        }
    }

    // 모임 생성 요청값 검증
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

    // 모임 수정 요청값 검증
    private void validateUpdateRequest(
            MeetupUpdateRequest request,
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

    //모임 목록 기능
    public List<MeetupListResponse> getMeetups(
            Long loginMemberId
    ){
        return meetupDao.findAll(loginMemberId);
    }

}
