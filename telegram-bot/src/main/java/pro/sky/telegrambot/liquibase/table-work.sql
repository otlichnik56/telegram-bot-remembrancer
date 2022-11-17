-- liquibase formatted sql

-- changeset nurkatovich:1
CREATE TABLE notification_task(
    id          SERIAL PRIMARY KEY,
    chat_id     INTEGER,
    message     TEXT,
    date_time   TIMESTAMP
);

-- SERIAL использовал, потому что не работает с другими. TIMESTAMP здесь потому что не работает DATETIME




