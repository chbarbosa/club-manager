# Prompt 09: Evaluation Groups And Events

## Summary
Correct the evaluation model so an evaluation is not tied to a team. An evaluation represents a group of players with the same age group and team category, and it can contain one or more scheduled events.

## Domain Rules
- Evaluation grouping uses `ageGroup` and `teamCategory` (`MASCULINE` or `FEMININE`).
- Players can be assigned directly to an evaluation when their team category matches the evaluation.
- Each evaluation event has:
  - place;
  - event date;
  - start time;
  - duration of 60, 90, or 120 minutes.
- For each event, the club records whether each assigned player participated.
- Before an event can be completed, every active player assigned to the evaluation must have participation and skill level recorded.
- Skill levels are fixed for now: `DEBUTANT`, `ADVANCED`, `SKILLED`.

## API Additions
- `GET /api/v1/evaluations/{evaluationUuid}/players`
- `POST /api/v1/evaluations/{evaluationUuid}/players`
- `DELETE /api/v1/evaluations/{evaluationUuid}/players/{assignmentUuid}`
- `GET /api/v1/evaluations/{evaluationUuid}/events`
- `POST /api/v1/evaluations/{evaluationUuid}/events`
- `GET /api/v1/evaluation-events/{eventUuid}/attendance`
- `PUT /api/v1/evaluation-events/{eventUuid}/attendance/{playerUuid}`
- `PATCH /api/v1/evaluation-events/{eventUuid}/complete`
- `PATCH /api/v1/evaluation-events/{eventUuid}/cancel`

## Notes
- Evaluation-level status remains available for the broader cycle.
- Event completion is independent and enforces the attendance/skill rule.
