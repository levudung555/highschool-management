CREATE TABLE IF NOT EXISTS invalidated_tokens (
    id VARCHAR(64) PRIMARY KEY,
    expire_time DATETIME(3) NOT NULL
);
