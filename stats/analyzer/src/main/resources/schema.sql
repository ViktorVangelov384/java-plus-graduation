CREATE SCHEMA IF NOT EXISTS stats_analyzer;

CREATE TABLE IF NOT EXISTS stats_analyzer.event (
    id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    event_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS stats_analyzer.user_action (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    action_timestamp TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS stats_analyzer.event_similarity (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_event_similarity_pair
ON stats_analyzer.event_similarity (event_a, event_b);

CREATE INDEX IF NOT EXISTS idx_user_action_user_event
ON stats_analyzer.user_action (user_id, event_id);