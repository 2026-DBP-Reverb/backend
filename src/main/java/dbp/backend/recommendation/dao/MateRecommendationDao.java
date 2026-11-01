package dbp.backend.recommendation.dao;

import dbp.backend.common.dao.ConnectionManager;
import dbp.backend.common.dao.JDBCUtil;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.recommendation.model.CommonTag;
import dbp.backend.recommendation.model.MateCandidate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MateRecommendationDao {

    public List<MateCandidate> findTopCandidates(Long memberId, int limit) {
        try(Connection conn = ConnectionManager.getConnection()) {
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            WITH tags AS (
                            SELECT member_id, 'A:' || spotify_artist_id AS tag FROM member_artist_taste
                            UNION ALL
                            SELECT member_id, 'G:' || TO_CHAR(genre_id) FROM member_genre_taste
                            UNION ALL
                            SELECT member_id, 'T:' || spotify_track_id FROM member_track_taste
                        ),
                        cnt AS (
                            SELECT member_id, COUNT(*) AS n
                            FROM tags
                            GROUP BY member_id
                        ),
                        common AS (
                            SELECT o.member_id AS other_id, COUNT(*) AS inter
                            FROM tags me
                            JOIN tags o
                                ON o.tag = me.tag
                               AND o.member_id <> me.member_id
                            WHERE me.member_id = ?
                            GROUP BY o.member_id
                        )
                        SELECT
                            m.member_id,
                            m.nickname,
                            m.profile_image_url,
                            ROUND(c.inter / (mc.n + oc.n - c.inter) * 100) AS match_rate
                        FROM common c
                        JOIN cnt oc ON oc.member_id = c.other_id
                        JOIN cnt mc ON mc.member_id = ?
                        JOIN member m ON m.member_id = c.other_id
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM mate_match mm
                            WHERE mm.ended_at IS NULL
                              AND (
                                   (mm.member_a_id = ? AND mm.member_b_id = c.other_id)
                                OR (mm.member_b_id = ? AND mm.member_a_id = c.other_id)
                              )
                        )
                        ORDER BY match_rate DESC
                        FETCH FIRST ? ROWS ONLY
                        """,
                        rs -> {
                        List<MateCandidate> candidates = new ArrayList<>();
                        while (rs.next()) {
                            candidates.add(new MateCandidate(
                                    rs.getLong("member_id"),
                                    rs.getString("nickname"),
                                    rs.getString("profile_image_url"),
                                    rs.getInt("match_rate")
                            ));
                        }
                        return candidates;
                        },
                    memberId,
                    memberId,
                    memberId,
                    memberId,
                    limit
            );
        } catch (SQLException e){
            throw new DatabaseException(e);
        }
    }

    public List<CommonTag> findCommonTags(Long memberId){
        try(Connection conn = ConnectionManager.getConnection()){
            return JDBCUtil.executeQuery(
                    conn,
                    """
                            SELECT o.member_id, 'ARTIST' AS tag_type, ma.artist_name AS name
                            FROM member_artist_taste me
                            JOIN member_artist_taste o
                                ON o.spotify_artist_id = me.spotify_artist_id
                               AND o.member_id <> me.member_id
                            JOIN music_artist ma
                                ON ma.spotify_artist_id = me.spotify_artist_id
                            WHERE me.member_id = ?
                            UNION ALL
                            SELECT o.member_id, 'GENRE', g.genre_name
                            FROM member_genre_taste me
                            JOIN member_genre_taste o
                                ON o.genre_id = me.genre_id
                               AND o.member_id <> me.member_id
                            JOIN genre g
                                ON g.genre_id = me.genre_id
                            WHERE me.member_id = ?
                            UNION ALL
                            SELECT o.member_id, 'TRACK', mt.track_name
                            FROM member_track_taste me
                            JOIN member_track_taste o
                                ON o.spotify_track_id = me.spotify_track_id
                               AND o.member_id <> me.member_id
                            JOIN music_track mt
                                ON mt.spotify_track_id = me.spotify_track_id
                            WHERE me.member_id = ?
                            """,
                    rs -> {
                        List<CommonTag> commonTags = new ArrayList<>();
                        while(rs.next()){
                            commonTags.add(new CommonTag(
                                    rs.getLong("member_id"),
                                    rs.getString("tag_type"),
                                    rs.getString("name")
                            ));
                        }
                        return commonTags;
                    },
                    memberId,
                    memberId,
                    memberId
            );
        } catch (SQLException e){
            throw new DatabaseException(e);
        }
    }
}
