# REST API

The JSON API is served under `/api`. It is session-based: `POST /api/auth/login` creates the same HTTP session used by the JSP application, so a browser or Angular client must retain the session cookie.

## Conventions

- Request and response bodies use `application/json` and UTF-8.
- User responses are explicit response objects. They expose `id`, `username`, `email`, role summary, and `createdAt`; they never expose password hashes, lockout counters, password-change flags, or reset-token data.
- Every API error has the same shape:

```json
{"error":{"code":"VALIDATION_ERROR","message":"Email address is required."}}
```

- Authentication returns `401`; authenticated users without the required function return `403`; invalid input returns `400`; missing resources return `404`; duplicate or protected changes return `409`.
- Existing service classes remain the source of truth for password policy, history, audit records, lockout, role protection, validation, paging, and query safety.
- Role-function requests are checked against the configured system-function list before persistence; an unknown code is rejected as `400`.

## Endpoints

| Operation | Method and route | Required function | Success |
|---|---|---|---|
| Register | `POST /api/auth/register` | Public | `201` |
| Sign in / sign out / current user | `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` | Public / session | `200` |
| Request / complete self-service reset | `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` | Public | `200` |
| List users / get one | `GET /api/users`, `GET /api/users/{id}` | `VIEW_USERS` | `200` |
| Create / edit / delete user | `POST /api/users`, `PUT /api/users/{id}`, `DELETE /api/users/{id}` | `EDIT_USER` / `DELETE_USER` | `201` / `200` |
| Change own password | `POST /api/users/me/password` | `CHANGE_OWN_PASSWORD` | `200` |
| Admin reset / unlock | `POST /api/users/{id}/reset-password`, `POST /api/users/{id}/unlock` | `RESET_PASSWORD` / `UNLOCK_USER` | `200` |
| List / create / edit / delete roles | `GET`, `POST /api/roles`; `GET`, `PUT`, `DELETE /api/roles/{id}` | `MANAGE_ROLES` | `200` / `201` |
| List available functions | `GET /api/functions` | `MANAGE_ROLES` | `200` |
| Query audit records | `GET /api/audit-log` | `VIEW_AUDIT_LOG` | `200` |

`GET /api/users` accepts `search`, `sort`, `dir`, and `page`. `GET /api/audit-log` accepts `action`, `actor`, `targetType`, `successful`, and `page`. The existing directory and audit services validate the allowed sort values and bind query values.

## Examples

```http
POST /api/auth/login
Content-Type: application/json

{"username":"farooq","password":"example-password"}
```

```http
POST /api/users
Content-Type: application/json

{"username":"new.user","email":"new.user@example.com","password":"Example#2026","roleId":2}
```

```http
PUT /api/roles/2
Content-Type: application/json

{"name":"Support","functions":["VIEW_USERS","UNLOCK_USER"]}
```

For local password-reset email delivery, configure the existing `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_STARTTLS` environment variables. Also set `PASSWORD_RESET_CLIENT_URL` to the future Angular reset screen, for example `http://localhost:4200/reset-password`. The API appends `?token=<raw-token>` to that URL. The raw token is included only in the mailed link; the database keeps only its SHA-256 hash.
