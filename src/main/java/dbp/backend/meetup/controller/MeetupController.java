package dbp.backend.meetup.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.meetup.dto.request.MeetupCreateRequest;
import dbp.backend.meetup.dto.response.MeetupResponse;
import dbp.backend.meetup.service.MeetupManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Meetup", description = "모임 API")
@RestController
@RequestMapping("/api/meetups")
public class MeetupController {

    private final MeetupManager meetupManager;

    public MeetupController(MeetupManager meetupManager){
        this.meetupManager = meetupManager;
    }

    @Operation(summary = "모임 생성")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    @PostMapping
    public ResponseEntity<ResponseBody<MeetupResponse>> createMeetup(
            @RequestBody MeetupCreateRequest request,
            HttpServletRequest httpRequest
    ){
        Long hostMemberId = SessionUtil.getLoginMemberId(httpRequest);

        if(hostMemberId == null){
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }

        MeetupResponse response = meetupManager.createMeetup(hostMemberId,request);

        return ResponseEntity.ok(new ResponseBody<>(response));
    }
}
