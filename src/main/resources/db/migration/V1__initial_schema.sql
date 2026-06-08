-- API Keys table
CREATE TABLE api_keys (
                          id          BIGSERIAL PRIMARY KEY,
                          key_hash    VARCHAR(64) UNIQUE NOT NULL,
                          key_prefix  VARCHAR(8)  NOT NULL,          -- e.g. "sk_abc123" shown to user
                          owner_name  VARCHAR(100) NOT NULL,
                          is_active   BOOLEAN DEFAULT TRUE,
                          created_at  TIMESTAMP DEFAULT NOW()
);

-- URLs table
CREATE TABLE urls (
                      id           BIGSERIAL PRIMARY KEY,
                      short_code   VARCHAR(10) UNIQUE NOT NULL,
                      original_url TEXT NOT NULL,
                      api_key_id   BIGINT REFERENCES api_keys(id) ON DELETE SET NULL,
                      custom_alias BOOLEAN DEFAULT FALSE,
                      created_at   TIMESTAMP DEFAULT NOW(),
                      expires_at   TIMESTAMP,
                      is_active    BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_urls_api_key_id ON urls(api_key_id);

-- Clicks table
CREATE TABLE clicks (
                        id          BIGSERIAL PRIMARY KEY,
                        short_code  VARCHAR(10) NOT NULL,
                        clicked_at  TIMESTAMP DEFAULT NOW(),
                        ip_address  VARCHAR(45),
                        device_type VARCHAR(20),   -- MOBILE, DESKTOP, TABLET, BOT, UNKNOWN
                        referrer    TEXT,
                        user_agent  TEXT
);

-- Critical index for analytics queries
CREATE INDEX idx_clicks_short_code_time ON clicks(short_code, clicked_at DESC);
CREATE INDEX idx_clicks_clicked_at      ON clicks(clicked_at DESC);