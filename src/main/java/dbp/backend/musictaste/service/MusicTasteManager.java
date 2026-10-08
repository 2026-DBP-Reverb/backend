package dbp.backend.musictaste.service;

import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.musictaste.dao.MusicTasteDao;
import dbp.backend.musictaste.dto.request.CompleteMusicTasteRequest;
import dbp.backend.musictaste.dto.response.*;
import dbp.backend.musictaste.model.MusicArtist;
import dbp.backend.musictaste.model.MusicTrack;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicTasteManager {
    private final SpotifyManager spotifyManager;
    private final MusicTasteDao musicTasteDao;

    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 10;

    public MusicTasteManager(
            SpotifyManager spotifyManager,
            MusicTasteDao musicTasteDao
    ) {
        this.spotifyManager = spotifyManager;
        this.musicTasteDao = musicTasteDao;
    }

    /**
     * 음악 취향 등록 (수정)
     */
    public void completeMusicTaste(
            Long memberId,
            CompleteMusicTasteRequest request
    ) {
        if (request == null) {
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        }
        musicTasteDao.replaceMusicTastes(
                memberId,
                request.artists().stream()
                        .map(artist -> new MusicArtist(
                                artist.spotifyArtistId(),
                                artist.artistName(),
                                artist.imageUrl()
                        ))
                        .toList(),
                request.genres().stream()
                        .map(genre -> genre.genreId())
                        .toList(),
                request.tracks().stream()
                        .map(track -> new MusicTrack(
                                track.spotifyTrackId(),
                                track.trackName(),
                                track.artistName(),
                                track.imageUrl()
                        ))
                        .toList()
        );
    }

    /**
     * 아티스트 검색 (SpotifyManager에 위임)
     */
    public List<ArtistResponse> searchArtists(
            String query,
            Integer limit
    ) {
        validateQuery(query);
        return spotifyManager.searchArtists(query, normalizeLimit(limit))
                .stream()
                .map(ArtistResponse::from)
                .toList();
    }

    /**
     * 곡 검색 (SpotifyManager에 위임)
     */
    public List<TrackResponse> searchTracks(
            String query,
            Integer limit
    ) {
        validateQuery(query);
        return spotifyManager.searchTracks(query, normalizeLimit(limit))
                .stream()
                .map(TrackResponse::from)
                .toList();
    }

    /**
     * 사용자의 아티스트 취향 조회
     */
    public List<ArtistResponse> getArtistTastes(Long memberId) {
        return musicTasteDao.getArtistTastes(memberId)
                .stream()
                .map(ArtistResponse::from)
                .toList();
    }

    /**
     * 사용자의 플레이리스트 취향 조회
     */
    public List<TrackResponse> getTrackTastes(Long memberId) {
        return musicTasteDao.getTrackTastes(memberId)
                .stream()
                .map(TrackResponse::from)
                .toList();
    }

    /**
     * 사용자의 장르 취향 조회
     */
    public List<GenreResponse> getGenres(Long memberId) {
        return musicTasteDao.getGenres(memberId)
                .stream()
                .map(GenreResponse::from)
                .toList();
    }

    // 비어있는 검색어 검증
    private void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new ApiException(CommonErrorCode.BAD_REQUEST);
        }
    }

    // 검색결과 개수 제한 검증
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_SEARCH_LIMIT;
        }
        if (limit > MAX_SEARCH_LIMIT) {
            return MAX_SEARCH_LIMIT;
        }
        return limit;
    }
}
