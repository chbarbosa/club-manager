# Club Manager Roadmap

This document records planned future milestones that should be implemented
after the current core workflows are stable. It is intentionally high level:
each item should become a focused implementation plan before coding starts.

## Product Features

### Championships

- Create and manage championships or tournaments.
- Link each championship to one complete team.
- Track start and end month/year.
- Show the participating team players from the active team roster.
- Add the number of expected matches so completed championships can be checked
  against registered team matches.

### Club Analysis

- Planned as a daily club health snapshot generated from current backend data.
- The feature name in the UI should remain "Club Analysis".
- When an admin opens the Club Analysis page, the backend checks whether an
  analysis already exists for the current day. If it exists, return it. If not,
  generate and persist one analysis for that day.
- Keep historical analyses readable through a history list and a detail page.
- Analysis items should support severity values such as info, warning, and
  critical so the UI can guide administrators without blocking normal work.
- Initial checks should include:
  - number of players and lists of players without skill level or positions;
  - teams with fewer than 18 active players;
  - teams without goalkeepers;
  - teams missing any assistant role;
  - championships started more than one month ago without registered matches;
  - ended championships where registered matches are fewer than expected
    matches;
  - number and list of evaluations that are not ended;
  - teams without a scheduled training schedule.
- Prefer backend-generated analysis items so the rules are consistent across
  the UI, reports, and future notifications.

### Reports And Exports

- Implemented for the first CSV set.
- Export operational data for club administration.
- Current CSV exports include player lists, team rosters, schedules,
  championships, evaluation results, and match analysis.
- Future improvements can add skill history exports, filtered exports, and PDF
  reports.

### Team Composition Advice

- Show team-balance advice on the team detail/edit experience.
- After a team has about 12 assigned players, highlight composition risks:
  no goalkeeper, only one goalkeeper, few defenders, few midfielders, or few
  attackers.
- Use existing roster and player-position data first.
- Prefer backend-generated advice so the rules are consistent across UI and
  future reports.

### Match Player Analysis

- Implemented.
- Let trainers register matches for a team.
- For each player, record improvement opportunities such as passing, physical
  preparation, shooting, positioning, decision making, or excessive ball
  control.
- Record highlights such as goals scored, assists, good shots, good passes,
  tackles and interceptions, and good physical condition.
- Seed the first set of tags, then consider making them configurable through
  club setup.

## Hardening Phase

### Trackability And Audit Trail

- Implemented as an internal-only audit API.
- Track who created, updated, canceled, deactivated, or reactivated important
  records.
- Add audit fields or audit events for players, trainers, teams, schedules,
  evaluations, championships, and admin actions.
- Preserve audit records for multi-admin accountability.

### Observability Hardening

- Implemented for backend Actuator, Prometheus, and key business counters.
- Improve structured logs and make trace IDs easy to follow.
- Add meaningful metrics for login, validation failures, schedules,
  evaluations, and error rates.
- Expand health/readiness checks for deployment.

### Security Hardening

- Implemented for the first backend hardening slice.
- Externalize and strengthen JWT secrets.
- Add password policy and login rate limiting, with Redis-backed storage for
  elastic deployments.
- Review CORS and environment-specific configuration.
- Prepare role separation for admin, trainer, and shared player credentials.

### Docker And Deployment Readiness

- Implemented for local Docker startup.
- Dockerfiles exist for backend and frontend.
- Docker Compose starts the frontend, backend, Redis-backed login throttling,
  and persistent H2 file data.
- PostgreSQL deployment remains the next deployment subtask because existing
  Flyway migrations need a portability or vendor-specific migration pass.
- Future work should document production profiles, reverse proxy/firewall
  expectations, secret handling, and PostgreSQL backup/restore.
