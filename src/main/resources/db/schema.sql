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

CREATE SEQUENCE match_req_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE mate_match_seq START WITH 1 INCREMENT BY  1;

CREATE TABLE mate_match_request(
    request_id      NUMBER      PRIMARY KEY,
    requester_id    NUMBER      NOT NULL,
    recipient_id    NUMBER      NOT NULL,
    status          VARCHAR2(10)    DEFAULT 'PENDING'   NOT NULL,
    requested_at        TIMESTAMP       DEFAULT systimestamp NOT NULL,
    responded_at        TIMESTAMP,

    CONSTRAINT mate_match_request_status_ck
        CHECK ( status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT mate_match_request_ck CHECK (requester_id <> recipient_id),
    CONSTRAINT mate_match_request_un UNIQUE(request_id, requester_id, recipient_id),
    FOREIGN KEY (requester_id)  REFERENCES  member(member_id),
    FOREIGN KEY (recipient_id)  REFERENCES  member(member_id)
);

CREATE TABLE mate_match (
    match_id    NUMBER  PRIMARY KEY,
    request_id  NUMBER  NOT NULL,
    member_a_id NUMBER  NOT NULL,
    member_b_id NUMBER  NOT NULL,
    matched_at  TIMESTAMP   DEFAULT systimestamp NOT NULL,
    ended_at    TIMESTAMP,

    CONSTRAINT mate_match_un UNIQUE (request_id, member_a_id, member_b_id),
    FOREIGN KEY (request_id, member_a_id, member_b_id)
        REFERENCES mate_match_request(request_id, requester_id, recipient_id)
);

CREATE SEQUENCE meetup_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE meetup (
    meetup_id      NUMBER NOT NULL,
    host_member_id NUMBER NOT NULL,
    title          VARCHAR2(200)   NOT NULL,
    description    CLOB,
    image_url      VARCHAR2(500),
    category       VARCHAR2(20)    DEFAULT 'OTHER' NOT NULL,
    place          VARCHAR2(255)   NOT NULL,
    capacity       NUMBER(5)       DEFAULT 10 NOT NULL,
    starts_at      DATE   NOT NULL,
    status         VARCHAR2(12)    DEFAULT 'OPEN' NOT NULL,
    created_at     TIMESTAMP WITH LOCAL TIME ZONE DEFAULT systimestamp NOT NULL,

    CONSTRAINT meetup_pk PRIMARY KEY (meetup_id),

    CONSTRAINT meetup_host_member_fk FOREIGN KEY (host_member_id)
        REFERENCES member (member_id),

    CONSTRAINT meetup_category_ck
        CHECK (
            category IN (
                         'CONCERT',
                         'FESTIVAL',
                         'LISTENING',
                         'OTHER'
                )
            ),

    CONSTRAINT meetup_capacity_ck CHECK (capacity >= 1),

    CONSTRAINT meetup_status_ck
        CHECK (
            status IN (
                       'OPEN',
                       'CLOSED',
                       'COMPLETED',
                       'CANCELLED'
                )
            )
);

CREATE INDEX meetup_host_idx ON meetup (host_member_id);

CREATE TABLE meetup_participant
(
    meetup_id NUMBER NOT NULL,
    member_id NUMBER NOT NULL,
    status    VARCHAR2(8) DEFAULT 'ACTIVE' NOT NULL,
    joined_at TIMESTAMP WITH LOCAL TIME ZONE DEFAULT systimestamp NOT NULL,
    left_at   TIMESTAMP WITH LOCAL TIME ZONE,

    CONSTRAINT meetup_participant_pk PRIMARY KEY (meetup_id, member_id),

    CONSTRAINT meetup_participant_meetup_fk FOREIGN KEY (meetup_id)
            REFERENCES meetup (meetup_id),

    CONSTRAINT meetup_participant_member_fk FOREIGN KEY (member_id)
            REFERENCES member (member_id),

    CONSTRAINT meetup_participant_status_ck
        CHECK (
            status IN (
                       'ACTIVE',
                       'LEFT'
                )
            ),

    CONSTRAINT meetup_participant_time_ck
        CHECK (
            left_at IS NULL
                OR left_at >= joined_at
            )
);

CREATE INDEX meetup_participant_member_idx ON meetup_participant (member_id, meetup_id);

CREATE TABLE EMAIL_VERIFICATION (
    EMAIL           VARCHAR2(255) PRIMARY KEY,
    CODE_MAC        VARCHAR2(64) NOT NULL,
    EXPIRES_AT      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    RESEND_AFTER    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    FAILED_ATTEMPTS NUMBER(1) DEFAULT 0 NOT NULL
        CHECK       (FAILED_ATTEMPTS BETWEEN 0 AND 5),
    USED_AT         TIMESTAMP(6) WITH TIME ZONE
);
