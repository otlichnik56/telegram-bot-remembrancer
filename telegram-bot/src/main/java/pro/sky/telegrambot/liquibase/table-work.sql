-- liquibase formatted sql

-- changeset nurkatovich:1
CREATE TABLE notification_task(
    id          INTEGER PRIMARY KEY,
    chatId      TEXT,
    message     TEXT,
    dateTime    TIMESTAMP
);

<-- 123 -->


