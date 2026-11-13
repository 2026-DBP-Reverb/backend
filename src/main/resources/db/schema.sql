CREATE TABLE member
(
    member_id         NUMBER PRIMARY KEY,
    email             VARCHAR2(255)                  NOT NULL UNIQUE,
    password_hash     VARCHAR2(255)                  NOT NULL,
    profile_image_url VARCHAR2(500),
    instagram_id      VARCHAR2(100),
    nickname          VARCHAR2(100),
    created_at        TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE member_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE genre
(
    genre_id   NUMBER PRIMARY KEY,
    genre_name VARCHAR2(30) NOT NULL UNIQUE
);

-- 이미 장르가 존재하면 insert하지 않기
MERGE INTO genre g
USING (SELECT 1 AS genre_id, 'KPOP' AS genre_name
       FROM dual
       UNION ALL
       SELECT 2, 'JPOP'
       FROM dual
       UNION ALL
       SELECT 3, 'INDIE'
       FROM dual
       UNION ALL
       SELECT 4, 'CLASSIC'
       FROM dual
       UNION ALL
       SELECT 5, 'POP'
       FROM dual
       UNION ALL
       SELECT 6, 'HIPHOP'
       FROM dual
       UNION ALL
       SELECT 7, 'R&B'
       FROM dual
       UNION ALL
       SELECT 8, 'JAZZ'
       FROM dual
       UNION ALL
       SELECT 9, 'ROCK'
       FROM dual
       UNION ALL
       SELECT 10, 'EDM'
       FROM dual) seed
ON (g.genre_id = seed.genre_id)
WHEN NOT MATCHED THEN
    INSERT (genre_id, genre_name)
    VALUES (seed.genre_id, seed.genre_name);
COMMIT;

CREATE TABLE music_artist
(
    spotify_artist_id VARCHAR2(100) PRIMARY KEY,
    artist_name       VARCHAR2(255) NOT NULL,
    image_url         VARCHAR2(500)
);

CREATE TABLE music_track
(
    spotify_track_id VARCHAR2(100) PRIMARY KEY,
    track_name       VARCHAR2(255) NOT NULL,
    artist_name      VARCHAR2(255) NOT NULL,
    image_url        VARCHAR2(500)
);

CREATE TABLE member_artist_taste
(
    member_id         NUMBER        NOT NULL,
    spotify_artist_id VARCHAR2(100) NOT NULL,

    PRIMARY KEY (member_id, spotify_artist_id),
    FOREIGN KEY (member_id) REFERENCES member (member_id),
    FOREIGN KEY (spotify_artist_id) REFERENCES music_artist (spotify_artist_id)
);

CREATE TABLE member_track_taste
(
    member_id        NUMBER        NOT NULL,
    spotify_track_id VARCHAR2(100) NOT NULL,

    PRIMARY KEY (member_id, spotify_track_id),
    FOREIGN KEY (member_id) REFERENCES member (member_id),
    FOREIGN KEY (spotify_track_id) REFERENCES music_track (spotify_track_id)
);

CREATE TABLE member_genre_taste
(
    member_id NUMBER NOT NULL,
    genre_id  NUMBER NOT NULL,

    PRIMARY KEY (member_id, genre_id),
    FOREIGN KEY (member_id) REFERENCES member (member_id),
    FOREIGN KEY (genre_id) REFERENCES genre (genre_id)
);

CREATE SEQUENCE match_req_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE mate_match_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE mate_match_request
(
    request_id   NUMBER PRIMARY KEY,
    requester_id NUMBER                            NOT NULL,
    recipient_id NUMBER                            NOT NULL,
    status       VARCHAR2(10) DEFAULT 'PENDING'    NOT NULL,
    requested_at TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    responded_at TIMESTAMP,

    CONSTRAINT mate_match_request_status_ck
        CHECK ( status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT mate_match_request_ck CHECK (requester_id <> recipient_id),
    CONSTRAINT mate_match_request_un UNIQUE (request_id, requester_id, recipient_id),
    FOREIGN KEY (requester_id) REFERENCES member (member_id),
    FOREIGN KEY (recipient_id) REFERENCES member (member_id)
);

CREATE TABLE mate_match
(
    match_id    NUMBER PRIMARY KEY,
    request_id  NUMBER                         NOT NULL,
    member_a_id NUMBER                         NOT NULL,
    member_b_id NUMBER                         NOT NULL,
    matched_at  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    ended_at    TIMESTAMP,

    CONSTRAINT mate_match_un UNIQUE (request_id, member_a_id, member_b_id),
    FOREIGN KEY (request_id, member_a_id, member_b_id)
        REFERENCES mate_match_request (request_id, requester_id, recipient_id)
);

CREATE SEQUENCE meetup_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE meetup
(
    meetup_id      NUMBER                                              NOT NULL,
    host_member_id NUMBER                                              NOT NULL,
    title          VARCHAR2(200)                                       NOT NULL,
    description    CLOB,
    image_url      VARCHAR2(500),
    category       VARCHAR2(20)                   DEFAULT 'OTHER'      NOT NULL,
    place          VARCHAR2(255)                                       NOT NULL,
    capacity       NUMBER(5)                      DEFAULT 10           NOT NULL,
    starts_at      DATE                                                NOT NULL,
    ends_at        DATE,
    status         VARCHAR2(12)                   DEFAULT 'OPEN'       NOT NULL,
    created_at     TIMESTAMP WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,

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
            ),

    CONSTRAINT meetup_time_ck
        CHECK (
            ends_at IS NULL
                OR ends_at > starts_at
            )
);

CREATE INDEX meetup_host_idx ON meetup (host_member_id);

CREATE TABLE meetup_participant
(
    meetup_id NUMBER                                              NOT NULL,
    member_id NUMBER                                              NOT NULL,
    status    VARCHAR2(8)                    DEFAULT 'ACTIVE'     NOT NULL,
    joined_at TIMESTAMP WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
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

CREATE TABLE email_verification
(
    email           VARCHAR2(255) PRIMARY KEY,
    code_mac        VARCHAR2(64)                NOT NULL,
    expires_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    resend_after    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    failed_attempts NUMBER(1) DEFAULT 0         NOT NULL
        CHECK (failed_attempts BETWEEN 0 AND 5),
    used_at         TIMESTAMP(6) WITH TIME ZONE
);

CREATE SEQUENCE settlement_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE settlement
(
    settlement_id     NUMBER                         DEFAULT settlement_seq.nextval PRIMARY KEY,
    meetup_id         NUMBER                                              NOT NULL,
    paid_by_member_id NUMBER                                              NOT NULL,
    title             VARCHAR2(255)                                       NOT NULL,
    total_amount      NUMBER(12)                                          NOT NULL
        CHECK ( total_amount > 0 ),
    account           VARCHAR2(127)                                       NOT NULL,
    split_method      VARCHAR2(8)                    DEFAULT 'EQUAL'      NOT NULL
        CHECK ( split_method IN ('EQUAL', 'WEIGHTED') ),
    spent_at          DATE                                                NOT NULL,
    created_at        TIMESTAMP WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE INDEX settlement_payer_idx ON settlement (meetup_id, paid_by_member_id);

ALTER TABLE settlement
    ADD CONSTRAINT settlement_un UNIQUE (settlement_id, meetup_id);

ALTER TABLE settlement
    ADD CONSTRAINT settlement_participant_fk
        FOREIGN KEY (meetup_id, paid_by_member_id)
            REFERENCES meetup_participant (meetup_id, member_id);


CREATE TABLE settlement_share
(
    settlement_id NUMBER                                              NOT NULL,
    member_id     NUMBER                                              NOT NULL,
    meetup_id     NUMBER                                              NOT NULL,
    amount        NUMBER(12)                                          NOT NULL
        CHECK ( amount >= 0 ),
    status        VARCHAR2(10)                   DEFAULT 'UNPAID'     NOT NULL
        CHECK ( status IN ('UNPAID', 'REQUESTED', 'CONFIRMED') ),
    created_at    TIMESTAMP WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    confirmed_at  TIMESTAMP WITH LOCAL TIME ZONE
);

ALTER TABLE settlement_share
    ADD CONSTRAINT settlement_share_pk PRIMARY KEY (settlement_id, member_id);

ALTER TABLE settlement_share
    ADD CONSTRAINT settlement_share_settlement_fk
        FOREIGN KEY (settlement_id, meetup_id)
            REFERENCES settlement (settlement_id, meetup_id);

ALTER TABLE settlement_share
    ADD CONSTRAINT settlement_share_participant_fk
        FOREIGN KEY (meetup_id, member_id)
            REFERENCES meetup_participant (meetup_id, member_id);

ALTER TABLE settlement_share
    ADD CONSTRAINT settlement_share_state_ck
        CHECK (
            (status = 'UNPAID' AND confirmed_at IS NULL)
                OR
            (status = 'REQUESTED' AND confirmed_at IS NULL)
                OR
            (status = 'CONFIRMED' AND confirmed_at IS NOT NULL));

CREATE INDEX settlement_share_idx ON settlement_share (settlement_id, meetup_id);

CREATE INDEX settlement_share_participant_idx ON settlement_share (meetup_id, member_id);
