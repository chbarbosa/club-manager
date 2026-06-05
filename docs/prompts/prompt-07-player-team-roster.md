# Prompt 07 — Player Team Roster

## Summary
Implement current team roster management. Club admins can assign active players to active teams, list the current roster, and remove players from a team without deleting assignment history.

## Backend Changes
- Add Flyway migration `V8__player_team.sql`.
- Add `PlayerTeam` entity with UUID public identifier, player, team, assigned date, removed date, and active flag.
- Add repository, DTOs, MapStruct mapper, service, and controller.
- Expose admin-only endpoints:
  - `GET /api/v1/teams/{teamUuid}/players`
  - `POST /api/v1/teams/{teamUuid}/players`
  - `DELETE /api/v1/teams/{teamUuid}/players/{assignmentUuid}`
- Validate assignment rules:
  - team must be active;
  - player must be active;
  - player team category must match the team category;
  - player cannot already be assigned to the same team;
  - player cannot already be assigned to another active team.
- Remove players by marking assignments inactive and setting `removedDate`.
- Keep API responses UUID-only; never expose internal database IDs.

## Frontend Changes
- Extend the teams API client with roster calls.
- Add roster management to the team detail page.
- Load available active players and prevent assigning players already in the current roster.
- Add assign/remove feedback and error messages.
- Extend the Teams Playwright flow to cover roster assignment and removal.

## Verification
- `mvn clean test` with a fresh in-memory H2 datasource.
- `npm.cmd run build`
- `npm.cmd run test:e2e:list`
- `npm.cmd run test:e2e`

## Notes
- The local file-mode H2 database may need to be reset if an interrupted migration corrupts the file.
- Historical roster views are deferred; this milestone exposes only current active roster assignments.

