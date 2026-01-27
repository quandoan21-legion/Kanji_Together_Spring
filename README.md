# Kanji_Together_Spring

## User Attempts + Kanji Mastery APIs

### POST /api/attempts
Request:
```json
{
  "userId": 1,
  "questionId": 55,
  "correct": true,
  "selectedAnswer": "A",
  "timeSpentMs": 1200,
  "answeredAt": "2026-01-27T10:30:00"
}
```

Response:
```json
{
  "questionAttemptId": 999,
  "updatedKanji": [
    {
      "kanjiId": 1,
      "masteryLevel": 3,
      "easeFactor": 2.7,
      "intervalDays": 3,
      "repetitions": 2,
      "nextReviewAt": "2026-01-30T10:30:00"
    }
  ]
}
```

### POST /api/attempts/batch
Request:
```json
{
  "attempts": [
    {
      "userId": 1,
      "questionId": 55,
      "correct": true,
      "selectedAnswer": "A",
      "timeSpentMs": 1200
    },
    {
      "userId": 1,
      "questionId": 56,
      "correct": false,
      "selectedAnswer": "B",
      "timeSpentMs": 900
    }
  ]
}
```

Response: list of `AttemptResponse` objects.

### GET /api/users/{userId}/kanji/{kanjiId}/mastery
Returns the current mastery snapshot for a user/kanji.

### GET /api/review/due?userId=1&limit=20
Returns due kanji mastery rows ordered by `nextReviewAt`.

## Database migration
The SQL migration is in:
- `src/main/resources/db/migration/V1__add_user_attempts_mastery.sql`

If you use Flyway, keep the file in that folder. If Flyway is not configured, run this SQL manually against the database.
