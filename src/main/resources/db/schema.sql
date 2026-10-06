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