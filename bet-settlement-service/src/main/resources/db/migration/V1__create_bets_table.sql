CREATE TABLE "bets"
(
    id              UUID DEFAULT random_uuid() PRIMARY KEY,
    user_id         UUID                        NOT NULL,
    event_id        UUID                        NOT NULL,
    event_market_id UUID                        NOT NULL,
    event_winner_id UUID                        NOT NULL,
    bet_amount      NUMERIC(19, 2)              NOT NULL,
    status          VARCHAR(20) DEFAULT 'PENDING' NOT NULL
);

CREATE INDEX idx_bets_event_id_status ON "bets" (event_id, status);
