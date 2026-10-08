CREATE TABLE member (
    member_id           NUMBER          PRIMARY KEY,
    email               VARCHAR2(255)   NOT NULL UNIQUE,
    password_hash       VARCHAR2(255)   NOT NULL,
    profile_image_url   VARCHAR2(500),
    instagram_id        VARCHAR2(100),
    nickname            VARCHAR2(100),
    created_at          TIMESTAMP       DEFAULT systimestamp NOT NULL
);

CREATE SEQUENCE member_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE genre (
   genre_id    NUMBER        PRIMARY KEY,
   genre_name  VARCHAR2(30)  NOT NULL UNIQUE
);

-- 이미 장르가 존재하면 insert하지 않기
MERGE INTO genre g
    USING (
        SELECT 1 AS genre_id, 'KPOP' AS genre_name FROM dual
        UNION ALL SELECT 2, 'JPOP' FROM dual
        UNION ALL SELECT 3, 'INDIE' FROM dual
        UNION ALL SELECT 4, 'CLASSIC' FROM dual
        UNION ALL SELECT 5, 'POP' FROM dual
        UNION ALL SELECT 6, 'HIPHOP' FROM dual
        UNION ALL SELECT 7, 'R&B' FROM dual
        UNION ALL SELECT 8, 'JAZZ' FROM dual
        UNION ALL SELECT 9, 'ROCK' FROM dual
        UNION ALL SELECT 10, 'EDM' FROM dual
    ) seed
    ON (g.genre_id = seed.genre_id)
    WHEN NOT MATCHED THEN
        INSERT (genre_id, genre_name)
            VALUES (seed.genre_id, seed.genre_name);
COMMIT;

CREATE TABLE music_artist (
    spotify_artist_id  VARCHAR2(100) PRIMARY KEY,
    artist_name        VARCHAR2(255) NOT NULL,
    image_url          VARCHAR2(500)
);

CREATE TABLE music_track (
    spotify_track_id   VARCHAR2(100) PRIMARY KEY,
    track_name         VARCHAR2(255) NOT NULL,
    artist_name        VARCHAR2(255) NOT NULL,
    image_url          VARCHAR2(500)
);

CREATE TABLE member_artist_taste (
    member_id           NUMBER        NOT NULL,
    spotify_artist_id   VARCHAR2(100) NOT NULL,

    PRIMARY KEY (member_id, spotify_artist_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (spotify_artist_id) REFERENCES music_artist(spotify_artist_id)
);

CREATE TABLE member_track_taste (
    member_id          NUMBER        NOT NULL,
    spotify_track_id   VARCHAR2(100) NOT NULL,

    PRIMARY KEY (member_id, spotify_track_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (spotify_track_id) REFERENCES music_track(spotify_track_id)
);

CREATE TABLE member_genre_taste (
    member_id  NUMBER NOT NULL,
    genre_id   NUMBER NOT NULL,

    PRIMARY KEY (member_id, genre_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);