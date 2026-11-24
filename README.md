# Bet Settlement

![Diagram](docs/diagram.png)

| Service | Does |
|---|---|
| `event-outcome-service` | Publishes event outcomes to Kafka |
| `bet-settlement-service` | Finds the bets of the event, sends them to RocketMQ, consumes RocketMQ and settles the bets |

## Run

Only Docker is required:

```bash
docker compose up --build -d --wait
```

| Service | Swagger UI | Health check |
|---|---|---|
| `event-outcome-service` | http://localhost:8080/api/v1/swagger | http://localhost:8080/api/v1/actuator/health |
| `bet-settlement-service` | http://localhost:8082/api/v1/swagger | http://localhost:8082/api/v1/actuator/health |

## Use

Demo bets are added on startup. Publish an event outcome:

```bash
curl -i -X POST http://localhost:8080/api/v1/event-outcomes \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "a1f0c3d2-0000-4000-8000-000000000001",
    "eventName": "Real Madrid vs Barcelona",
    "eventWinnerId": "b2e1d4c3-0000-4000-8000-000000000001"
  }'
```

Check that its bets are settled:

```bash
curl 'http://localhost:8082/api/v1/bets?eventId=a1f0c3d2-0000-4000-8000-000000000001'
```

## Design

- **Two services**: outcomes can come from different sources, so publishing is separate from the bets. Matching and settlement share the bets, so they are one service.
- **RocketMQ** carries one message per bet, so each bet is settled and retried independently.
- **Duplicates** are safe: only `PENDING` bets are sent and settled.
- **Failures**: if RocketMQ is not available, the outcome is retried until it is. Malformed outcomes are logged and skipped. Failed settlements are redelivered up to 5 times, then moved to the dead letter queue.

## Assumptions

- There are no odds in the task, so a bet becomes `WON` or `LOST` without a payout.
- An outcome applies to all markets of the event, because it has no market id.
- An event has one outcome. If two different winners are published, its bets can be settled with either.

## Limitations

- All pending bets of an event are loaded and sent one by one. A large event would need paging.
- H2 is in-memory, so there is one instance of `bet-settlement-service` and a restart resets the bets.

## Tests

Java 17+ and Docker are required:

```bash
./gradlew check
```
