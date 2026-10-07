package dbp.backend.musictaste.controller;

import dbp.backend.common.config.api.OpenApiConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.common.response.ResponseBody;
import dbp.backend.common.util.SessionUtil;
import dbp.backend.musictaste.dto.request.CompleteMusicTasteRequest;
import dbp.backend.musictaste.dto.response.*;
import dbp.backend.musictaste.service.MusicTasteManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Music Taste", description = "음악 취향 설정 API")
@RestController("/api/music-tastes")
public class MusicTasteController {
    private final MusicTasteManager musicTasteManager;

    public MusicTasteController(MusicTasteManager musicTasteManager) {
        this.musicTasteManager = musicTasteManager;
    }

    @Operation(summary = "이전에 선택한 아티스트 리스트 조회")
    @GetMapping("/artists")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    public ResponseEntity<ResponseBody<List<ArtistResponse>>> getSelectedArtists(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new ResponseBody<>(
                musicTasteManager.getArtistTastes(getMemberId(request))
        ));
    }

    @Operation(summary = "아티스트 검색")
    @GetMapping("/artists/search")
    public ResponseEntity<ResponseBody<List<ArtistResponse>>>
    searchArtists(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(new ResponseBody<>(
                musicTasteManager.searchArtists(query, limit)
        ));
    }

    @Operation(summary = "이전에 선택한 장르 리스트 조회")
    @GetMapping("/genres")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    public ResponseEntity<ResponseBody<List<GenreResponse>>> getSelectedGenres(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new ResponseBody<>(
                musicTasteManager.getGenres(getMemberId(request))
        ));
    }

    @Operation(summary = "이전에 선택한 플레이리스트 조회")
    @GetMapping("/tracks")
    @SecurityRequirement(
            name = OpenApiConfig.SESSION_COOKIE_SECURITY_SCHEME
    )
    public ResponseEntity<ResponseBody<List<TrackResponse>>> getSelectedTracks(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new ResponseBody<>(
                musicTasteManager.getTrackTastes(getMemberId(request))
        ));
    }

    @Operation(summary = "곡 검색")
    @GetMapping("/tracks/search")
    public ResponseEntity<ResponseBody<List<TrackResponse>>> searchTracks(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(new ResponseBody<>(
                musicTasteManager.searchTracks(query, limit)
        ));
    }

    @Operation(summary = "음악 취향 등록(수정)")
    @PutMapping
    public ResponseEntity<ResponseBody<Void>> completeMusicTaste(
            @RequestBody CompleteMusicTasteRequest request,
            HttpServletRequest httpRequest
    ) {
        musicTasteManager.completeMusicTaste(getMemberId(httpRequest), request);
        return ResponseEntity.ok(new ResponseBody<>(null));
    }

    private Long getMemberId(HttpServletRequest request) {
        Long memberId = SessionUtil.getLoginMemberId(request);
        if (memberId == null) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED);
        }
        return memberId;
    }
}
