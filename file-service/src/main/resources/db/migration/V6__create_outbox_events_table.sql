CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_key VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);
CREATE INDEX idx_outbox_events_unpublished ON outbox_events(created_at) WHERE published = false;