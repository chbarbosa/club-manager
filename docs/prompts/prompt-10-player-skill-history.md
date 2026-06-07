# Prompt 10: Player Skill History

## Summary
Persist player skill progression after evaluation events. Completing an evaluation event updates each assigned player's current skill level and writes a history entry.

## Domain Rules
- `player.currentSkillLevel` stores the latest evaluated level.
- `player_skill_history` preserves every event-driven skill update.
- Event completion remains blocked until every active evaluation player has participation and skill level recorded.
- Completing an event writes one history row per assigned player.
- History rows include:
  - player;
  - skill level;
  - changed timestamp;
  - admin who completed the event when available;
  - evaluation event;
  - short description.

## API Additions
- `GET /api/v1/players/{uuid}/skill-history`

## Frontend Changes
- Player list shows current skill level.
- Player detail shows current skill level and a skill history table.

## Notes
- Skill levels remain fixed enum values for now: `DEBUTANT`, `ADVANCED`, `SKILLED`.
- Configurable evaluation level labels can be revisited later through `club_setup`.
