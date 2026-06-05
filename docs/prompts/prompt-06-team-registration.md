# Prompt 06 — Team Registration

## Summary
Implement team registration after trainers. Teams are managed by club admins and connect an age group plus team category to a trainer. Player roster assignment is intentionally deferred to a later milestone.

## Backend Changes
- Add Flyway migration `V7__team.sql`.
- Add `Team` entity with UUID public identifier, `ageGroup`, `teamCategory`, `trainer`, and `active`.
- Add repository, DTOs, MapStruct mapper, service, and controller.
- Keep services returning domain entities and controllers mapping to response DTOs.
- Protect `/api/v1/teams` endpoints with `ADMIN`.
- Expose create, list/search, view, update, deactivate, and reactivate endpoints.
- Return UUID-only responses, including `trainerUuid` and `trainerName`; never expose internal IDs.
- Fetch trainer relationships for team reads so controller mapping does not trip lazy-loading errors.

## Frontend Changes
- Add `teams` API client.
- Add team form, list page, and detail/edit page.
- Add `/teams` and `/teams/:uuid` protected routes.
- Add Teams links to the navbar and dashboard.
- Require a trainer selection when creating or updating a team.

## Verification
- `mvn clean test`
- `npm.cmd run build`
- `npm.cmd run test:e2e:list`
- `npm.cmd run test:e2e`

## Deferred
- Player/team roster assignment.
- Evaluation creation and evaluation players.

