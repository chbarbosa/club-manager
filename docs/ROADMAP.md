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

- Active next hardening feature.
- Add Dockerfiles for backend and frontend.
- Add Docker Compose for local full-stack startup.
- Support PostgreSQL deployment while keeping H2 convenient for local
  development.
- Document required environment variables and deployment profiles.
