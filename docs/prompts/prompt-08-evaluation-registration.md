# Prompt 08 — Evaluation Registration

## Summary
Implement the first evaluation workflow. Club admins can create evaluations for active teams, list/filter them, view details, update open/in-progress evaluations, start evaluations, and finalize evaluations.

## Backend Changes
- Add Flyway migration `V9__evaluation.sql`.
- Add `Evaluation` entity and `EvaluationStatus` enum: `OPEN`, `IN_PROGRESS`, `FINALIZED`.
- Add repository, DTOs, MapStruct mapper, service, and controller.
- Expose admin-only endpoints:
  - `POST /api/v1/evaluations`
  - `GET /api/v1/evaluations`
  - `GET /api/v1/evaluations/{uuid}`
  - `PUT /api/v1/evaluations/{uuid}`
  - `PATCH /api/v1/evaluations/{uuid}/start`
  - `PATCH /api/v1/evaluations/{uuid}/finalize`
- Validate:
  - title must not be blank;
  - team must exist and be active;
  - finalized evaluations cannot be updated;
  - only open evaluations can be started;
  - finalized evaluations cannot be finalized again.
- Keep services returning domain entities and controllers mapping to response DTOs.
- Return UUID-only responses, including team and trainer display fields; never expose internal IDs.

## Frontend Changes
- Add evaluations API client.
- Add evaluation form, list page, and detail/edit page.
- Add `/evaluations` and `/evaluations/:uuid` protected routes.
- Add Evaluations links to the navbar and dashboard.
- Support status/team filtering, start, and finalize actions.

## Verification
- `mvn clean test`
- `npm.cmd run build`
- `npm.cmd run test:e2e:list`
- `npm.cmd run test:e2e`

## Deferred
- Evaluation player snapshots.
- Evaluation events and attendance.
- Evaluation results and levels.

