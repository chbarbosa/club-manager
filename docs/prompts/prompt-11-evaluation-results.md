# Prompt 11: Evaluation Results

## Summary
Persist final evaluation results from a dedicated participant evaluation step after all events are closed. Events record participation only; final skill levels are assigned later for each active participant before the evaluation can be finalized.

## Backend Changes
- Add `evaluation_result` with UUID public identifiers and internal numeric IDs.
- Store evaluation, player, optional source event, final skill level, participation status, and finalization timestamp.
- Add `GET /api/v1/evaluations/{uuid}/results`.
- Add `PUT /api/v1/evaluations/{uuid}/results/{playerUuid}` for saving the final skill level for one participant.
- Keep services returning domain entities and controllers doing DTO mapping.
- Require at least one event before an evaluation can be started.
- Require all events to be completed or canceled before an evaluation can be finalized.
- Attendance and event completion are only available while the evaluation is in progress.
- Event attendance records participation only.
- Require every active assigned player to be evaluated before finalization.
- Saving a participant evaluation updates the player's current skill level and writes skill history with the authenticated admin.

## Frontend Changes
- Add evaluations API calls for results and participant result updates.
- Add an "Evaluate participants" section on the evaluation detail page.
- Block participant evaluation until all events are completed or canceled.
- Hide event creation after the last scheduled event is closed, and alert admins that they must evaluate participants before finalizing.
- Show a Results card on finalized evaluation detail pages.
- Refresh saved results immediately after participant evaluation and finalization.

## Verification
- Backend service and MockMvc tests cover participation-only attendance, participant evaluation, player skill history, finalization blocking, and UUID-only responses.
- E2E verifies attendance, event completion, participant evaluation, finalization, and the result row.
