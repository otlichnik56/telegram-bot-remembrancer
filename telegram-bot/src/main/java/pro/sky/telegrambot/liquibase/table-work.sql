-- liquibase formatted sql

-- changeset nurkatovich:1
CREATE TABLE notification_task(
    id         SERIAL PRIMARY KEY,
    chat_id     INTEGER,
    message    TEXT,
    date_time   TIMESTAMP
);



