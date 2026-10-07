package dbp.backend.musictaste.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.musictaste.model.Genre;
import dbp.backend.musictaste.model.MusicArtist;
import dbp.backend.musictaste.model.MusicTrack;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MusicTasteDao {

    // 사용자의 아티스트 취향 조회
    public List<MusicArtist> getArtistTastes(Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT
                                ma.spotify_artist_id,
                                ma.artist_name,
                                ma.image_url
                            FROM member_artist_taste mat
                            JOIN music_artist ma
                                ON mat.spotify_artist_id =
                                   ma.spotify_artist_id
                            WHERE mat.member_id = ?
                            ORDER BY ma.artist_name
                            """,
                    rs -> {
                        List<MusicArtist> artists = new ArrayList<>();
                        while (rs.next()) {
                            artists.add(new MusicArtist(
                                    rs.getString("spotify_artist_id"),
                                    rs.getString("artist_name"),
                                    rs.getString("image_url")
                            ));
                        }
                        return artists;
                    },
                    memberId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 사용자의 플레이리스트 취향 조회
    public List<MusicTrack> getTrackTastes(Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT
                                mt.spotify_track_id,
                                mt.track_name,
                                mt.artist_name,
                                mt.image_url
                            FROM member_track_taste mtt
                            JOIN music_track mt
                                ON mtt.spotify_track_id =
                                   mt.spotify_track_id
                            WHERE mtt.member_id = ?
                            ORDER BY mt.track_name
                            """,
                    rs -> {
                        List<MusicTrack> tracks = new ArrayList<>();
                        while (rs.next()) {
                            tracks.add(new MusicTrack(
                                    rs.getString("spotify_track_id"),
                                    rs.getString("track_name"),
                                    rs.getString("artist_name"),
                                    rs.getString("image_url")
                            ));
                        }
                        return tracks;
                    },
                    memberId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 사용자의 장르 취향 조회
    public List<Genre> getGenres(Long memberId) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT
                                g.genre_id,
                                g.genre_name,
                                CASE
                                    WHEN mgt.member_id IS NULL THEN 0
                                    ELSE 1
                                END AS selected
                            FROM genre g
                            LEFT JOIN member_genre_taste mgt
                                ON g.genre_id = mgt.genre_id
                                AND mgt.member_id = ?
                            ORDER BY g.genre_id
                            """,
                    rs -> {
                        List<Genre> genres = new ArrayList<>();
                        while (rs.next()) {
                            genres.add(new Genre(
                                    rs.getLong("genre_id"),
                                    rs.getString("genre_name"),
                                    rs.getInt("selected") == 1
                            ));
                        }
                        return genres;
                    },
                    memberId
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 사용자의 음악 취향 갱신
    public void replaceMusicTastes(
            Long memberId,
            List<MusicArtist> artists,
            List<Long> genreIds,
            List<MusicTrack> tracks
    ) {
        try (Connection conn = ConnectionManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                deleteArtistTastes(conn, memberId);
                deleteGenreTastes(conn, memberId);
                deleteTrackTastes(conn, memberId);

                for (MusicArtist artist : artists) {
                    saveMusicArtistIfAbsent(conn, artist);
                    saveArtistTaste(
                            conn,
                            memberId,
                            artist.getSpotifyArtistId()
                    );
                }

                for (MusicTrack track : tracks) {
                    saveMusicTrackIfAbsent(conn, track);
                    saveTrackTaste(
                            conn,
                            memberId,
                            track.getSpotifyTrackId()
                    );
                }

                for (Long genreId : genreIds) {
                    saveGenreTaste(conn, memberId, genreId);
                }

                conn.commit();

            } catch (SQLException e) {
                JDBCUtil.rollbackQuietly(conn);
                throw new DatabaseException(e);

            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // 사용자의 기존 아티스트 취향 삭제
    private void deleteArtistTastes(
            Connection conn,
            Long memberId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        DELETE FROM member_artist_taste
                        WHERE member_id = ?
                        """,
                memberId
        );
    }

    // 사용자의 아티스트 취향 저장
    private void saveArtistTaste(
            Connection conn,
            Long memberId,
            String spotifyArtistId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        INSERT INTO member_artist_taste (
                            member_id,
                            spotify_artist_id
                        ) VALUES (?, ?)
                        """,
                memberId,
                spotifyArtistId
        );
    }

    // 사용자의 기존 플레이리스트 취향 삭제
    private void deleteTrackTastes(
            Connection conn,
            Long memberId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        DELETE FROM member_track_taste
                        WHERE member_id = ?
                        """,
                memberId
        );
    }

    // 사용자의 플레이리스트 취향 저장
    private void saveTrackTaste(
            Connection conn,
            Long memberId,
            String spotifyTrackId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        INSERT INTO member_track_taste (
                            member_id,
                            spotify_track_id
                        ) VALUES (?, ?)
                        """,
                memberId,
                spotifyTrackId
        );
    }

    // 사용자의 기존 장르 취향 삭제
    private void deleteGenreTastes(
            Connection conn,
            Long memberId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        DELETE FROM member_genre_taste
                        WHERE member_id = ?
                        """,
                memberId
        );
    }

    // 사용자의 장르 취향 저장
    private void saveGenreTaste(
            Connection conn,
            Long memberId,
            Long genreId
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        INSERT INTO member_genre_taste (
                            member_id,
                            genre_id
                        ) VALUES (?, ?)
                        """,
                memberId,
                genreId
        );
    }

    // 아티스트 테이블에 아티스트 정보 저장 (아직 테이블에 정보가 없는 경우)
    private void saveMusicArtistIfAbsent(
            Connection conn,
            MusicArtist artist
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        MERGE INTO music_artist target
                        USING (
                            SELECT
                                ? AS spotify_artist_id,
                                ? AS artist_name,
                                ? AS image_url
                            FROM dual
                        ) source
                        ON (
                            target.spotify_artist_id =
                            source.spotify_artist_id
                        )
                        WHEN NOT MATCHED THEN
                            INSERT (
                                spotify_artist_id,
                                artist_name,
                                image_url
                            )
                            VALUES (
                                source.spotify_artist_id,
                                source.artist_name,
                                source.image_url
                            )
                        """,
                artist.getSpotifyArtistId(),
                artist.getArtistName(),
                artist.getImageUrl()
        );
    }

    // 곡 테이블에 곡 정보 저장 (아직 테이블에 정보가 없는 경우)
    private void saveMusicTrackIfAbsent(
            Connection conn,
            MusicTrack track
    ) throws SQLException {
        JDBCUtil.executeUpdate(
                conn,
                """
                        MERGE INTO music_track target
                        USING (
                            SELECT
                                ? AS spotify_track_id,
                                ? AS track_name,
                                ? AS artist_name,
                                ? AS image_url
                            FROM dual
                        ) source
                        ON (
                            target.spotify_track_id =
                            source.spotify_track_id
                        )
                        WHEN NOT MATCHED THEN
                            INSERT (
                                spotify_track_id,
                                track_name,
                                artist_name,
                                image_url
                            )
                            VALUES (
                                source.spotify_track_id,
                                source.track_name,
                                source.artist_name,
                                source.image_url
                            )
                        """,
                track.getSpotifyTrackId(),
                track.getTrackName(),
                track.getArtistName(),
                track.getImageUrl()
        );
    }
}
