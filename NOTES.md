# Notes

## Summary of changes
1. **SQL precedence bug** (`TaskRepository`, `db/queries/search_tasks.sql`, Oracle package): `AND` bound tighter than `OR`, so archived tasks leaked into results and the status filter was ignored for title matches. Wrapped the title/description `OR` in parentheses.
2. **Artificial delay**: removed `Thread.sleep` in `TaskController` that made short search terms slow.
3. **Input validation**: invalid `status` returned 500 and `page < 1` crashed in `subList`. Both now return 400; `pageSize` is capped at 100.
4. **DB pagination**: replaced in-memory `subList` with a `Pageable` query plus count query.
5. **LIKE wildcards**: `%` and `_` in user input are escaped so they match literally.
6. **Frontend `useTasks`**: added request cancellation (stale responses could overwrite newer ones), cleared old errors, reset `loading` on failure.
7. **UX**: 300 ms search debounce; page resets to 1 when a filter changes.

## What I chose not to change
Add/edit endpoints, authentication, a proper test suite, and `System.out` logging (the removed logging line was tied to the sleep). Kept the diff small on purpose.

## Biggest remaining risk
No automated tests, and CORS is hard-coded to `localhost:5173`. The `tasks` table also has no index on `archived`/`created_at`, which will hurt as the table grows.

## Tools / AI used
Used Claude (Claude Code) to review the repo, draft the fixes and this file. I reviewed each change and wrote the handwritten explanations myself. The backend was not run in the AI sandbox (Maven Central blocked), so I ran it locally to verify.
